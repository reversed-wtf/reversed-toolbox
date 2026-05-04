package wtf.reversed.toolbox.collect;

import java.io.IOException;
import java.lang.Comparable;
import java.lang.Float;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import javax.annotation.processing.Generated;
import wtf.reversed.toolbox.io.BinarySource;
import wtf.reversed.toolbox.util.Check;

@Generated("wtf.reversed.toolbox.util.SliceGenerator")
public sealed class Floats extends Slice implements Comparable<Floats> {
    private static final Floats EMPTY = new Floats(new byte[0], 0, 0);

    Floats(byte[] array, int offset, int length) {
        super(array, offset, length);
    }

    public static Floats empty() {
        return EMPTY;
    }

    public static Floats wrap(float[] array) {
        return wrap(array, 0, array.length);
    }

    public static Floats wrap(float[] array, int offset, int length) {
        byte[] buffer = new byte[length * Float.BYTES];
        ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().put(array, offset, length);
        return new Floats(buffer, 0, buffer.length);
    }

    public static Mutable allocate(int length) {
        int byteLength = Math.multiplyExact(length, Float.BYTES);
        return new Mutable(new byte[byteLength], 0, byteLength);
    }

    public static Floats from(FloatBuffer buffer) {
        Check.argument(buffer.hasArray(), "buffer must be backed by an array");
        return wrap(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
    }

    public float get(int index) {
        Check.index(index, length);
        return getInternal(index);
    }

    float getInternal(int index) {
        return (float) VH_FLOAT.get(array, offset + index * Float.BYTES);
    }

    @Override
    public int length() {
        return length >>> 2;
    }

    public boolean contains(float value) {
        return indexOf(value) >= 0;
    }

    public int indexOf(float value) {
        for (int i = 0, limit = length(); i < limit; i++) {
            if (java.lang.Float.compare(getInternal(i), value) == 0) {
                return i;
            }
        }
        return -1;
    }

    public int lastIndexOf(float value) {
        for (int i = length() - 1; i >= 0; i--) {
            if (java.lang.Float.compare(getInternal(i), value) == 0) {
                return i;
            }
        }
        return -1;
    }

    public Floats slice(int offset) {
        return slice(offset, length() - offset);
    }

    public Floats slice(int offset, int length) {
        Check.fromIndexSize(offset, length, length());
        return new Floats(array, this.offset + offset * Float.BYTES, length * Float.BYTES);
    }

    public void copyTo(Mutable target, int offset) {
        Check.fromIndexSize(offset * Float.BYTES, length, target.length);
        System.arraycopy(array, this.offset, target.array, target.offset + offset * Float.BYTES, length);
    }

    @Override
    public FloatBuffer asBuffer() {
        return asByteBuffer().asFloatBuffer().slice().asReadOnlyBuffer();
    }

    public DoubleStream stream() {
        return IntStream.range(0, length()).mapToDouble(i -> getInternal(i));
    }

    public float[] toArray() {
        float[] result = new float[length()];
        asBuffer().get(result);
        return result;
    }

    @Override
    public int compareTo(Floats o) {
        int min = Math.min(length(), o.length());
        for (int i = 0; i < min; i++) {
            int c = Float.compare(getInternal(i), o.getInternal(i));
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
        if (!(obj instanceof Floats o)) {
            return false;
        }
        if (length() != o.length()) {
            return false;
        }
        for (int i = 0, len = length(); i < len; i++) {
            if (Float.compare(getInternal(i), o.getInternal(i)) != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;
        for (int i = 0, len = length(); i < len; i++) {
            result = 31 * result + Float.hashCode(getInternal(i));
        }
        return result;
    }

    @Override
    public String toString() {
        return "[" + length() + " floats]";
    }

    public static final class Mutable extends Floats {
        Mutable(byte[] array, int offset, int length) {
            super(array, offset, length);
        }

        public static Mutable wrap(float[] array) {
            return wrap(array, 0, array.length);
        }

        public static Mutable wrap(float[] array, int offset, int length) {
            byte[] buffer = new byte[length * Float.BYTES];
            ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().put(array, offset, length);
            return new Mutable(buffer, 0, buffer.length);
        }

        public Mutable set(int index, float value) {
            Check.index(index, length());
            return setInternal(index, value);
        }

        public Mutable setInternal(int index, float value) {
            VH_FLOAT.set(array, offset + index * Float.BYTES, value);
            return this;
        }

        public Mutable slice(int offset) {
            return slice(offset, length() - offset);
        }

        public Mutable slice(int offset, int length) {
            Check.fromIndexSize(offset, length, length());
            return new Mutable(array, this.offset + offset * Float.BYTES, length * Float.BYTES);
        }

        public Mutable copyFrom(float[] src) {
            return copyFrom(src, 0, src.length);
        }

        public Mutable copyFrom(float[] src, int offset, int length) {
            Check.fromIndexSize(offset, length, src.length);
            Check.fromIndexSize(0, length, length());
            asByteBuffer().asFloatBuffer().put(src, offset, length);
            return this;
        }

        public Mutable copyWithin(int srcIndex, int dstIndex, int length) {
            copyWithinBytes(srcIndex * Float.BYTES, dstIndex * Float.BYTES, length * Float.BYTES);
            return this;
        }

        public Mutable fill(float value) {
            for (int i = 0; i < length(); i++) {
                setInternal(i, value);
            }
            return this;
        }

        public Mutable fillFrom(BinarySource source) throws IOException {
            source.readBytes(new Bytes.Mutable(array, offset, length));
            if (source.order() == ByteOrder.BIG_ENDIAN) {
                for (int i = 0, len = length(); i < len; i++) {
                    setInternal(i, Float.intBitsToFloat(Integer.reverseBytes(Float.floatToRawIntBits(getInternal(i)))));
                }
            }
            return this;
        }

        public FloatBuffer asMutableBuffer() {
            return asByteBuffer().asFloatBuffer().slice();
        }
    }
}
