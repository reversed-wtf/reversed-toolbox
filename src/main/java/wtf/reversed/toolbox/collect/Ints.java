package wtf.reversed.toolbox.collect;

import wtf.reversed.toolbox.io.*;
import wtf.reversed.toolbox.util.*;

import javax.annotation.processing.*;
import java.io.*;
import java.nio.*;
import java.util.*;
import java.util.stream.*;

@Generated("wtf.reversed.toolbox.util.SliceGenerator")
public sealed class Ints extends Slice implements Comparable<Ints> {
    private static final Ints EMPTY = new Ints(EMPTY_ARRAY, 0, 0);

    Ints(byte[] array, int offset, int length) {
        super(array, offset, length);
    }

    public static Ints empty() {
        return EMPTY;
    }

    public static Ints copyOf(int[] array) {
        return copyOf(array, 0, array.length);
    }

    public static Ints copyOf(int[] array, int offset, int length) {
        byte[] buffer = new byte[Math.multiplyExact(length, Integer.BYTES)];
        ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().put(array, offset, length);
        return new Ints(buffer, 0, buffer.length);
    }

    public static Ints copyOf(IntBuffer buffer) {
        Check.argument(buffer.hasArray(), "buffer must be backed by an array");
        return copyOf(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
    }

    public static Mutable allocate(int length) {
        int byteLength = Math.multiplyExact(length, Integer.BYTES);
        return new Mutable(new byte[byteLength], 0, byteLength);
    }

    public int get(int index) {
        Check.index(index, length());
        return getInternal(index);
    }

    public long getUnsigned(int offset) {
        return Integer.toUnsignedLong(get(offset));
    }

    int getInternal(int index) {
        return (int) VH_INT_LE.get(array, offset + index * Integer.BYTES);
    }

    @Override
    public int length() {
        return length >>> 2;
    }

    public boolean contains(int value) {
        return indexOf(value) >= 0;
    }

    public int indexOf(int value) {
        for (int i = 0, len = length(); i < len; i++) {
            if (getInternal(i) == value) {
                return i;
            }
        }
        return -1;
    }

    public int lastIndexOf(int value) {
        for (int i = length() - 1; i >= 0; i--) {
            if (getInternal(i) == value) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public IntBuffer asBuffer() {
        return asTypedBuffer().slice().asReadOnlyBuffer();
    }

    public void copyTo(Mutable target, int offset) {
        Check.fromIndexSize(offset, length(), target.length());
        System.arraycopy(array, this.offset, target.array, target.offset + Math.multiplyExact(offset, Integer.BYTES), length);
    }

    public void copyTo(int[] dst) {
        copyTo(dst, 0, length());
    }

    public void copyTo(int[] dst, int offset, int length) {
        Check.fromIndexSize(offset, length, dst.length);
        Check.fromIndexSize(0, length, length());
        asTypedBuffer().get(dst, offset, length);
    }

    public Ints slice(int offset) {
        return slice(offset, length() - offset);
    }

    public Ints slice(int offset, int length) {
        Check.fromIndexSize(offset, length, length());
        return new Ints(array, this.offset + Math.multiplyExact(offset, Integer.BYTES), Math.multiplyExact(length, Integer.BYTES));
    }

    public IntStream stream() {
        return IntStream.range(0, length()).map(i -> getInternal(i));
    }

    public int[] toArray() {
        int[] result = new int[length()];
        copyTo(result);
        return result;
    }

    IntBuffer asTypedBuffer() {
        return asByteBuffer().asIntBuffer();
    }

    @Override
    public int compareTo(Ints o) {
        int prefix = Math.min(length, o.length);
        int mismatch = Arrays.mismatch(array, offset, offset + prefix, o.array, o.offset, o.offset + prefix);
        if (mismatch < 0) {
            return Integer.compare(length(), o.length());
        }
        int idx = mismatch >>> 2;
        return Integer.compare(getInternal(idx), o.getInternal(idx));
    }

    public static final class Mutable extends Ints {
        Mutable(byte[] array, int offset, int length) {
            super(array, offset, length);
        }

        public static Mutable copyOf(int[] array) {
            return copyOf(array, 0, array.length);
        }

        public static Mutable copyOf(int[] array, int offset, int length) {
            byte[] buffer = new byte[Math.multiplyExact(length, Integer.BYTES)];
            ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().put(array, offset, length);
            return new Mutable(buffer, 0, buffer.length);
        }

        public Mutable set(int index, int value) {
            Check.index(index, length());
            return setInternal(index, value);
        }

        private Mutable setInternal(int index, int value) {
            VH_INT_LE.set(array, offset + index * Integer.BYTES, value);
            return this;
        }

        public IntBuffer asMutableBuffer() {
            return asTypedBuffer().slice();
        }

        public Mutable slice(int offset) {
            return slice(offset, length() - offset);
        }

        public Mutable slice(int offset, int length) {
            Check.fromIndexSize(offset, length, length());
            return new Mutable(array, this.offset + Math.multiplyExact(offset, Integer.BYTES), Math.multiplyExact(length, Integer.BYTES));
        }

        public Mutable copyFrom(int[] src) {
            return copyFrom(src, 0, src.length);
        }

        public Mutable copyFrom(int[] src, int offset, int length) {
            Check.fromIndexSize(offset, length, src.length);
            Check.fromIndexSize(0, length, length());
            asTypedBuffer().put(src, offset, length);
            return this;
        }

        public Mutable fill(int value) {
            if (value == (value & 0xFF) * 0x0101_0101) {
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
                    setInternal(i, (int) VH_INT_BE.get(array, offset + i * Integer.BYTES));
                }
            }
            return this;
        }
    }
}
