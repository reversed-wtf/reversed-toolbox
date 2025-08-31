package wtf.reversed.toolbox.compression;

import java.io.*;
import java.nio.*;
import java.nio.file.*;

public abstract sealed class Decompressor implements Closeable
    permits DeflateDecompressor, LZ4Decompressor, LZMADecompressor, OodleDecompressor {

    public static Decompressor deflate() {
        return new DeflateDecompressor();
    }

    public static Decompressor lz4() {
        return new LZ4Decompressor();
    }

    public static Decompressor lzma() {
        return new LZMADecompressor();
    }

    public static Decompressor oodle(Path path) {
        return new OodleDecompressor(path);
    }

    public abstract void decompress(ByteBuffer src, ByteBuffer dst) throws IOException;

    public void decompress(byte[] src, int srcLen, byte[] dst, int dstLen) throws IOException {
        decompress(src, 0, srcLen, dst, 0, dstLen);
    }

    public void decompress(byte[] src, int srcOff, int srcLen, byte[] dst, int dstOff, int dstLen) throws IOException {
        decompress(ByteBuffer.wrap(src, srcOff, srcLen), ByteBuffer.wrap(dst, dstOff, dstLen));
    }

    @Override
    public void close() throws IOException {
        // does nothing by default
    }
}
