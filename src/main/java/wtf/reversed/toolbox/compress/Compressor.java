package wtf.reversed.toolbox.compress;

import wtf.reversed.toolbox.collect.*;

public interface Compressor {
    /**
     * Returns a compressor for the LZ4 block format (raw blocks without frame metadata).
     *
     * @return A shared LZ4 block compressor instance.
     */
    static Compressor lz4Block() {
        return LZ4BlockCompressor.INSTANCE;
    }

    int compress(Bytes src, Bytes.Mutable dst);

    default Bytes.Mutable compress(Bytes src) {
        var dst = Bytes.allocate(maxCompressedLength(src.length()));
        var length = compress(src, dst);
        return dst.slice(0, length);
    }

    /**
     * Returns the maximum compressed length for an input of size {@code length}.
     *
     * @param length the input size in bytes
     * @return the maximum compressed length in bytes
     */
    int maxCompressedLength(int length);
}
