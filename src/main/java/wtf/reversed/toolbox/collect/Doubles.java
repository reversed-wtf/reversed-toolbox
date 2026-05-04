package wtf.reversed.toolbox.collect;

import wtf.reversed.toolbox.io.*;
import wtf.reversed.toolbox.util.*;

import javax.annotation.processing.*;
import java.io.*;
import java.nio.*;
import java.util.stream.*;

@Generated("wtf.reversed.toolbox.util.SliceGenerator")
public sealed class Doubles extends Slice implements Comparable<Doubles> {
    private static final Doubles EMPTY = new Doubles(new byte[0], 0, 0);

    Doubles(byte[] array, int offset, int length) {
        super(array, offset, length);
    }

    public static Doubles empty() {
        return EMPTY;
    }

    public static Doubles wrap(double[] array) {
        return wrap(array, 0, array.length);
    }

    public static Doubles wrap(double[] array, int offset, int length) {
        byte[] buffer = new byte[length * Double.BYTES];
        ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asDoubleBuffer().put(array, offset, length);
        return new Doubles(buffer, 0, buffer.length);
    }

    public static Mutable allocate(int length) {
        int byteLength = Math.multiplyExact(length, Double.BYTES);
        return new Mutable(new byte[byteLength], 0, byteLength);
    }

    public static Doubles from(DoubleBuffer buffer) {
        Check.argument(buffer.hasArray(), "buffer must be backed by an array");
        return wrap(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
    }

    public double get(int index) {
        Check.index(index, length);
        return getInternal(index);
    }

    double getInternal(int index) {
        return (double) VH_DOUBLE.get(array, offset + index * Double.BYTES);
    }

    @Override
    public int length() {
        return length >>> 3;
    }

    public boolean contains(double value) {
        return indexOf(value) >= 0;
    }

    public int indexOf(double value) {
        for (int i = 0, limit = length(); i < limit; i++) {
            if (java.lang.Double.compare(getInternal(i), value) == 0) {
                return i;
            }
        }
        return -1;
    }

    public int lastIndexOf(double value) {
        for (int i = length() - 1; i >= 0; i--) {
            if (java.lang.Double.compare(getInternal(i), value) == 0) {
                return i;
            }
        }
        return -1;
    }

    public Doubles slice(int offset) {
        return slice(offset, length() - offset);
    }

    public Doubles slice(int offset, int length) {
        Check.fromIndexSize(offset, length, length());
        return new Doubles(array, this.offset + offset * Double.BYTES, length * Double.BYTES);
    }

    public void copyTo(Mutable target, int offset) {
        Check.fromIndexSize(offset * Double.BYTES, length, target.length);
        System.arraycopy(array, this.offset, target.array, target.offset + offset * Double.BYTES, length);
    }

    @Override
    public DoubleBuffer asBuffer() {
        return asByteBuffer().asDoubleBuffer().slice().asReadOnlyBuffer();
    }

    public DoubleStream stream() {
        return IntStream.range(0, length()).mapToDouble(i -> getInternal(i));
    }

    public double[] toArray() {
        double[] result = new double[length()];
        asBuffer().get(result);
        return result;
    }

    @Override
    public int compareTo(Doubles o) {
        int min = Math.min(length(), o.length());
        for (int i = 0; i < min; i++) {
            int c = Double.compare(getInternal(i), o.getInternal(i));
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
        if (!(obj instanceof Doubles o)) {
            return false;
        }
        if (length() != o.length()) {
            return false;
        }
        for (int i = 0, len = length(); i < len; i++) {
            if (Double.compare(getInternal(i), o.getInternal(i)) != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;
        for (int i = 0, len = length(); i < len; i++) {
            result = 31 * result + Double.hashCode(getInternal(i));
        }
        return result;
    }

    @Override
    public String toString() {
        return "[" + length() + " doubles]";
    }

    public static final class Mutable extends Doubles {
        Mutable(byte[] array, int offset, int length) {
            super(array, offset, length);
        }

        public static Mutable wrap(double[] array) {
            return wrap(array, 0, array.length);
        }

        public static Mutable wrap(double[] array, int offset, int length) {
            byte[] buffer = new byte[length * Double.BYTES];
            ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asDoubleBuffer().put(array, offset, length);
            return new Mutable(buffer, 0, buffer.length);
        }

        public Mutable set(int index, double value) {
            Check.index(index, length());
            return setInternal(index, value);
        }

        public Mutable setInternal(int index, double value) {
            VH_DOUBLE.set(array, offset + index * Double.BYTES, value);
            return this;
        }

        public Mutable slice(int offset) {
            return slice(offset, length() - offset);
        }

        public Mutable slice(int offset, int length) {
            Check.fromIndexSize(offset, length, length());
            return new Mutable(array, this.offset + offset * Double.BYTES, length * Double.BYTES);
        }

        public Mutable copyFrom(double[] src) {
            return copyFrom(src, 0, src.length);
        }

        public Mutable copyFrom(double[] src, int offset, int length) {
            Check.fromIndexSize(offset, length, src.length);
            Check.fromIndexSize(0, length, length());
            asByteBuffer().asDoubleBuffer().put(src, offset, length);
            return this;
        }

        public Mutable copyWithin(int srcIndex, int dstIndex, int length) {
            copyWithinBytes(srcIndex * Double.BYTES, dstIndex * Double.BYTES, length * Double.BYTES);
            return this;
        }

        public Mutable fill(double value) {
            for (int i = 0; i < length(); i++) {
                setInternal(i, value);
            }
            return this;
        }

        public Mutable fillFrom(BinarySource source) throws IOException {
            source.readBytes(new Bytes.Mutable(array, offset, length));
            if (source.order() == ByteOrder.BIG_ENDIAN) {
                for (int i = 0, len = length(); i < len; i++) {
                    setInternal(i, Double.longBitsToDouble(Long.reverseBytes(Double.doubleToRawLongBits(getInternal(i)))));
                }
            }
            return this;
        }

        public DoubleBuffer asMutableBuffer() {
            return asByteBuffer().asDoubleBuffer().slice();
        }
    }
}
