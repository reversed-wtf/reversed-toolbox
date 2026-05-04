package wtf.reversed.toolbox.collect;

import java.io.IOException;
import java.lang.Comparable;
import java.lang.Object;
import java.lang.Override;
import java.lang.Short;
import java.lang.String;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.stream.IntStream;
import javax.annotation.processing.Generated;
import wtf.reversed.toolbox.io.BinarySource;
import wtf.reversed.toolbox.util.Check;

@Generated("wtf.reversed.toolbox.util.SliceGenerator")
public sealed class Shorts extends Slice implements Comparable<Shorts> {
    private static final Shorts EMPTY = new Shorts(new byte[0], 0, 0);

    Shorts(byte[] array, int offset, int length) {
        super(array, offset, length);
    }

    public static Shorts empty() {
        return EMPTY;
    }

    public static Shorts wrap(short[] array) {
        return wrap(array, 0, array.length);
    }

    public static Shorts wrap(short[] array, int offset, int length) {
        byte[] buffer = new byte[length * Short.BYTES];
        ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(array, offset, length);
        return new Shorts(buffer, 0, buffer.length);
    }

    public static Mutable allocate(int length) {
        int byteLength = Math.multiplyExact(length, Short.BYTES);
        return new Mutable(new byte[byteLength], 0, byteLength);
    }

    public static Shorts from(ShortBuffer buffer) {
        Check.argument(buffer.hasArray(), "buffer must be backed by an array");
        return wrap(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
    }

    public short get(int index) {
        Check.index(index, length);
        return getInternal(index);
    }

    short getInternal(int index) {
        return (short) VH_SHORT.get(array, offset + index * Short.BYTES);
    }

    public int getUnsigned(int offset) {
        return Short.toUnsignedInt(get(offset));
    }

    @Override
    public int length() {
        return length >>> 1;
    }

    public boolean contains(short value) {
        return indexOf(value) >= 0;
    }

    public int indexOf(short value) {
        for (int i = 0, limit = length(); i < limit; i++) {
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

    public Shorts slice(int offset) {
        return slice(offset, length() - offset);
    }

    public Shorts slice(int offset, int length) {
        Check.fromIndexSize(offset, length, length());
        return new Shorts(array, this.offset + offset * Short.BYTES, length * Short.BYTES);
    }

    public void copyTo(Mutable target, int offset) {
        Check.fromIndexSize(offset * Short.BYTES, length, target.length);
        System.arraycopy(array, this.offset, target.array, target.offset + offset * Short.BYTES, length);
    }

    @Override
    public ShortBuffer asBuffer() {
        return asByteBuffer().asShortBuffer().slice().asReadOnlyBuffer();
    }

    public IntStream stream() {
        return IntStream.range(0, length()).map(i -> getInternal(i));
    }

    public short[] toArray() {
        short[] result = new short[length()];
        asBuffer().get(result);
        return result;
    }

    @Override
    public int compareTo(Shorts o) {
        int min = Math.min(length(), o.length());
        for (int i = 0; i < min; i++) {
            int c = Short.compare(getInternal(i), o.getInternal(i));
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
        if (!(obj instanceof Shorts o)) {
            return false;
        }
        return Arrays.equals(array, offset, offset + length, o.array, o.offset, o.offset + o.length);
    }

    @Override
    public int hashCode() {
        int result = 1;
        for (int i = 0, len = length(); i < len; i++) {
            result = 31 * result + Short.hashCode(getInternal(i));
        }
        return result;
    }

    @Override
    public String toString() {
        return "[" + length() + " shorts]";
    }

    public static final class Mutable extends Shorts {
        Mutable(byte[] array, int offset, int length) {
            super(array, offset, length);
        }

        public static Mutable wrap(short[] array) {
            return wrap(array, 0, array.length);
        }

        public static Mutable wrap(short[] array, int offset, int length) {
            byte[] buffer = new byte[length * Short.BYTES];
            ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(array, offset, length);
            return new Mutable(buffer, 0, buffer.length);
        }

        public Mutable set(int index, short value) {
            Check.index(index, length());
            return setInternal(index, value);
        }

        public Mutable setInternal(int index, short value) {
            VH_SHORT.set(array, offset + index * Short.BYTES, value);
            return this;
        }

        public Mutable slice(int offset) {
            return slice(offset, length() - offset);
        }

        public Mutable slice(int offset, int length) {
            Check.fromIndexSize(offset, length, length());
            return new Mutable(array, this.offset + offset * Short.BYTES, length * Short.BYTES);
        }

        public Mutable copyFrom(short[] src) {
            return copyFrom(src, 0, src.length);
        }

        public Mutable copyFrom(short[] src, int offset, int length) {
            Check.fromIndexSize(offset, length, src.length);
            Check.fromIndexSize(0, length, length());
            asByteBuffer().asShortBuffer().put(src, offset, length);
            return this;
        }

        public Mutable copyWithin(int srcIndex, int dstIndex, int length) {
            copyWithinBytes(srcIndex * Short.BYTES, dstIndex * Short.BYTES, length * Short.BYTES);
            return this;
        }

        public Mutable fill(short value) {
            for (int i = 0; i < length(); i++) {
                setInternal(i, value);
            }
            return this;
        }

        public Mutable fillFrom(BinarySource source) throws IOException {
            source.readBytes(new Bytes.Mutable(array, offset, length));
            if (source.order() == ByteOrder.BIG_ENDIAN) {
                for (int i = 0, len = length(); i < len; i++) {
                    setInternal(i, Short.reverseBytes(getInternal(i)));
                }
            }
            return this;
        }

        public ShortBuffer asMutableBuffer() {
            return asByteBuffer().asShortBuffer().slice();
        }
    }
}
