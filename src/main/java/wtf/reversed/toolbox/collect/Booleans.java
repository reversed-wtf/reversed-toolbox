package wtf.reversed.toolbox.collect;

import wtf.reversed.toolbox.io.*;
import wtf.reversed.toolbox.util.*;

import java.io.*;
import java.nio.*;
import java.util.*;
import java.util.stream.*;

/**
 * A view of bit-packed booleans, stored least-significant bit first within each byte.
 * <p>
 * Indices, lengths, and slice offsets are measured in bits. Slices share storage and may start at any bit.
 * Byte and buffer views expose the covering bytes, including neighboring bits in the boundary bytes;
 * {@link #bitOffset()} identifies the first logical bit within the first byte.
 */
public sealed class Booleans extends Slice implements Comparable<Booleans> {
    private static final Booleans EMPTY = new Booleans(EMPTY_ARRAY, 0, 0, 0);

    final int bitOffset;
    final int bitLength;

    Booleans(byte[] array, int offset, int bitOffset, int bitLength) {
        super(array, offset, coveringBytes(bitOffset, bitLength));
        this.bitOffset = bitOffset;
        this.bitLength = bitLength;
    }

    private static int coveringBytes(int bitOffset, int bitLength) {
        Check.argument(bitOffset >= 0 && bitOffset < Byte.SIZE, "bitOffset must be between 0 and 7");
        Check.positiveOrZero(bitLength, "length");
        return bitLength == 0 ? 0 : (int) ((long) bitOffset + bitLength + 7 >>> 3);
    }

    public static Booleans empty() {
        return EMPTY;
    }

    public static Booleans copyOf(boolean[] array) {
        return copyOf(array, 0, array.length);
    }

    public static Booleans copyOf(boolean[] array, int offset, int length) {
        Mutable copy = Mutable.copyOf(array, offset, length);
        return new Booleans(copy.array, 0, 0, length);
    }

    public static Mutable allocate(int length) {
        return new Mutable(new byte[coveringBytes(0, length)], 0, 0, length);
    }

    /**
     * Returns the offset, from 0 to 7, of the first logical bit within the first covering byte.
     */
    public int bitOffset() {
        return bitOffset;
    }

    @Override
    public int length() {
        return bitLength;
    }

    public boolean get(int index) {
        Check.index(index, bitLength);
        return getInternal(index);
    }

    boolean getInternal(int index) {
        long bit = (long) bitOffset + index;
        return (array[offset + (int) (bit >>> 3)] & 1 << (bit & 7)) != 0;
    }

    public boolean contains(boolean value) {
        return indexOf(value) >= 0;
    }

    public int indexOf(boolean value) {
        for (int i = 0; i < bitLength; i++) {
            if (getInternal(i) == value) {
                return i;
            }
        }
        return -1;
    }

    public int lastIndexOf(boolean value) {
        for (int i = bitLength - 1; i >= 0; i--) {
            if (getInternal(i) == value) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns a shared, read-only view of the covering bytes, including neighboring boundary bits.
     */
    @Override
    public ByteBuffer asBuffer() {
        return asByteBuffer().slice().asReadOnlyBuffer().order(ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Copies logical bits to the target, preserving bits outside the destination range.
     * Overlapping source and destination ranges are supported.
     */
    public void copyTo(Mutable target, int offset) {
        Objects.checkFromIndexSize(offset, bitLength, target.bitLength);
        long sourceStart = (long) this.offset * Byte.SIZE + bitOffset;
        long targetStart = (long) target.offset * Byte.SIZE + target.bitOffset + offset;
        if (array == target.array && targetStart > sourceStart && targetStart < sourceStart + bitLength) {
            for (int i = bitLength - 1; i >= 0; i--) {
                target.setInternal(offset + i, getInternal(i));
            }
        } else {
            for (int i = 0; i < bitLength; i++) {
                target.setInternal(offset + i, getInternal(i));
            }
        }
    }

    public void copyTo(boolean[] target) {
        copyTo(target, 0, bitLength);
    }

    public void copyTo(boolean[] target, int offset, int length) {
        Objects.checkFromIndexSize(offset, length, target.length);
        Objects.checkFromIndexSize(0, length, bitLength);
        for (int i = 0; i < length; i++) {
            target[offset + i] = getInternal(i);
        }
    }

    public Booleans slice(int offset) {
        return slice(offset, bitLength - offset);
    }

    public Booleans slice(int offset, int length) {
        Objects.checkFromIndexSize(offset, length, bitLength);
        long bit = (long) bitOffset + offset;
        return new Booleans(array, this.offset + (int) (bit >>> 3), (int) (bit & 7), length);
    }

    public Stream<Boolean> stream() {
        return IntStream.range(0, bitLength).mapToObj(this::getInternal);
    }

    public boolean[] toArray() {
        boolean[] result = new boolean[bitLength];
        copyTo(result);
        return result;
    }

    @Override
    public int compareTo(Booleans other) {
        int prefix = Math.min(bitLength, other.bitLength);
        for (int i = 0; i < prefix; i++) {
            int comparison = Boolean.compare(getInternal(i), other.getInternal(i));
            if (comparison != 0) {
                return comparison;
            }
        }
        return Integer.compare(bitLength, other.bitLength);
    }

    @Override
    boolean contentEquals(Slice that) {
        Booleans other = (Booleans) that;
        return bitLength == other.bitLength && compareTo(other) == 0;
    }

    @Override
    int contentHashCode() {
        int hash = 1;
        for (int i = 0; i < bitLength; i++) {
            hash = 31 * hash + Boolean.hashCode(getInternal(i));
        }
        return hash;
    }

    public static final class Mutable extends Booleans {
        Mutable(byte[] array, int offset, int bitOffset, int bitLength) {
            super(array, offset, bitOffset, bitLength);
        }

        public static Mutable copyOf(boolean[] array) {
            return copyOf(array, 0, array.length);
        }

        public static Mutable copyOf(boolean[] array, int offset, int length) {
            Objects.checkFromIndexSize(offset, length, array.length);
            return allocate(length).copyFrom(array, offset, length);
        }

        public Mutable set(int index, boolean value) {
            Check.index(index, bitLength);
            setInternal(index, value);
            return this;
        }

        private void setInternal(int index, boolean value) {
            long bit = (long) bitOffset + index;
            setMasked(offset + (int) (bit >>> 3), 1 << (bit & 7), value);
        }

        private void setMasked(int index, int mask, boolean value) {
            array[index] = (byte) (value ? array[index] | mask : array[index] & ~mask);
        }

        /**
         * Returns a shared writable view of the covering bytes. Writes can change neighboring bits
         * outside this slice; use {@link #set(int, boolean)} or {@link #fill(boolean)} to preserve them.
         */
        public ByteBuffer asMutableBuffer() {
            return asByteBuffer().slice().order(ByteOrder.LITTLE_ENDIAN);
        }

        @Override
        public Mutable slice(int offset) {
            return slice(offset, bitLength - offset);
        }

        @Override
        public Mutable slice(int offset, int length) {
            Objects.checkFromIndexSize(offset, length, bitLength);
            long bit = (long) bitOffset + offset;
            return new Mutable(array, this.offset + (int) (bit >>> 3), (int) (bit & 7), length);
        }

        public Mutable copyFrom(boolean[] source) {
            return copyFrom(source, 0, source.length);
        }

        public Mutable copyFrom(boolean[] source, int offset, int length) {
            Objects.checkFromIndexSize(offset, length, source.length);
            Objects.checkFromIndexSize(0, length, bitLength);
            for (int i = 0; i < length; i++) {
                setInternal(i, source[offset + i]);
            }
            return this;
        }

        public Mutable fill(boolean value) {
            if (bitLength == 0) {
                return this;
            }
            int firstMask = 0xff << bitOffset;
            int endBit = (int) ((long) bitOffset + bitLength & 7);
            int lastMask = endBit == 0 ? 0xff : (1 << endBit) - 1;
            if (length == 1) {
                setMasked(offset, firstMask & lastMask, value);
            } else {
                setMasked(offset, firstMask, value);
                Arrays.fill(array, offset + 1, offset + length - 1, value ? (byte) 0xff : 0);
                setMasked(offset + length - 1, lastMask, value);
            }
            return this;
        }

        /**
         * Reads packed bits LSB-first, consuming {@code ceil(length() / 8)} bytes regardless of source byte order.
         * Unused high bits in the final input byte are ignored. Neighboring destination bits are preserved.
         */
        public Mutable fillFrom(BinarySource source) throws IOException {
            for (int i = 0; i < bitLength; ) {
                int bits = source.readUnsignedByte();
                int count = Math.min(Byte.SIZE, bitLength - i);
                for (int j = 0; j < count; j++) {
                    setInternal(i + j, (bits & 1 << j) != 0);
                }
                i += count;
            }
            return this;
        }
    }
}
