package wtf.reversed.toolbox.collect;

import wtf.reversed.toolbox.io.*;
import wtf.reversed.toolbox.util.*;

import javax.annotation.processing.*;
import java.io.*;
import java.nio.*;
import java.util.*;
import java.util.stream.*;

@Generated("wtf.reversed.toolbox.util.SliceGenerator")
public sealed class Shorts extends Slice implements Comparable<Shorts> {
    private static final Shorts EMPTY = new Shorts(EMPTY_ARRAY, 0, 0);

    Shorts(byte[] array, int offset, int length) {
        super(array, offset, length);
        Check.argument((length & (Short.BYTES - 1)) == 0, "length must be a multiple of 2");
    }

    public static Shorts empty() {
        return EMPTY;
    }

    public static Shorts copyOf(short[] array) {
        return copyOf(array, 0, array.length);
    }

    public static Shorts copyOf(short[] array, int offset, int length) {
        byte[] buffer = new byte[Math.multiplyExact(length, Short.BYTES)];
        ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(array, offset, length);
        return new Shorts(buffer, 0, buffer.length);
    }

    public static Shorts copyOf(ShortBuffer buffer) {
        Check.argument(buffer.hasArray(), "buffer must be backed by an array");
        return copyOf(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
    }

    public static Mutable allocate(int length) {
        int byteLength = Math.multiplyExact(length, Short.BYTES);
        return new Mutable(new byte[byteLength], 0, byteLength);
    }

    public short get(int index) {
        Check.index(index, length());
        return getInternal(index);
    }

    public int getUnsigned(int offset) {
        return Short.toUnsignedInt(get(offset));
    }

    short getInternal(int index) {
        return (short) VH_SHORT_LE.get(array, offset + index * Short.BYTES);
    }

    @Override
    public int length() {
        return length >>> 1;
    }

    public boolean contains(short value) {
        return indexOf(value) >= 0;
    }

    public int indexOf(short value) {
        for (int i = 0, len = length(); i < len; i++) {
            if (getInternal(i) == value) {
                return i;
            }
        }
        return -1;
    }

    public int lastIndexOf(short value) {
        for (int i = length() - 1; i >= 0; i--) {
            if (getInternal(i) == value) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public ShortBuffer asBuffer() {
        return asTypedBuffer().slice().asReadOnlyBuffer();
    }

    public void copyTo(Mutable target, int offset) {
        Check.fromIndexSize(offset, length(), target.length());
        System.arraycopy(array, this.offset, target.array, target.offset + Math.multiplyExact(offset, Short.BYTES), length);
    }

    public void copyTo(short[] dst) {
        copyTo(dst, 0, length());
    }

    public void copyTo(short[] dst, int offset, int length) {
        Check.fromIndexSize(offset, length, dst.length);
        Check.fromIndexSize(0, length, length());
        asTypedBuffer().get(dst, offset, length);
    }

    public Shorts slice(int offset) {
        return slice(offset, length() - offset);
    }

    public Shorts slice(int offset, int length) {
        Check.fromIndexSize(offset, length, length());
        return new Shorts(array, this.offset + Math.multiplyExact(offset, Short.BYTES), Math.multiplyExact(length, Short.BYTES));
    }

    public IntStream stream() {
        return IntStream.range(0, length()).map(i -> getInternal(i));
    }

    public short[] toArray() {
        short[] result = new short[length()];
        copyTo(result);
        return result;
    }

    ShortBuffer asTypedBuffer() {
        return asByteBuffer().asShortBuffer();
    }

    @Override
    public int compareTo(Shorts o) {
        int prefix = Math.min(length, o.length);
        int mismatch = Arrays.mismatch(array, offset, offset + prefix, o.array, o.offset, o.offset + prefix);
        if (mismatch < 0) {
            return Integer.compare(length(), o.length());
        }
        int idx = mismatch >>> 1;
        return Short.compare(getInternal(idx), o.getInternal(idx));
    }

    public static final class Mutable extends Shorts {
        Mutable(byte[] array, int offset, int length) {
            super(array, offset, length);
        }

        public static Mutable copyOf(short[] array) {
            return copyOf(array, 0, array.length);
        }

        public static Mutable copyOf(short[] array, int offset, int length) {
            byte[] buffer = new byte[Math.multiplyExact(length, Short.BYTES)];
            ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(array, offset, length);
            return new Mutable(buffer, 0, buffer.length);
        }

        public Mutable set(int index, short value) {
            Check.index(index, length());
            return setInternal(index, value);
        }

        private Mutable setInternal(int index, short value) {
            VH_SHORT_LE.set(array, offset + index * Short.BYTES, value);
            return this;
        }

        public ShortBuffer asMutableBuffer() {
            return asTypedBuffer().slice();
        }

        public Mutable slice(int offset) {
            return slice(offset, length() - offset);
        }

        public Mutable slice(int offset, int length) {
            Check.fromIndexSize(offset, length, length());
            return new Mutable(array, this.offset + Math.multiplyExact(offset, Short.BYTES), Math.multiplyExact(length, Short.BYTES));
        }

        public Mutable copyFrom(short[] src) {
            return copyFrom(src, 0, src.length);
        }

        public Mutable copyFrom(short[] src, int offset, int length) {
            Check.fromIndexSize(offset, length, src.length);
            Check.fromIndexSize(0, length, length());
            asTypedBuffer().put(src, offset, length);
            return this;
        }

        public Mutable fill(short value) {
            if (value == (value & 0xFF) * 0x0101) {
                Arrays.fill(array, offset, offset + length, (byte) value);
            } else {
                for (int i = 0, len = length(); i < len; i++) {
                    setInternal(i, value);
                }
            }
            return this;
        }

        public Mutable fillFrom(BinarySource source) throws IOException {
            source.readBytes(new Bytes.Mutable(array, offset, length));
            if (source.order() == ByteOrder.BIG_ENDIAN) {
                for (int i = 0, len = length(); i < len; i++) {
                    setInternal(i, (short) VH_SHORT_BE.get(array, offset + i * Short.BYTES));
                }
            }
            return this;
        }
    }
}
