package wtf.reversed.toolbox.collect;

import wtf.reversed.toolbox.io.*;
import wtf.reversed.toolbox.util.*;

import javax.annotation.processing.*;
import java.io.*;
import java.nio.*;
import java.util.*;
import java.util.stream.*;

@Generated("wtf.reversed.toolbox.util.SliceGenerator")
public sealed class Doubles extends Slice implements Comparable<Doubles> {
    private static final Doubles EMPTY = new Doubles(EMPTY_ARRAY, 0, 0);

    Doubles(byte[] array, int offset, int length) {
        super(array, offset, length);
        Check.argument((length & (Double.BYTES - 1)) == 0, "length must be a multiple of 8");
    }

    public static Doubles empty() {
        return EMPTY;
    }

    public static Doubles copyOf(double[] array) {
        return copyOf(array, 0, array.length);
    }

    public static Doubles copyOf(double[] array, int offset, int length) {
        byte[] buffer = new byte[Math.multiplyExact(length, Double.BYTES)];
        ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asDoubleBuffer().put(array, offset, length);
        return new Doubles(buffer, 0, buffer.length);
    }

    public static Doubles copyOf(DoubleBuffer buffer) {
        Check.argument(buffer.hasArray(), "buffer must be backed by an array");
        return copyOf(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
    }

    public static Mutable allocate(int length) {
        int byteLength = Math.multiplyExact(length, Double.BYTES);
        return new Mutable(new byte[byteLength], 0, byteLength);
    }

    public double get(int index) {
        Check.index(index, length());
        return getInternal(index);
    }

    double getInternal(int index) {
        return (double) VH_DOUBLE_LE.get(array, offset + index * Double.BYTES);
    }

    @Override
    public int length() {
        return length >>> 3;
    }

    public boolean contains(double value) {
        return indexOf(value) >= 0;
    }

    public int indexOf(double value) {
        for (int i = 0, len = length(); i < len; i++) {
            if (Double.doubleToRawLongBits(getInternal(i)) == Double.doubleToRawLongBits(value)) {
                return i;
            }
        }
        return -1;
    }

    public int lastIndexOf(double value) {
        for (int i = length() - 1; i >= 0; i--) {
            if (Double.doubleToRawLongBits(getInternal(i)) == Double.doubleToRawLongBits(value)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public DoubleBuffer asBuffer() {
        return asTypedBuffer().slice().asReadOnlyBuffer();
    }

    public void copyTo(Mutable target, int offset) {
        Check.fromIndexSize(offset, length(), target.length());
        System.arraycopy(array, this.offset, target.array, target.offset + Math.multiplyExact(offset, Double.BYTES), length);
    }

    public void copyTo(double[] dst) {
        copyTo(dst, 0, length());
    }

    public void copyTo(double[] dst, int offset, int length) {
        Check.fromIndexSize(offset, length, dst.length);
        Check.fromIndexSize(0, length, length());
        asTypedBuffer().get(dst, offset, length);
    }

    public Doubles slice(int offset) {
        return slice(offset, length() - offset);
    }

    public Doubles slice(int offset, int length) {
        Check.fromIndexSize(offset, length, length());
        return new Doubles(array, this.offset + Math.multiplyExact(offset, Double.BYTES), Math.multiplyExact(length, Double.BYTES));
    }

    public DoubleStream stream() {
        return IntStream.range(0, length()).mapToDouble(i -> getInternal(i));
    }

    public double[] toArray() {
        double[] result = new double[length()];
        copyTo(result);
        return result;
    }

    DoubleBuffer asTypedBuffer() {
        return asByteBuffer().asDoubleBuffer();
    }

    @Override
    public int compareTo(Doubles o) {
        int prefix = Math.min(length, o.length);
        int mismatch = Arrays.mismatch(array, offset, offset + prefix, o.array, o.offset, o.offset + prefix);
        if (mismatch < 0) {
            return Integer.compare(length(), o.length());
        }
        int idx = mismatch >>> 3;
        return Long.compare(Double.doubleToRawLongBits(getInternal(idx)), Double.doubleToRawLongBits(o.getInternal(idx)));
    }

    public static final class Mutable extends Doubles {
        Mutable(byte[] array, int offset, int length) {
            super(array, offset, length);
        }

        public static Mutable copyOf(double[] array) {
            return copyOf(array, 0, array.length);
        }

        public static Mutable copyOf(double[] array, int offset, int length) {
            byte[] buffer = new byte[Math.multiplyExact(length, Double.BYTES)];
            ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asDoubleBuffer().put(array, offset, length);
            return new Mutable(buffer, 0, buffer.length);
        }

        public Mutable set(int index, double value) {
            Check.index(index, length());
            return setInternal(index, value);
        }

        private Mutable setInternal(int index, double value) {
            VH_DOUBLE_LE.set(array, offset + index * Double.BYTES, value);
            return this;
        }

        public DoubleBuffer asMutableBuffer() {
            return asTypedBuffer().slice();
        }

        public Mutable slice(int offset) {
            return slice(offset, length() - offset);
        }

        public Mutable slice(int offset, int length) {
            Check.fromIndexSize(offset, length, length());
            return new Mutable(array, this.offset + Math.multiplyExact(offset, Double.BYTES), Math.multiplyExact(length, Double.BYTES));
        }

        public Mutable copyFrom(double[] src) {
            return copyFrom(src, 0, src.length);
        }

        public Mutable copyFrom(double[] src, int offset, int length) {
            Check.fromIndexSize(offset, length, src.length);
            Check.fromIndexSize(0, length, length());
            asTypedBuffer().put(src, offset, length);
            return this;
        }

        public Mutable fill(double value) {
            if (Double.doubleToRawLongBits(value) == 0L) {
                Arrays.fill(array, offset, offset + length, (byte) 0);
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
                    setInternal(i, (double) VH_DOUBLE_BE.get(array, offset + i * Double.BYTES));
                }
            }
            return this;
        }
    }
}
