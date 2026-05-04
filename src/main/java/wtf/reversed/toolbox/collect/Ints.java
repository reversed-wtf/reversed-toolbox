package wtf.reversed.toolbox.collect;

import java.io.IOException;
import java.lang.Comparable;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.stream.IntStream;
import javax.annotation.processing.Generated;
import wtf.reversed.toolbox.io.BinarySource;
import wtf.reversed.toolbox.util.Check;

@Generated("wtf.reversed.toolbox.util.SliceGenerator")
public sealed class Ints extends Slice implements Comparable<Ints> {
    private static final Ints EMPTY = new Ints(new byte[0], 0, 0);

    Ints(byte[] array, int offset, int length) {
        super(array, offset, length);
    }

    public static Ints empty() {
        return EMPTY;
    }

    public static Ints wrap(int[] array) {
        return wrap(array, 0, array.length);
    }

    public static Ints wrap(int[] array, int offset, int length) {
        byte[] buffer = new byte[length * Integer.BYTES];
        ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().put(array, offset, length);
        return new Ints(buffer, 0, buffer.length);
    }

    public static Mutable allocate(int length) {
        int byteLength = Math.multiplyExact(length, Integer.BYTES);
        return new Mutable(new byte[byteLength], 0, byteLength);
    }

    public static Ints from(IntBuffer buffer) {
        Check.argument(buffer.hasArray(), "buffer must be backed by an array");
        return wrap(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
    }

    public int get(int index) {
        Check.index(index, length);
        return getInternal(index);
    }

    int getInternal(int index) {
        return (int) VH_INT.get(array, offset + index * Integer.BYTES);
    }

    public long getUnsigned(int offset) {
        return Integer.toUnsignedLong(get(offset));
    }

    @Override
    public int length() {
        return length >>> 2;
    }

    public boolean contains(int value) {
        return indexOf(value) >= 0;
    }

    public int indexOf(int value) {
        for (int i = 0, limit = length(); i < limit; i++) {
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

    public Ints slice(int offset) {
        return slice(offset, length() - offset);
    }

    public Ints slice(int offset, int length) {
        Check.fromIndexSize(offset, length, length());
        return new Ints(array, this.offset + offset * Integer.BYTES, length * Integer.BYTES);
    }

    public void copyTo(Mutable target, int offset) {
        Check.fromIndexSize(offset * Integer.BYTES, length, target.length);
        System.arraycopy(array, this.offset, target.array, target.offset + offset * Integer.BYTES, length);
    }

    @Override
    public IntBuffer asBuffer() {
        return asByteBuffer().asIntBuffer().slice().asReadOnlyBuffer();
    }

    public IntStream stream() {
        return IntStream.range(0, length()).map(i -> getInternal(i));
    }

    public int[] toArray() {
        int[] result = new int[length()];
        asBuffer().get(result);
        return result;
    }

    @Override
    public int compareTo(Ints o) {
        int min = Math.min(length(), o.length());
        for (int i = 0; i < min; i++) {
            int c = Integer.compare(getInternal(i), o.getInternal(i));
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
        if (!(obj instanceof Ints o)) {
            return false;
        }
        return Arrays.equals(array, offset, offset + length, o.array, o.offset, o.offset + o.length);
    }

    @Override
    public int hashCode() {
        int result = 1;
        for (int i = 0, len = length(); i < len; i++) {
            result = 31 * result + Integer.hashCode(getInternal(i));
        }
        return result;
    }

    @Override
    public String toString() {
        return "[" + length() + " ints]";
    }

    public static final class Mutable extends Ints {
        Mutable(byte[] array, int offset, int length) {
            super(array, offset, length);
        }

        public static Mutable wrap(int[] array) {
            return wrap(array, 0, array.length);
        }

        public static Mutable wrap(int[] array, int offset, int length) {
            byte[] buffer = new byte[length * Integer.BYTES];
            ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().put(array, offset, length);
            return new Mutable(buffer, 0, buffer.length);
        }

        public Mutable set(int index, int value) {
            Check.index(index, length());
            return setInternal(index, value);
        }

        public Mutable setInternal(int index, int value) {
            VH_INT.set(array, offset + index * Integer.BYTES, value);
            return this;
        }

        public Mutable slice(int offset) {
            return slice(offset, length() - offset);
        }

        public Mutable slice(int offset, int length) {
            Check.fromIndexSize(offset, length, length());
            return new Mutable(array, this.offset + offset * Integer.BYTES, length * Integer.BYTES);
        }

        public Mutable copyFrom(int[] src) {
            return copyFrom(src, 0, src.length);
        }

        public Mutable copyFrom(int[] src, int offset, int length) {
            Check.fromIndexSize(offset, length, src.length);
            Check.fromIndexSize(0, length, length());
            asByteBuffer().asIntBuffer().put(src, offset, length);
            return this;
        }

        public Mutable copyWithin(int srcIndex, int dstIndex, int length) {
            copyWithinBytes(srcIndex * Integer.BYTES, dstIndex * Integer.BYTES, length * Integer.BYTES);
            return this;
        }

        public Mutable fill(int value) {
            for (int i = 0; i < length(); i++) {
                setInternal(i, value);
            }
            return this;
        }

        public Mutable fillFrom(BinarySource source) throws IOException {
            source.readBytes(new Bytes.Mutable(array, offset, length));
            if (source.order() == ByteOrder.BIG_ENDIAN) {
                for (int i = 0, len = length(); i < len; i++) {
                    setInternal(i, Integer.reverseBytes(getInternal(i)));
                }
            }
            return this;
        }

        public IntBuffer asMutableBuffer() {
            return asByteBuffer().asIntBuffer().slice();
        }
    }
}
