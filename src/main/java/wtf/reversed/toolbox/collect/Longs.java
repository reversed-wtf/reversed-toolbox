package wtf.reversed.toolbox.collect;

import wtf.reversed.toolbox.io.*;
import wtf.reversed.toolbox.util.*;

import javax.annotation.processing.*;
import java.io.*;
import java.nio.*;
import java.util.*;
import java.util.stream.*;

@Generated("wtf.reversed.toolbox.util.SliceGenerator")
public sealed class Longs extends Slice implements Comparable<Longs> {
    private static final Longs EMPTY = new Longs(EMPTY_ARRAY, 0, 0);

    Longs(byte[] array, int offset, int length) {
        super(array, offset, length);
        Check.argument((length & (Long.BYTES - 1)) == 0, "length must be a multiple of 8");
    }

    public static Longs empty() {
        return EMPTY;
    }

    public static Longs copyOf(long[] array) {
        return copyOf(array, 0, array.length);
    }

    public static Longs copyOf(long[] array, int offset, int length) {
        byte[] buffer = new byte[Math.multiplyExact(length, Long.BYTES)];
        ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asLongBuffer().put(array, offset, length);
        return new Longs(buffer, 0, buffer.length);
    }

    public static Longs copyOf(LongBuffer buffer) {
        Check.argument(buffer.hasArray(), "buffer must be backed by an array");
        return copyOf(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
    }

    public static Mutable allocate(int length) {
        int byteLength = Math.multiplyExact(length, Long.BYTES);
        return new Mutable(new byte[byteLength], 0, byteLength);
    }

    public long get(int index) {
        Check.index(index, length());
        return getInternal(index);
    }

    long getInternal(int index) {
        return (long) VH_LONG_LE.get(array, offset + index * Long.BYTES);
    }

    @Override
    public int length() {
        return length >>> 3;
    }

    public boolean contains(long value) {
        return indexOf(value) >= 0;
    }

    public int indexOf(long value) {
        for (int i = 0, len = length(); i < len; i++) {
            if (getInternal(i) == value) {
                return i;
            }
        }
        return -1;
    }

    public int lastIndexOf(long value) {
        for (int i = length() - 1; i >= 0; i--) {
            if (getInternal(i) == value) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public LongBuffer asBuffer() {
        return asTypedBuffer().slice().asReadOnlyBuffer();
    }

    public void copyTo(Mutable target, int offset) {
        Check.fromIndexSize(offset, length(), target.length());
        System.arraycopy(array, this.offset, target.array, target.offset + Math.multiplyExact(offset, Long.BYTES), length);
    }

    public void copyTo(long[] dst) {
        copyTo(dst, 0, length());
    }

    public void copyTo(long[] dst, int offset, int length) {
        Check.fromIndexSize(offset, length, dst.length);
        Check.fromIndexSize(0, length, length());
        asTypedBuffer().get(dst, offset, length);
    }

    public Longs slice(int offset) {
        return slice(offset, length() - offset);
    }

    public Longs slice(int offset, int length) {
        Check.fromIndexSize(offset, length, length());
        return new Longs(array, this.offset + Math.multiplyExact(offset, Long.BYTES), Math.multiplyExact(length, Long.BYTES));
    }

    public LongStream stream() {
        return IntStream.range(0, length()).mapToLong(i -> getInternal(i));
    }

    public long[] toArray() {
        long[] result = new long[length()];
        copyTo(result);
        return result;
    }

    LongBuffer asTypedBuffer() {
        return asByteBuffer().asLongBuffer();
    }

    @Override
    public int compareTo(Longs o) {
        int prefix = Math.min(length, o.length);
        int mismatch = Arrays.mismatch(array, offset, offset + prefix, o.array, o.offset, o.offset + prefix);
        if (mismatch < 0) {
            return Integer.compare(length(), o.length());
        }
        int idx = mismatch >>> 3;
        return Long.compare(getInternal(idx), o.getInternal(idx));
    }

    public static final class Mutable extends Longs {
        Mutable(byte[] array, int offset, int length) {
            super(array, offset, length);
        }

        public static Mutable copyOf(long[] array) {
            return copyOf(array, 0, array.length);
        }

        public static Mutable copyOf(long[] array, int offset, int length) {
            byte[] buffer = new byte[Math.multiplyExact(length, Long.BYTES)];
            ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asLongBuffer().put(array, offset, length);
            return new Mutable(buffer, 0, buffer.length);
        }

        public Mutable set(int index, long value) {
            Check.index(index, length());
            return setInternal(index, value);
        }

        private Mutable setInternal(int index, long value) {
            VH_LONG_LE.set(array, offset + index * Long.BYTES, value);
            return this;
        }

        public LongBuffer asMutableBuffer() {
            return asTypedBuffer().slice();
        }

        public Mutable slice(int offset) {
            return slice(offset, length() - offset);
        }

        public Mutable slice(int offset, int length) {
            Check.fromIndexSize(offset, length, length());
            return new Mutable(array, this.offset + Math.multiplyExact(offset, Long.BYTES), Math.multiplyExact(length, Long.BYTES));
        }

        public Mutable copyFrom(long[] src) {
            return copyFrom(src, 0, src.length);
        }

        public Mutable copyFrom(long[] src, int offset, int length) {
            Check.fromIndexSize(offset, length, src.length);
            Check.fromIndexSize(0, length, length());
            asTypedBuffer().put(src, offset, length);
            return this;
        }

        public Mutable fill(long value) {
            if (value == (value & 0xFF) * 0x0101_0101_0101_0101L) {
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
                    setInternal(i, (long) VH_LONG_BE.get(array, offset + i * Long.BYTES));
                }
            }
            return this;
        }
    }
}
