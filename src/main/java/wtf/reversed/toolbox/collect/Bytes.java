package wtf.reversed.toolbox.collect;

import wtf.reversed.toolbox.io.*;
import wtf.reversed.toolbox.util.*;

import javax.annotation.processing.*;
import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.util.*;
import java.util.stream.*;

@Generated("wtf.reversed.toolbox.util.SliceGenerator")
public sealed class Bytes extends Slice implements Comparable<Bytes> {
    private static final Bytes EMPTY = new Bytes(EMPTY_ARRAY, 0, 0);

    Bytes(byte[] array, int offset, int length) {
        super(array, offset, length);
    }

    public static Bytes empty() {
        return EMPTY;
    }

    public static Bytes wrap(byte[] array) {
        return wrap(array, 0, array.length);
    }

    public static Bytes wrap(byte[] array, int offset, int length) {
        return new Bytes(array, offset, length);
    }

    public static Mutable allocate(int length) {
        int byteLength = length;
        return new Mutable(new byte[byteLength], 0, byteLength);
    }

    public static Bytes from(ByteBuffer buffer) {
        Check.argument(buffer.hasArray(), "buffer must be backed by an array");
        return wrap(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
    }

    public byte get(int index) {
        Check.index(index, this.length);
        return getInternal(index);
    }

    byte getInternal(int index) {
        return array[offset + index];
    }

    public short getShort(int offset) {
        Check.fromIndexSize(offset, Short.BYTES, length);
        return (short) VH_SHORT.get(array, this.offset + offset);
    }

    public int getInt(int offset) {
        Check.fromIndexSize(offset, Integer.BYTES, length);
        return (int) VH_INT.get(array, this.offset + offset);
    }

    public long getLong(int offset) {
        Check.fromIndexSize(offset, Long.BYTES, length);
        return (long) VH_LONG.get(array, this.offset + offset);
    }

    public float getFloat(int offset) {
        Check.fromIndexSize(offset, Float.BYTES, length);
        return (float) VH_FLOAT.get(array, this.offset + offset);
    }

    public double getDouble(int offset) {
        Check.fromIndexSize(offset, Double.BYTES, length);
        return (double) VH_DOUBLE.get(array, this.offset + offset);
    }

    public int getUnsigned(int offset) {
        return Byte.toUnsignedInt(get(offset));
    }

    public int getUnsignedShort(int offset) {
        return Short.toUnsignedInt(getShort(offset));
    }

    public long getUnsignedInt(int offset) {
        return Integer.toUnsignedLong(getInt(offset));
    }

    @Override
    public int length() {
        return length;
    }

    public boolean contains(byte value) {
        return indexOf(value) >= 0;
    }

    public int indexOf(byte value) {
        for (int i = 0, limit = this.length; i < limit; i++) {
            if (getInternal(i) == value) {
                return i;
            }
        }
        return -1;
    }

    public int lastIndexOf(byte value) {
        for (int i = this.length - 1; i >= 0; i--) {
            if (getInternal(i) == value) {
                return i;
            }
        }
        return -1;
    }

    public Bytes slice(int offset) {
        return slice(offset, this.length - offset);
    }

    public Bytes slice(int offset, int length) {
        Check.fromIndexSize(offset, length, this.length);
        return new Bytes(array, this.offset + offset, length);
    }

    public void copyTo(Mutable target, int offset) {
        Check.fromIndexSize(offset, length(), target.length());
        System.arraycopy(array, this.offset, target.array, target.offset + offset, length);
    }

    @Override
    public ByteBuffer asBuffer() {
        return asByteBuffer().slice().asReadOnlyBuffer();
    }

    public IntStream stream() {
        return IntStream.range(0, this.length).map(i -> getInternal(i));
    }

    public byte[] toArray() {
        return Arrays.copyOfRange(array, offset, offset + length);
    }

    @Override
    public Bytes asBytes() {
        return this;
    }

    public InputStream asInputStream() {
        return new ByteArrayInputStream(array, offset, length);
    }

    public String toHexString(HexFormat format) {
        return format.formatHex(array, offset, offset + length);
    }

    public String toString(Charset charset) {
        return new String(array, offset, length, charset);
    }

    @Override
    public int compareTo(Bytes o) {
        return Arrays.compare(array, offset, offset + length, o.array, o.offset, o.offset + o.length);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Bytes o)) {
            return false;
        }
        return Arrays.equals(array, offset, offset + length, o.array, o.offset, o.offset + o.length);
    }

    @Override
    public int hashCode() {
        int result = 1;
        for (int i = 0, len = length(); i < len; i++) {
            result = 31 * result + Byte.hashCode(getInternal(i));
        }
        return result;
    }

    @Override
    public String toString() {
        return "[" + this.length + " bytes]";
    }

    public static final class Mutable extends Bytes {
        Mutable(byte[] array, int offset, int length) {
            super(array, offset, length);
        }

        public static Mutable wrap(byte[] array) {
            return wrap(array, 0, array.length);
        }

        public static Mutable wrap(byte[] array, int offset, int length) {
            return new Mutable(array, offset, length);
        }

        public Mutable set(int index, byte value) {
            Check.index(index, this.length);
            return setInternal(index, value);
        }

        private Mutable setInternal(int index, byte value) {
            array[offset + index] = value;
            return this;
        }

        public Mutable setShort(int offset, short value) {
            Check.fromIndexSize(offset, Short.BYTES, this.length);
            VH_SHORT.set(array, this.offset + offset, value);
            return this;
        }

        public Mutable setInt(int offset, int value) {
            Check.fromIndexSize(offset, Integer.BYTES, this.length);
            VH_INT.set(array, this.offset + offset, value);
            return this;
        }

        public Mutable setLong(int offset, long value) {
            Check.fromIndexSize(offset, Long.BYTES, this.length);
            VH_LONG.set(array, this.offset + offset, value);
            return this;
        }

        public Mutable setFloat(int offset, float value) {
            Check.fromIndexSize(offset, Float.BYTES, this.length);
            VH_FLOAT.set(array, this.offset + offset, value);
            return this;
        }

        public Mutable setDouble(int offset, double value) {
            Check.fromIndexSize(offset, Double.BYTES, this.length);
            VH_DOUBLE.set(array, this.offset + offset, value);
            return this;
        }

        public Mutable slice(int offset) {
            return slice(offset, this.length - offset);
        }

        public Mutable slice(int offset, int length) {
            Check.fromIndexSize(offset, length, this.length);
            return new Mutable(array, this.offset + offset, length);
        }

        public Mutable copyFrom(byte[] src) {
            return copyFrom(src, 0, src.length);
        }

        public Mutable copyFrom(byte[] src, int offset, int length) {
            Check.fromIndexSize(offset, length, src.length);
            Check.fromIndexSize(0, length, this.length);
            System.arraycopy(src, offset, array, this.offset, length);
            return this;
        }

        public Mutable copyWithin(int srcIndex, int dstIndex, int length) {
            copyWithinBytes(srcIndex, dstIndex, length);
            return this;
        }

        public Mutable fill(byte value) {
            Arrays.fill(array, offset, offset + length, value);
            return this;
        }

        public Mutable fillFrom(BinarySource source) throws IOException {
            source.readBytes(this);
            return this;
        }

        public ByteBuffer asMutableBuffer() {
            return asByteBuffer().slice();
        }
    }
}
