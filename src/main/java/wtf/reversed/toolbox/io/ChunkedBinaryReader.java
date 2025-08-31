package wtf.reversed.toolbox.io;

import wtf.reversed.toolbox.util.Arrays;

import java.io.*;
import java.nio.*;
import java.util.*;

/**
 * A reader for compressed data split into chunks.
 */
public abstract class ChunkedBinaryReader implements BinaryReader {
    public record Chunk(long offset, long compressedOffset, int size, int compressedSize) {
    }

    private final BinaryReader reader;
    private final NavigableMap<Long, Chunk> chunks = new TreeMap<>(Long::compareUnsigned);

    private final byte[] compressed;
    private final byte[] decompressed;
    private final byte[] scratch = new byte[8];

    private ByteOrder order = ByteOrder.nativeOrder();
    private Chunk chunk;
    private long position;

    protected ChunkedBinaryReader(BinaryReader reader, List<Chunk> chunks) {
        int maxCompressedChunkSize = 0;
        int maxDecompressedChunkSize = 0;

        for (Chunk chunk : chunks) {
            this.chunks.put(chunk.offset(), chunk);
            maxCompressedChunkSize = Math.max(maxCompressedChunkSize, chunk.compressedSize());
            maxDecompressedChunkSize = Math.max(maxDecompressedChunkSize, chunk.size());
        }

        this.reader = reader;
        this.compressed = new byte[maxCompressedChunkSize];
        this.decompressed = new byte[maxDecompressedChunkSize];
    }

    @Override
    public byte readByte() throws IOException {
        readBytes(scratch, 0, Byte.BYTES);
        return scratch[0];
    }

    @Override
    public short readShort() throws IOException {
        readBytes(scratch, 0, Short.BYTES);
        return Arrays.getShort(scratch, 0, order);
    }

    @Override
    public int readInt() throws IOException {
        readBytes(scratch, 0, Integer.BYTES);
        return Arrays.getInt(scratch, 0, order);
    }

    @Override
    public long readLong() throws IOException {
        readBytes(scratch, 0, Long.BYTES);
        return Arrays.getLong(scratch, 0, order);
    }

    @Override
    public float readFloat() throws IOException {
        readBytes(scratch, 0, Float.BYTES);
        return Arrays.getFloat(scratch, 0, order);
    }

    @Override
    public double readDouble() throws IOException {
        readBytes(scratch, 0, Double.BYTES);
        return Arrays.getDouble(scratch, 0, order);
    }

    @Override
    public void readBytes(byte[] dst, int off, int len) throws IOException {
        while (len > 0) {
            Chunk chunk = chunks.floorEntry(position).getValue();
            int offset = Math.toIntExact(position - chunk.offset());
            int length = Math.min(chunk.size() - offset, len);

            if (length == 0) {
                throw new EOFException();
            }

            if (this.chunk != chunk) {
                this.chunk = chunk;

                reader.position(chunk.compressedOffset());
                reader.readBytes(compressed, 0, chunk.compressedSize());
                decompress(compressed, chunk.compressedSize(), decompressed, chunk.size());
            }

            System.arraycopy(decompressed, offset, dst, off, length);
            position += length;
            off += length;
            len -= length;
        }
    }

    @Override
    public long position() {
        return position;
    }

    @Override
    public void position(long pos) throws IOException {
        if (pos < 0) {
            throw new IllegalArgumentException("position is negative");
        }
        if (pos > size()) {
            throw new EOFException("position is beyond the bounds");
        }
        position = pos;
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
    public void close() throws IOException {
        reader.close();
    }

    protected abstract void decompress(byte[] src, int srcLen, byte[] dst, int dstLen) throws IOException;
}
