package wtf.reversed.toolbox.io;

import wtf.reversed.toolbox.util.*;

import java.io.*;
import java.nio.*;

abstract class BufferedBinaryReader implements BinaryReader {
    private static final int DEFAULT_BUFFER_SIZE = 0x4000;

    private final ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE).limit(0);
    private final long size;
    private long position;

    BufferedBinaryReader(long size) {
        this.size = size;
    }

    @Override
    public byte readByte() throws IOException {
        refill(Byte.BYTES);
        return buffer.get();
    }

    @Override
    public short readShort() throws IOException {
        refill(Short.BYTES);
        return buffer.getShort();
    }

    @Override
    public int readInt() throws IOException {
        refill(Integer.BYTES);
        return buffer.getInt();
    }

    @Override
    public long readLong() throws IOException {
        refill(Long.BYTES);
        return buffer.getLong();
    }

    @Override
    public float readFloat() throws IOException {
        refill(Float.BYTES);
        return buffer.getFloat();
    }

    @Override
    public double readDouble() throws IOException {
        refill(Double.BYTES);
        return buffer.getDouble();
    }

    @Override
    public void readBytes(byte[] dst, int off, int len) throws IOException {
        Check.fromIndexSize(off, len, dst.length);

        int remaining = buffer.remaining();

        // If we can read the entire array in one go, do so
        if (remaining >= len) {
            buffer.get(dst, off, len);
            return;
        }

        if (remaining > 0) {
            buffer.get(dst, off, remaining);
            off += remaining;
            len -= remaining;
        }

        // If we can fit the remaining bytes in the buffer, do a refill and read
        if (buffer.capacity() >= len) {
            refill();
            buffer.get(dst, off, len);
            return;
        }

        // If we can't fit the remaining bytes in the buffer, read directly into the destination
        long end = position + buffer.position() + len;
        if (end > size) {
            throw new EOFException();
        }

        readImpl(ByteBuffer.wrap(dst, off, len));
        position = end;
        buffer.limit(0);
    }


    @Override
    public final long size() {
        return size;
    }

    @Override
    public final long position() {
        return position + buffer.position();
    }

    @Override
    public final void position(long pos) throws IOException {
        if (pos < 0) {
            throw new IllegalArgumentException("position is negative");
        }
        if (pos > size) {
            throw new EOFException("position is beyond the bounds");
        }
        if (pos >= position && pos < position + buffer.limit()) {
            buffer.position(Math.toIntExact(pos - position));
        } else {
            position = pos;
            buffer.limit(0);
            positionImpl(pos);
        }
    }

    @Override
    public final ByteOrder order() {
        return buffer.order();
    }

    @Override
    public final BinaryReader order(ByteOrder order) {
        buffer.order(order);
        return this;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[position=" + position() + ", size=" + size() + "]";
    }

    /**
     * Optionally refills the buffer if it contains less than {@code count} bytes remaining.
     *
     * @param count number of bytes to refill
     * @throws IOException if an I/O error occurs
     */
    private void refill(int count) throws IOException {
        if (buffer.capacity() < count) {
            throw new IllegalArgumentException("Can't refill more bytes than the buffer can hold");
        }
        if (buffer.remaining() < count) {
            refill();
            if (buffer.remaining() < count) {
                throw new EOFException("Expected to read " + count + " bytes, but only " + buffer.remaining() + " bytes are available");
            }
        }
    }

    /**
     * Compacts the buffer and fills the remaining, updating the {@code position} accordingly.
     *
     * @throws IOException if an I/O error occurs
     */
    private void refill() throws IOException {
        long start = position();
        long end = Math.min(start + buffer.capacity(), size);

        position = start;
        buffer.compact();
        buffer.limit(Math.toIntExact(end - start));
        readImpl(buffer);
        buffer.flip();
    }

    /**
     * Fills the destination buffer until it is full.
     *
     * @param dst destination buffer
     * @throws IOException if an I/O error occurs
     */
    protected abstract void readImpl(ByteBuffer dst) throws IOException;

    /**
     * Adjusts the internal cursor to the given {@code position} for future
     * <i>read</i> operations via {@link #readImpl(ByteBuffer)}
     *
     * @param position new position
     * @throws IOException if an I/O error occurs
     */
    protected abstract void positionImpl(long position) throws IOException;
}
