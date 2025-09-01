package wtf.reversed.toolbox.collect;

import wtf.reversed.toolbox.util.*;

import java.nio.*;

public final class MutableBytes extends Bytes {
    private MutableBytes(byte[] array, int fromIndex, int toIndex) {
        super(array, fromIndex, toIndex);
    }

    public static MutableBytes wrap(byte[] array) {
        return new MutableBytes(array, 0, array.length);
    }

    public static MutableBytes wrap(byte[] array, int fromIndex, int toIndex) {
        return new MutableBytes(array, fromIndex, toIndex);
    }

    public static MutableBytes allocate(int size) {
        return new MutableBytes(new byte[size], 0, size);
    }

    public void setByte(int index, byte value) {
        Check.index(index, size());
        array[fromIndex + index] = value;
    }

    public MutableBytes setShort(int offset, short value) {
        Check.fromIndexSize(offset, Short.BYTES, size());
        Arrays.setShort(array, fromIndex + offset, value, ByteOrder.LITTLE_ENDIAN);
        return this;
    }

    public MutableBytes setInt(int offset, int value) {
        Check.fromIndexSize(offset, Integer.BYTES, size());
        Arrays.setInt(array, fromIndex + offset, value, ByteOrder.LITTLE_ENDIAN);
        return this;
    }

    public MutableBytes setLong(int offset, long value) {
        Check.fromIndexSize(offset, Long.BYTES, size());
        Arrays.setLong(array, fromIndex + offset, value, ByteOrder.LITTLE_ENDIAN);
        return this;
    }

    public MutableBytes setFloat(int offset, float value) {
        Check.fromIndexSize(offset, Float.BYTES, size());
        Arrays.setFloat(array, fromIndex + offset, value, ByteOrder.LITTLE_ENDIAN);
        return this;
    }

    public MutableBytes setDouble(int offset, double value) {
        Check.fromIndexSize(offset, Double.BYTES, size());
        Arrays.setDouble(array, fromIndex + offset, value, ByteOrder.LITTLE_ENDIAN);
        return this;
    }

    public ByteBuffer asMutableBuffer() {
        return ByteBuffer.wrap(array, fromIndex, size());
    }

    public void fill(int fromIndex, int toIndex, byte value) {
        Check.fromToIndex(fromIndex, toIndex, size());
        java.util.Arrays.fill(array, this.fromIndex + fromIndex, this.fromIndex + toIndex, value);
    }

    @Override
    public Byte set(int index, Byte element) {
        byte oldValue = getByte(index);
        setByte(index, element);
        return oldValue;
    }
}
