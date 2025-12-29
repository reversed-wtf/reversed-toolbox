package wtf.reversed.toolbox.io;

import wtf.reversed.toolbox.collect.*;
import wtf.reversed.toolbox.util.*;

import java.io.*;
import java.nio.channels.*;

final class ChannelBinarySource extends BinarySource {
    private static final int BUFFER_SIZE = 0x2000;
    private final Bytes.Mutable buffer = Bytes.Mutable.allocate(BUFFER_SIZE);
    private final FileChannel channel;
    private long channelPosition = 0; // Always points to buffer[0]
    private int bufferPosition = 0; // Points to the next byte to read
    private int bufferLength = 0; // Number of bytes in the buffer

    ChannelBinarySource(SeekableByteChannel channel) throws IOException {
        super(channel.size());
        if (!(channel instanceof FileChannel fileChannel)) {
            throw new IllegalArgumentException("channel must be a FileChannel");
        }
        this.channel = fileChannel;
    }

    @Override
    public long position() {
        return channelPosition + bufferPosition;
    }

    @Override
    public BinarySource position(long position) {
        Check.position(position, size, "position");

        if (channelPosition <= position && position <= channelPosition + bufferLength) {
            // If we fit in the current buffer, just adjust the position
            bufferPosition = (int) (position - channelPosition);
        } else {
            // If not, move the channel position and mark the buffer empty
            channelPosition = position;
            bufferPosition = 0;
            bufferLength = 0;
        }
        return this;
    }

    @Override
    public void readBytes(Bytes.Mutable target) throws IOException {
        // If the buffer has enough data, just copy the data and return
        if (bufferRemaining() >= target.length()) {
            buffer.slice(bufferPosition, target.length()).copyTo(target, 0);
            bufferPosition += target.length();
            return;
        }

        // If there's remaining data in the buffer, copy it first
        int targetPosition = 0;
        if (bufferPosition < bufferLength) {
            int remaining = bufferRemaining();
            buffer.slice(bufferPosition, remaining).copyTo(target, targetPosition);
            targetPosition += remaining;

            // We drained the buffer, so update our channelPosition, and mark it empty
            channelPosition += bufferLength;
            bufferPosition = 0;
            bufferLength = 0;
        }

        // If the data we want to read fits in a single buffer, do a refill and copy
        int targetRemaining = target.length() - targetPosition;
        if (targetRemaining < BUFFER_SIZE) {
            refill(targetRemaining); // Make sure we have enough data in the buffer
            buffer.slice(bufferPosition, targetRemaining).copyTo(target, targetPosition);
            bufferPosition += targetRemaining;
            return;
        }

        // If not, do a straight read, buffer is emptied
        int read = channel.read(target.slice(targetPosition, targetRemaining).asMutableBuffer(), channelPosition);
        if (read != targetRemaining) {
            throw new EOFException("Unexpected end of stream, expected " + targetRemaining + " bytes, got " + read);
        }
        channelPosition += read;
    }

    @Override
    public byte readByte() throws IOException {
        refill(Byte.BYTES);
        byte result = buffer.get(bufferPosition);
        bufferPosition++;
        return result;
    }

    @Override
    public short readShort() throws IOException {
        refill(Short.BYTES);
        short result = buffer.getShort(bufferPosition);
        bufferPosition += Short.BYTES;
        return bigEndian ? Short.reverseBytes(result) : result;
    }

    @Override
    public int readInt() throws IOException {
        refill(Integer.BYTES);
        int result = buffer.getInt(bufferPosition);
        bufferPosition += Integer.BYTES;
        return bigEndian ? Integer.reverseBytes(result) : result;
    }

    @Override
    public long readLong() throws IOException {
        refill(Long.BYTES);
        long result = buffer.getLong(bufferPosition);
        bufferPosition += Long.BYTES;
        return bigEndian ? Long.reverseBytes(result) : result;
    }

    private void refill(int length) throws IOException {
        int remaining = bufferRemaining();
        if (remaining >= length) {
            return;
        }

        // First we have to move the leftover data to the front
        buffer.slice(bufferPosition, remaining).copyTo(buffer, 0);
        channelPosition += bufferPosition;
        bufferPosition = 0;
        bufferLength = remaining;

        // Then we can copy in new data from the channel
        Bytes.Mutable target = buffer.slice(remaining, BUFFER_SIZE - remaining);
        int read = channel.read(target.asMutableBuffer(), channelPosition + remaining);
        bufferLength += read;

        // Final check if we read enough data
        if (bufferRemaining() < length) {
            throw new EOFException("Expected at least " + length + " bytes, but only " + bufferRemaining() + " available");
        }
    }

    private int bufferRemaining() {
        return bufferLength - bufferPosition;
    }

    @Override
    public void close() throws IOException {
        channel.close();
        channelPosition = 0;
        bufferPosition = 0;
        bufferLength = 0;
    }
}
