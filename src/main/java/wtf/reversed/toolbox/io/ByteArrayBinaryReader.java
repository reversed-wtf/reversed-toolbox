package wtf.reversed.toolbox.io;

import wtf.reversed.toolbox.util.*;

import java.io.*;
import java.nio.*;

final class ByteArrayBinaryReader implements BinaryReader {
    private final byte[] array;
    private final int offset;
    private final int length;
    private int position;
    private ByteOrder order = ByteOrder.nativeOrder();

    ByteArrayBinaryReader(byte[] array, int offset, int length) {
        Check.fromIndexSize(offset, length, array.length);
        this.array = array;
        this.offset = offset;
        this.length = length;
    }

    @Override
    public byte readByte() {
        var value = array[offset + position];
        position++;
        return value;
    }

    @Override
    public void readBytes(byte[] dst, int off, int len) {
        Check.fromIndexSize(off, len, dst.length);
        System.arraycopy(array, offset + position, dst, off, len);
        position += len;
    }

    @Override
    public short readShort() {
        var value = Arrays.getShort(array, offset + position, order);
        position += Short.BYTES;
        return value;
    }

    @Override
    public int readInt() {
        var value = Arrays.getInt(array, offset + position, order);
        position += Integer.BYTES;
        return value;
    }

    @Override
    public long readLong() {
        var value = Arrays.getLong(array, offset + position, order);
        position += Long.BYTES;
        return value;
    }

    @Override
    public float readFloat() {
        var value = Arrays.getFloat(array, offset + position, order);
        position += Float.BYTES;
        return value;
    }

    @Override
    public double readDouble() {
        var value = Arrays.getDouble(array, offset + position, order);
        position += Double.BYTES;
        return value;
    }

    @Override
    public long size() {
        return length;
    }

    @Override
    public long position() {
        return position;
    }

    @Override
    public void position(long pos) throws EOFException {
        if (pos < 0) {
            throw new IllegalArgumentException("position is negative");
        }
        if (pos > length) {
            throw new EOFException("position is beyond the bounds");
        }
        this.position = Math.toIntExact(pos);
    }

    @Override
    public ByteOrder order() {
        return order;
    }

    @Override
    public BinaryReader order(ByteOrder order) {
        this.order = order;
        return this;
    }

    @Override
    public void close() {
        // nothing to close
    }

    @Override
    public String toString() {
        return "ByteArrayDataSource[position=" + position + ", size=" + length + "]";
    }
}
