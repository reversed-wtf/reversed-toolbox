package wtf.reversed.toolbox.collect;

import wtf.reversed.toolbox.util.*;
import wtf.reversed.toolbox.util.Arrays;

import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.util.*;

public class Bytes extends AbstractList<Byte> implements Comparable<Bytes>, RandomAccess {
    final byte[] array;

    final int fromIndex;

    final int toIndex;

    Bytes(byte[] array, int fromIndex, int toIndex) {
        Check.fromToIndex(fromIndex, toIndex, array.length);
        this.array = array;
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
    }

    public static Bytes wrap(byte[] array) {
        return new Bytes(array, 0, array.length);
    }

    public static Bytes wrap(byte[] array, int fromIndex, int toIndex) {
        return new Bytes(array, fromIndex, toIndex);
    }

    public static Bytes from(ByteBuffer buffer) {
        Check.argument(buffer.hasArray(), "buffer must be backed by an array");
        return new Bytes(buffer.array(), buffer.position(), buffer.limit());
    }

    public byte getByte(int index) {
        Check.index(index, size());
        return array[fromIndex + index];
    }

    public short getShort(int offset) {
        Check.fromIndexSize(offset, Short.BYTES, size());
        return Arrays.getShort(array, fromIndex + offset, ByteOrder.LITTLE_ENDIAN);
    }

    public int getInt(int offset) {
        Check.fromIndexSize(offset, Integer.BYTES, size());
        return Arrays.getInt(array, fromIndex + offset, ByteOrder.LITTLE_ENDIAN);
    }

    public long getLong(int offset) {
        Check.fromIndexSize(offset, Long.BYTES, size());
        return Arrays.getLong(array, fromIndex + offset, ByteOrder.LITTLE_ENDIAN);
    }

    public float getFloat(int offset) {
        Check.fromIndexSize(offset, Float.BYTES, size());
        return Arrays.getFloat(array, fromIndex + offset, ByteOrder.LITTLE_ENDIAN);
    }

    public double getDouble(int offset) {
        Check.fromIndexSize(offset, Double.BYTES, size());
        return Arrays.getDouble(array, fromIndex + offset, ByteOrder.LITTLE_ENDIAN);
    }

    public int getUnsignedByte(int offset) {
        return Byte.toUnsignedInt(getByte(offset));
    }

    public int getUnsignedShort(int offset) {
        return Short.toUnsignedInt(getShort(offset));
    }

    public long getUnsignedInt(int offset) {
        return Integer.toUnsignedLong(getInt(offset));
    }

    public InputStream asInputStream() {
        return new ByteArrayInputStream(array, fromIndex, size());
    }

    public String toString(Charset charset) {
        return new String(array, fromIndex, size(), charset);
    }

    public ByteBuffer asBuffer() {
        return ByteBuffer.wrap(array, fromIndex, size()).asReadOnlyBuffer();
    }

    public void copyTo(MutableBytes target, int offset) {
        System.arraycopy(array, fromIndex, target.array, target.fromIndex + offset, size());
    }

    public Bytes slice(int fromIndex) {
        return slice(fromIndex, size());
    }

    public Bytes slice(int fromIndex, int toIndex) {
        Check.fromToIndex(fromIndex, toIndex, size());
        return new Bytes(array, this.fromIndex + fromIndex, this.fromIndex + toIndex);
    }

    @Override
    public int size() {
        return toIndex - fromIndex;
    }

    @Override
    @Deprecated
    public Byte get(int index) {
        return getByte(index);
    }

    @Override
    public boolean contains(Object o) {
        return o instanceof java.lang.Byte value && ArrayUtils.contains(array, fromIndex, toIndex, value);
    }

    @Override
    public int indexOf(Object o) {
        if (o instanceof java.lang.Byte value) {
            int index = ArrayUtils.indexOf(array, fromIndex, toIndex, value);
            if (index >= 0) {
                return index - fromIndex;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        if (o instanceof java.lang.Byte value) {
            int index = ArrayUtils.lastIndexOf(array, fromIndex, toIndex, value);
            if (index >= 0) {
                return index - fromIndex;
            }
        }
        return -1;
    }

    @Override
    public Bytes subList(int fromIndex, int toIndex) {
        Check.fromToIndex(fromIndex, toIndex, size());
        return new Bytes(array, this.fromIndex + fromIndex, this.fromIndex + toIndex);
    }

    @Override
    public int compareTo(Bytes o) {
        return java.util.Arrays.compare(array, fromIndex, toIndex, o.array, o.fromIndex, o.toIndex);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Bytes o && java.util.Arrays.equals(array, fromIndex, toIndex, o.array, o.fromIndex, o.toIndex);
    }

    @Override
    public int hashCode() {
        return ArrayUtils.hashCode(array, fromIndex, toIndex);
    }

    @Override
    public String toString() {
        return ArrayUtils.toString(array, fromIndex, toIndex);
    }
}
