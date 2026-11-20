package wtf.reversed.toolbox.io;

import wtf.reversed.toolbox.collect.*;
import wtf.reversed.toolbox.util.*;

import java.io.*;

abstract class BufferedBinaryWriter extends BinaryWriter {
    private static final int BUFFER_SIZE = 0x2000;

    private final Bytes.Mutable buffer = Bytes.allocate(BUFFER_SIZE);
    private long sourcePosition = 0; // Always points to buffer[0]
    private long position = 0; // Logical position; may be outside the buffered range
    private int bufferLength = 0; // Number of bytes in the buffer

    /**
     * Writes up to {@code source.length()} bytes at the given absolute position.
     *
     * @param source   the source of bytes to write
     * @param position the absolute position to write at
     * @return the number of bytes actually written, or {@code -1} if the end of the output is reached
     */
    abstract int writeImpl(Bytes source, long position) throws IOException;

    @Override
    public final long position() {
        return position;
    }

    @Override
    public final BinaryWriter position(long pos) {
        Check.nonNegative(pos, "position");
        position = pos;
        return this;
    }

    @Override
    public final void writeBytes(Bytes values) throws IOException {
        if (values.length() == 0) {
            return;
        }

        // If the buffer has enough space, just copy the data and return
        if (bufferRemaining() >= values.length()) {
            values.copyTo(buffer, bufferPosition());
            advance(values.length());
            bufferLength = Math.max(bufferLength, bufferPosition());
            return;
        }

        // Otherwise, write directly to the output
        flush();
        writeInternal(values, position);
        advance(values.length());
        sourcePosition = position;
    }

    @Override
    public final void writeByte(byte value) throws IOException {
        reserve(Byte.BYTES);
        buffer.set(bufferPosition(), value);
        advance(Byte.BYTES);
    }

    @Override
    public final void writeShort(short value) throws IOException {
        reserve(Short.BYTES);
        buffer.setShort(bufferPosition(), bigEndian ? Short.reverseBytes(value) : value);
        advance(Short.BYTES);
    }

    @Override
    public final void writeInt(int value) throws IOException {
        reserve(Integer.BYTES);
        buffer.setInt(bufferPosition(), bigEndian ? Integer.reverseBytes(value) : value);
        advance(Integer.BYTES);
    }

    @Override
    public final void writeLong(long value) throws IOException {
        reserve(Long.BYTES);
        buffer.setLong(bufferPosition(), bigEndian ? Long.reverseBytes(value) : value);
        advance(Long.BYTES);
    }

    private void reserve(int count) throws IOException {
        if (count > bufferRemaining()) {
            flush();
        }
        bufferLength = Math.max(bufferLength, bufferPosition() + count);
    }

    @Override
    public final void flush() throws IOException {
        writeInternal(buffer.slice(0, bufferLength), sourcePosition);

        sourcePosition = position;
        bufferLength = 0;
    }

    private void writeInternal(Bytes source, long position) throws IOException {
        int written = 0;
        while (written < source.length()) {
            int count = writeImpl(source.slice(written), position + written);
            if (count < 0) {
                throw new EOFException("Unexpected end of output");
            }
            if (count == 0 || count > source.length() - written) {
                throw new IOException("Invalid write progress: " + count);
            }
            written += count;
        }
    }

    private void advance(int bytes) {
        position += bytes;
    }

    private int bufferRemaining() {
        long offset = position - sourcePosition;
        // A seek must not expose a gap of stale buffer contents as pending data.
        if (offset < 0 || offset > bufferLength) {
            return 0;
        }
        return BUFFER_SIZE - (int) offset;
    }

    private int bufferPosition() {
        return (int) (position - sourcePosition);
    }

    void reset() {
        sourcePosition = 0;
        position = 0;
        bufferLength = 0;
    }
}
