package wtf.reversed.toolbox.compress;

import net.jpountz.lz4.*;
import wtf.reversed.toolbox.collect.*;

public final class LZ4BlockCompressor implements Compressor {
    static final LZ4BlockCompressor INSTANCE = new LZ4BlockCompressor();

    private final LZ4Compressor compressor = LZ4Factory.fastestInstance().fastCompressor();

    private LZ4BlockCompressor() {
    }

    @Override
    public int compress(Bytes src, Bytes.Mutable dst) {
        var srcBuf = src.asBuffer();
        var dstBuf = dst.asMutableBuffer();
        try {
            compressor.compress(srcBuf, dstBuf);
        } catch (LZ4Exception e) {
            throw new CompressorException("dst buffer is too small");
        }
        return dstBuf.position();
    }

    @Override
    public int maxCompressedLength(int length) {
        return compressor.maxCompressedLength(length);
    }
}
