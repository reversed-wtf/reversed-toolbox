package wtf.reversed.toolbox.compress;

import wtf.reversed.toolbox.collect.*;

import java.nio.file.*;

/**
 * A sealed interface representing a one-shot block decompressor for a specific compression algorithm. Instances are
 * obtained through the static factory methods on this interface and may be reused across calls.
 * <p>
 * Implementations decompress a complete compressed payload into a caller-supplied destination buffer. The destination
 * must be sized to the exact uncompressed length; any mismatch or malformed input raises a
 * {@link DecompressorException}.
 */
public interface Decompressor {
    /**
     * Returns a decompressor for the DEFLATE algorithm, backed by {@link java.util.zip.Inflater}.
     *
     * @param nowrap if {@code true}, decompresses raw DEFLATE without the surrounding zlib header and checksum.
     * @return A new DEFLATE decompressor.
     */
    static Decompressor deflate(boolean nowrap) {
        return new DeflateDecompressor(nowrap);
    }

    /**
     * Returns a decompressor for the FastLZ algorithm.
     *
     * @return A shared FastLZ decompressor instance.
     */
    static Decompressor fastLZ() {
        return FastLZDecompressor.INSTANCE;
    }

    /**
     * Returns a decompressor for the LZ4 block format (raw blocks without frame metadata).
     *
     * @return A shared LZ4 block decompressor instance.
     */
    static Decompressor lz4Block() {
        return LZ4BlockDecompressor.INSTANCE;
    }

    /**
     * Returns a decompressor for the LZ4 frame format (with magic, flags, and optional checksums).
     *
     * @return A shared LZ4 frame decompressor instance.
     */
    static Decompressor lz4Frame() {
        return LZ4FrameDecompressor.INSTANCE;
    }

    /**
     * Returns a decompressor for the LZMA algorithm.
     *
     * @return A shared LZMA decompressor instance.
     */
    static Decompressor lzma() {
        return LZMADecompressor.INSTANCE;
    }

    /**
     * Returns a pass-through decompressor that copies {@code src} into {@code dst} unchanged. Both buffers must have
     * the same length.
     *
     * @return A shared no-op decompressor instance.
     */
    static Decompressor none() {
        return NoneDecompressor.INSTANCE;
    }

    /**
     * Returns a decompressor for the Oodle algorithm, backed by the native Oodle library at the given path. The library
     * is loaded through Java's foreign function interface and is required at runtime.
     *
     * @param path Path to the Oodle shared library to load.
     * @return A new Oodle decompressor bound to the specified library.
     */
    static Decompressor oodle(Path path) {
        return new OodleDecompressor(path);
    }

    /**
     * Decompresses {@code src} into {@code dst}. The destination must be sized to the exact uncompressed length.
     *
     * @param src The compressed input.
     * @param dst The destination buffer to write the uncompressed output into.
     * @throws DecompressorException If the input is malformed or the output size does not match {@code dst.length()}.
     */
    void decompress(Bytes src, Bytes.Mutable dst);

    /**
     * Decompresses {@code src} into a freshly allocated buffer of the given uncompressed size.
     *
     * @param src  The compressed input.
     * @param size The exact uncompressed length, in bytes.
     * @return A new buffer containing the uncompressed output.
     * @throws DecompressorException If the input is malformed or its uncompressed size does not equal {@code size}.
     */
    default Bytes decompress(Bytes src, int size) {
        var dst = Bytes.allocate(size);
        decompress(src, dst);
        return dst;
    }
}
