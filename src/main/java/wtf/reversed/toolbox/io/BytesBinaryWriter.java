package wtf.reversed.toolbox.io;

import wtf.reversed.toolbox.collect.*;
import wtf.reversed.toolbox.util.*;

public final class BytesBinaryWriter extends BinaryWriter {
    private Bytes.Mutable buffer;
    private int position = 0;
    private int length = 0;

    public BytesBinaryWriter() {
        this(32);
    }

    public BytesBinaryWriter(int capacity) {
        Check.nonNegative(capacity, "capacity");
        buffer = Bytes.allocate(capacity);
    }

    @Override
    public void writeBytes(Bytes values) {
        if (values.length() == 0) {
            return;
        }
        reserve(values.length());
        values.copyTo(buffer, position);
        advance(values.length());
    }

    @Override
    public void writeByte(byte value) {
        reserve(Byte.BYTES);
        buffer.set(position, value);
        advance(Byte.BYTES);
    }

    @Override
    public void writeShort(short value) {
        reserve(Short.BYTES);
        buffer.setShort(position, bigEndian ? Short.reverseBytes(value) : value);
        advance(Short.BYTES);
    }

    @Override
    public void writeInt(int value) {
        reserve(Integer.BYTES);
        buffer.setInt(position, bigEndian ? Integer.reverseBytes(value) : value);
        advance(Integer.BYTES);
    }

    @Override
    public void writeLong(long value) {
        reserve(Long.BYTES);
        buffer.setLong(position, bigEndian ? Long.reverseBytes(value) : value);
        position += Long.BYTES;
    }

    @Override
    public long position() {
        return position;
    }

    @Override
    public BinaryWriter position(long pos) {
        Check.nonNegative(pos, "position");
        position = Math.toIntExact(pos);
        return this;
    }

    @Override
    public void flush() {
        // Do nothing
    }

    @Override
    public void close() {
        // Do nothing
    }

    /**
     * Returns an independent snapshot of the bytes written so far.
     */
    public Bytes toBytes() {
        var result = Bytes.allocate(length);
        buffer.slice(0, length).copyTo(result, 0);
        return result;
    }

    private void advance(int values) {
        position += values;
        length = Math.max(length, position);
    }

    private void reserve(int count) {
        if (buffer.length() - position < count) {
            int newCapacity = Math.max(buffer.length() * 2, position + count);
            var newBuffer = Bytes.allocate(newCapacity);
            buffer.slice(0, length).copyTo(newBuffer, 0);
            buffer = newBuffer;
        }
        length = Math.max(length, position + count);
    }
}
