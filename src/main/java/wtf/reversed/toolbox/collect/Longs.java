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
    }

    public static Longs empty() {
        return EMPTY;
    }

    public static Longs wrap(long[] array) {
        return wrap(array, 0, array.length);
    }

    public static Longs wrap(long[] array, int offset, int length) {
        byte[] buffer = new byte[Math.multiplyExact(length, Long.BYTES)];
        ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asLongBuffer().put(array, offset, length);
        return new Longs(buffer, 0, buffer.length);
    }

    public static Mutable allocate(int length) {
        int byteLength = Math.multiplyExact(length, Long.BYTES);
        return new Mutable(new byte[byteLength], 0, byteLength);
    }

    public static Longs from(LongBuffer buffer) {
        Check.argument(buffer.hasArray(), "buffer must be backed by an array");
        return wrap(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
    }

    public long get(int index) {
        Check.index(index, length());
        return getInternal(index);
    }

    long getInternal(int index) {
        return (long) VH_LONG.get(array, offset + Math.multiplyExact(index, Long.BYTES));
    }

    @Override
    public int length() {
        return length >>> 3;
    }

    public boolean contains(long value) {
        return indexOf(value) >= 0;
    }

    public int indexOf(long value) {
        for (int i = 0, limit = length(); i < limit; i++) {
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

    public Longs slice(int offset) {
        return slice(offset, length() - offset);
    }

    public Longs slice(int offset, int length) {
        Check.fromIndexSize(offset, length, length());
        return new Longs(array, this.offset + Math.multiplyExact(offset, Long.BYTES), Math.multiplyExact(length, Long.BYTES));
    }

    public void copyTo(Mutable target, int offset) {
        Check.fromIndexSize(offset, length(), target.length());
        System.arraycopy(array, this.offset, target.array, target.offset + Math.multiplyExact(offset, Long.BYTES), length);
    }

    @Override
    public LongBuffer asBuffer() {
        return asByteBuffer().asLongBuffer().slice().asReadOnlyBuffer();
    }

    public LongStream stream() {
        return IntStream.range(0, length()).mapToLong(i -> getInternal(i));
    }

    public long[] toArray() {
        long[] result = new long[length()];
        asBuffer().get(result);
        return result;
    }

    @Override
    public int compareTo(Longs o) {
        int min = Math.min(length(), o.length());
        for (int i = 0; i < min; i++) {
            int c = Long.compare(getInternal(i), o.getInternal(i));
            if (c != 0) {
                return c;
            }
        }
        return Integer.compare(length(), o.length());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Longs o)) {
            return false;
        }
        return Arrays.equals(array, offset, offset + length, o.array, o.offset, o.offset + o.length);
    }

    @Override
    public int hashCode() {
        int result = 1;
        for (int i = 0, len = length(); i < len; i++) {
            result = 31 * result + Long.hashCode(getInternal(i));
        }
        return result;
    }

    @Override
    public String toString() {
        return "[" + length() + " longs]";
    }

    public static final class Mutable extends Longs {
        Mutable(byte[] array, int offset, int length) {
            super(array, offset, length);
        }

        public static Mutable wrap(long[] array) {
            return wrap(array, 0, array.length);
        }

        public static Mutable wrap(long[] array, int offset, int length) {
            byte[] buffer = new byte[Math.multiplyExact(length, Long.BYTES)];
            ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asLongBuffer().put(array, offset, length);
            return new Mutable(buffer, 0, buffer.length);
        }

        public Mutable set(int index, long value) {
            Check.index(index, length());
            return setInternal(index, value);
        }

        private Mutable setInternal(int index, long value) {
            VH_LONG.set(array, offset + Math.multiplyExact(index, Long.BYTES), value);
            return this;
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
            asByteBuffer().asLongBuffer().put(src, offset, length);
            return this;
        }

        public Mutable copyWithin(int srcIndex, int dstIndex, int length) {
            copyWithinBytes(Math.multiplyExact(srcIndex, Long.BYTES), Math.multiplyExact(dstIndex, Long.BYTES), Math.multiplyExact(length, Long.BYTES));
            return this;
        }

        public Mutable fill(long value) {
            if (value == (value & 0xFF) * 0x0101010101010101L) {
                Arrays.fill(array, offset, offset + length, (byte) value);
            } else {
                for (int i = 0; i < length(); i++) {
                    setInternal(i, value);
                }
            }
            return this;
        }

        public Mutable fillFrom(BinarySource source) throws IOException {
            source.readBytes(new Bytes.Mutable(array, offset, length));
            if (source.order() == ByteOrder.BIG_ENDIAN) {
                for (int i = 0, len = length(); i < len; i++) {
                    setInternal(i, (long) VH_LONG_BE.get(array, offset + Math.multiplyExact(i, Long.BYTES)));
                }
            }
            return this;
        }

        public LongBuffer asMutableBuffer() {
            return asByteBuffer().asLongBuffer().slice();
        }
    }
}
