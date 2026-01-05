package wtf.reversed.toolbox.compress;

import wtf.reversed.toolbox.collect.*;

import java.io.*;
import java.nio.file.*;

public sealed interface Decompressor
    permits DeflateDecompressor, LZ4FrameDecompressor, LZDecompressor, LZMADecompressor, NoneDecompressor, OodleDecompressor {

    static Decompressor deflate(boolean nowrap) {
        return new DeflateDecompressor(nowrap);
    }

    static Decompressor fastLZ() {
        return FastLZDecompressor.INSTANCE;
    }

    static Decompressor lz4Block() {
        return LZ4BlockDecompressor.INSTANCE;
    }

    static Decompressor lz4Frame() {
        return LZ4FrameDecompressor.INSTANCE;
    }

    static Decompressor lzma() {
        return LZMADecompressor.INSTANCE;
    }

    static Decompressor none() {
        return NoneDecompressor.INSTANCE;
    }

    static Decompressor oodle(Path path) {
        return new OodleDecompressor(path);
    }

    void decompress(Bytes src, Bytes.Mutable dst) throws IOException;

    default Bytes decompress(Bytes src, int size) throws IOException {
        var dst = Bytes.Mutable.allocate(size);
        decompress(src, dst);
        return dst;
    }

    default void decompress(byte[] src, int srcLen, byte[] dst, int dstLen) throws IOException {
        decompress(src, 0, srcLen, dst, 0, dstLen);
    }

    default void decompress(byte[] src, int srcOff, int srcLen, byte[] dst, int dstOff, int dstLen) throws IOException {
        decompress(Bytes.wrap(src, srcOff, srcLen), Bytes.Mutable.wrap(dst, dstOff, dstLen));
    }
}
