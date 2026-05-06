package wtf.reversed.toolbox.collect;

import wtf.reversed.toolbox.io.*;
import wtf.reversed.toolbox.util.*;

import javax.annotation.processing.*;
import java.io.*;
import java.nio.*;
import java.util.*;
import java.util.stream.*;

@Generated("wtf.reversed.toolbox.util.SliceGenerator")
public sealed class Floats extends Slice implements Comparable<Floats> {
    private static final Floats EMPTY = new Floats(EMPTY_ARRAY, 0, 0);

    Floats(byte[] array, int offset, int length) {
        super(array, offset, length);
    }

    public static Floats empty() {
        return EMPTY;
    }

    public static Floats copyOf(float[] array) {
        return copyOf(array, 0, array.length);
    }

    public static Floats copyOf(float[] array, int offset, int length) {
        byte[] buffer = new byte[Math.multiplyExact(length, Float.BYTES)];
        ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().put(array, offset, length);
        return new Floats(buffer, 0, buffer.length);
    }

    public static Floats copyOf(FloatBuffer buffer) {
        Check.argument(buffer.hasArray(), "buffer must be backed by an array");
        return copyOf(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
    }

    public static Mutable allocate(int length) {
        int byteLength = Math.multiplyExact(length, Float.BYTES);
        return new Mutable(new byte[byteLength], 0, byteLength);
    }

    public float get(int index) {
        Check.index(index, length());
        return getInternal(index);
    }

    float getInternal(int index) {
        return (float) VH_FLOAT_LE.get(array, offset + index * Float.BYTES);
    }

    @Override
    public int length() {
        return length >>> 2;
    }

    public boolean contains(float value) {
        return indexOf(value) >= 0;
    }

    public int indexOf(float value) {
        for (int i = 0, len = length(); i < len; i++) {
            if (Float.floatToRawIntBits(getInternal(i)) == Float.floatToRawIntBits(value)) {
                return i;
            }
        }
        return -1;
    }

    public int lastIndexOf(float value) {
        for (int i = length() - 1; i >= 0; i--) {
            if (Float.floatToRawIntBits(getInternal(i)) == Float.floatToRawIntBits(value)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public FloatBuffer asBuffer() {
        return asTypedBuffer().slice().asReadOnlyBuffer();
    }

    public void copyTo(Mutable target, int offset) {
        Check.fromIndexSize(offset, length(), target.length());
        System.arraycopy(array, this.offset, target.array, target.offset + Math.multiplyExact(offset, Float.BYTES), length);
    }

    public void copyTo(float[] dst) {
        copyTo(dst, 0, length());
    }

    public void copyTo(float[] dst, int offset, int length) {
        Check.fromIndexSize(offset, length, dst.length);
        Check.fromIndexSize(0, length, length());
        asTypedBuffer().get(dst, offset, length);
    }

    public Floats slice(int offset) {
        return slice(offset, length() - offset);
    }

    public Floats slice(int offset, int length) {
        Check.fromIndexSize(offset, length, length());
        return new Floats(array, this.offset + Math.multiplyExact(offset, Float.BYTES), Math.multiplyExact(length, Float.BYTES));
    }

    public DoubleStream stream() {
        return IntStream.range(0, length()).mapToDouble(i -> getInternal(i));
    }

    public float[] toArray() {
        float[] result = new float[length()];
        copyTo(result);
        return result;
    }

    FloatBuffer asTypedBuffer() {
        return asByteBuffer().asFloatBuffer();
    }

    @Override
    public int compareTo(Floats o) {
        int prefix = Math.min(length, o.length);
        int mismatch = Arrays.mismatch(array, offset, offset + prefix, o.array, o.offset, o.offset + prefix);
        if (mismatch < 0) {
            return Integer.compare(length(), o.length());
        }
        int idx = mismatch >>> 2;
        return Integer.compare(Float.floatToRawIntBits(getInternal(idx)), Float.floatToRawIntBits(o.getInternal(idx)));
    }

    public static final class Mutable extends Floats {
        Mutable(byte[] array, int offset, int length) {
            super(array, offset, length);
        }

        public static Mutable copyOf(float[] array) {
            return copyOf(array, 0, array.length);
        }

        public static Mutable copyOf(float[] array, int offset, int length) {
            byte[] buffer = new byte[Math.multiplyExact(length, Float.BYTES)];
            ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().put(array, offset, length);
            return new Mutable(buffer, 0, buffer.length);
        }

        public Mutable set(int index, float value) {
            Check.index(index, length());
            return setInternal(index, value);
        }

        private Mutable setInternal(int index, float value) {
            VH_FLOAT_LE.set(array, offset + index * Float.BYTES, value);
            return this;
        }

        public FloatBuffer asMutableBuffer() {
            return asTypedBuffer().slice();
        }

        public Mutable slice(int offset) {
            return slice(offset, length() - offset);
        }

        public Mutable slice(int offset, int length) {
            Check.fromIndexSize(offset, length, length());
            return new Mutable(array, this.offset + Math.multiplyExact(offset, Float.BYTES), Math.multiplyExact(length, Float.BYTES));
        }

        public Mutable copyFrom(float[] src) {
            return copyFrom(src, 0, src.length);
        }

        public Mutable copyFrom(float[] src, int offset, int length) {
            Check.fromIndexSize(offset, length, src.length);
            Check.fromIndexSize(0, length, length());
            asTypedBuffer().put(src, offset, length);
            return this;
        }

        public Mutable fill(float value) {
            if (Float.floatToRawIntBits(value) == 0) {
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
                    setInternal(i, (float) VH_FLOAT_BE.get(array, offset + i * Float.BYTES));
                }
            }
            return this;
        }
    }
}
