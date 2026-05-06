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

    public static Bytes wrap(ByteBuffer buffer) {
        Check.argument(buffer.hasArray(), "buffer must be backed by an array");
        return wrap(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
    }

    public static Mutable allocate(int length) {
        int byteLength = length;
        return new Mutable(new byte[byteLength], 0, byteLength);
    }

    public byte get(int index) {
        Check.index(index, this.length);
        return getInternal(index);
    }

    public short getShort(int offset) {
        Check.fromIndexSize(offset, Short.BYTES, length);
        return (short) VH_SHORT_LE.get(array, this.offset + offset);
    }

    public int getInt(int offset) {
        Check.fromIndexSize(offset, Integer.BYTES, length);
        return (int) VH_INT_LE.get(array, this.offset + offset);
    }

    public long getLong(int offset) {
        Check.fromIndexSize(offset, Long.BYTES, length);
        return (long) VH_LONG_LE.get(array, this.offset + offset);
    }

    public float getFloat(int offset) {
        Check.fromIndexSize(offset, Float.BYTES, length);
        return (float) VH_FLOAT_LE.get(array, this.offset + offset);
    }

    public double getDouble(int offset) {
        Check.fromIndexSize(offset, Double.BYTES, length);
        return (double) VH_DOUBLE_LE.get(array, this.offset + offset);
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

    byte getInternal(int index) {
        return array[offset + index];
    }

    @Override
    public int length() {
        return length;
    }

    public boolean contains(byte value) {
        return indexOf(value) >= 0;
    }

    public int indexOf(byte value) {
        for (int i = 0, len = this.length; i < len; i++) {
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

    @Override
    public ByteBuffer asBuffer() {
        return asByteBuffer().slice().asReadOnlyBuffer();
    }

    @Override
    public Bytes asBytes() {
        return this;
    }

    public Shorts asShorts() {
        return new Shorts(array, offset, length);
    }

    public Ints asInts() {
        return new Ints(array, offset, length);
    }

    public Longs asLongs() {
        return new Longs(array, offset, length);
    }

    public Floats asFloats() {
        return new Floats(array, offset, length);
    }

    public Doubles asDoubles() {
        return new Doubles(array, offset, length);
    }

    public InputStream asInputStream() {
        return new ByteArrayInputStream(array, offset, length);
    }

    public void copyTo(Mutable target, int offset) {
        Check.fromIndexSize(offset, length(), target.length());
        System.arraycopy(array, this.offset, target.array, target.offset + offset, length);
    }

    public void copyTo(byte[] target) {
        copyTo(target, 0, this.length);
    }

    public void copyTo(byte[] target, int offset, int length) {
        Check.fromIndexSize(offset, length, target.length);
        Check.fromIndexSize(0, length, this.length);
        System.arraycopy(array, this.offset, target, offset, length);
    }

    public Bytes slice(int offset) {
        return slice(offset, this.length - offset);
    }

    public Bytes slice(int offset, int length) {
        Check.fromIndexSize(offset, length, this.length);
        return new Bytes(array, this.offset + offset, length);
    }

    public IntStream stream() {
        return IntStream.range(0, this.length).map(i -> getInternal(i));
    }

    public byte[] toArray() {
        byte[] result = new byte[length()];
        copyTo(result);
        return result;
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

        public Mutable setShort(int offset, short value) {
            Check.fromIndexSize(offset, Short.BYTES, this.length);
            VH_SHORT_LE.set(array, this.offset + offset, value);
            return this;
        }

        public Mutable setInt(int offset, int value) {
            Check.fromIndexSize(offset, Integer.BYTES, this.length);
            VH_INT_LE.set(array, this.offset + offset, value);
            return this;
        }

        public Mutable setLong(int offset, long value) {
            Check.fromIndexSize(offset, Long.BYTES, this.length);
            VH_LONG_LE.set(array, this.offset + offset, value);
            return this;
        }

        public Mutable setFloat(int offset, float value) {
            Check.fromIndexSize(offset, Float.BYTES, this.length);
            VH_FLOAT_LE.set(array, this.offset + offset, value);
            return this;
        }

        public Mutable setDouble(int offset, double value) {
            Check.fromIndexSize(offset, Double.BYTES, this.length);
            VH_DOUBLE_LE.set(array, this.offset + offset, value);
            return this;
        }

        private Mutable setInternal(int index, byte value) {
            array[offset + index] = value;
            return this;
        }

        public ByteBuffer asMutableBuffer() {
            return asByteBuffer().slice();
        }

        public Mutable slice(int offset) {
            return slice(offset, this.length - offset);
        }

        public Mutable slice(int offset, int length) {
            Check.fromIndexSize(offset, length, this.length);
            return new Mutable(array, this.offset + offset, length);
        }

        public Mutable copyFrom(byte[] source) {
            return copyFrom(source, 0, source.length);
        }

        public Mutable copyFrom(byte[] source, int offset, int length) {
            Check.fromIndexSize(offset, length, source.length);
            Check.fromIndexSize(0, length, this.length);
            System.arraycopy(source, offset, array, this.offset, length);
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
    }
}
