package wtf.reversed.toolbox.io;

import wtf.reversed.toolbox.util.*;

import java.io.*;

/**
 * Reads variable-width bit fields from a {@link BinarySource}. Supports both little and big endian bit sources.
 */
public final class BitSource implements Closeable {
    final BinarySource source;
    final boolean bigEndian;
    long bitBuf;
    int bitCount;

    private BitSource(BinarySource source, boolean bigEndian) {
        this.source = Check.nonNull(source, "source");
        this.bigEndian = bigEndian;
    }

    /**
     * Creates a big-endian bit source: bits within each byte are read MSB-first. For a byte {@code 0xAB},
     * {@code read(4)} yields {@code 0xA} then {@code 0xB}.
     */
    public static BitSource big(BinarySource source) {
        return new BitSource(source, true);
    }

    /**
     * Creates a little-endian bit source: bits within each byte are read LSB-first. For a byte {@code 0xAB},
     * {@code read(4)} yields {@code 0xB} then {@code 0xA}.
     */
    public static BitSource little(BinarySource source) {
        return new BitSource(source, false);
    }

    /**
     * Discards any unread bits within the current byte, advancing to the next byte boundary.
     */
    public void alignToByte() {
        consume(bitCount & 7);
    }

    /**
     * Returns the number of bits consumed from this source since it was created.
     * <p>
     * Unaffected by {@link #peek} and {@link #peekLong}.
     */
    public long bitsRead() {
        return source.position() * 8L - bitCount;
    }

    /**
     * Returns the number of bits still available to read.
     */
    public long bitsRemaining() {
        return bitCount + source.remaining() * 8L;
    }

    /**
     * Consumes and returns the next {@code count} bits as an unsigned value.
     *
     * @param count number of bits to read, in {@code [0, 31]}
     * @throws EOFException if fewer than {@code count} bits remain
     */
    public int read(int count) throws IOException {
        Check.argument(0 <= count && count <= 31, "count must be between 0 and 31 inclusive");
        return (int) readLong(count);
    }

    /**
     * Consumes and returns the next {@code count} bits as an unsigned long value.
     *
     * @param count number of bits to read, in {@code [0, 57]}
     * @throws EOFException if fewer than {@code count} bits remain
     */
    public long readLong(int count) throws IOException {
        Check.argument(0 <= count && count <= 57, "count must be between 0 and 57 inclusive");
        refillStrict(count);
        long result = peekBits(count);
        consume(count);
        return result;
    }

    /**
     * Consumes and returns the next single bit as {@code 0} or {@code 1}.
     *
     * @throws EOFException if no bits remain
     */
    public int readOne() throws IOException {
        return (int) readLong(1);
    }

    /**
     * Consumes and returns the next single bit as {@code true} or {@code false}.
     *
     * @throws EOFException if no bits remain
     */
    public boolean readFlag() throws IOException {
        return readOne() != 0;
    }

    /**
     * Returns the next {@code count} bits without advancing the cursor. Can peek past the end of the source.
     *
     * @param count number of bits to peek, in {@code [0, 31]}
     */
    public int peek(int count) throws IOException {
        Check.argument(0 <= count && count <= 31, "count must be between 0 and 31 inclusive");
        return (int) peekLong(count);
    }

    /**
     * Returns the next {@code count} bits without advancing the cursor. Can peek past the end of the source.
     *
     * @param count number of bits to peek, in {@code [0, 57]}
     */
    public long peekLong(int count) throws IOException {
        Check.argument(0 <= count && count <= 57, "count must be between 0 and 57 inclusive");
        refill(count);
        return peekBits(count);
    }

    /**
     * Discards the next {@code count} bits.
     * <p>
     * If you skip more than the remaining bits in the buffer, no exception will be thrown.
     *
     * @param count number of bits to skip, in {@code [0, 57]}
     */
    public void skip(int count) {
        Check.argument(0 <= count && count <= 57, "count must be between 0 and 57 inclusive");
        consume(count);
    }

    private long peekBits(int count) {
        if (count == 0) {
            return 0;
        }
        if (bigEndian) {
            return bitBuf >>> (64 - count);
        } else {
            return bitBuf & (-1L >>> (64 - count));
        }
    }

    private void consume(int count) {
        if (bigEndian) {
            bitBuf <<= count;
        } else {
            bitBuf >>>= count;
        }
        bitCount -= count;
    }

    private void refillStrict(int count) throws IOException {
        refill(count);
        if (bitCount < count) {
            throw new EOFException("Reached end of source while reading " + count + " bits");
        }
    }

    private void refill(int count) throws IOException {
        if (bitCount < count) {
            if (bitCount <= 56 && source.remaining() >= 8) {
                refillLong();
            }
            // Handle the end
            while (bitCount < count && source.remaining() > 0) {
                refillByte(Byte.toUnsignedLong(source.readByte()));
            }
        }
    }

    private void refillLong() throws IOException {
        // Read 8 bytes and flip if we need to
        long value = source.readLong();
        if (source.bigEndian ^ bigEndian) {
            value = Long.reverseBytes(value);
        }

        // Shift a multiple of bytes over for the new bits
        int shift = (bitCount + 7) & ~7;
        if (bigEndian) {
            bitBuf |= (value & (-1L << shift)) >>> bitCount;
        } else {
            bitBuf |= (value & (-1L >>> shift)) << bitCount;
        }

        // Adjust the position, in case we read too much
        int usedBytes = (64 - bitCount) >>> 3;
        source.position(source.position() - (8 - usedBytes));
        bitCount += usedBytes << 3;
    }

    private void refillByte(long value) {
        if (bigEndian) {
            bitBuf |= value << (56 - bitCount);
        } else {
            bitBuf |= value << bitCount;
        }
        bitCount += 8;
    }

    @Override
    public void close() throws IOException {
        source.close();
    }
}
