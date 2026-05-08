package wtf.reversed.toolbox.compress;

import wtf.reversed.toolbox.collect.*;

final class NoneDecompressor implements Decompressor {
    static final NoneDecompressor INSTANCE = new NoneDecompressor();

    private NoneDecompressor() {
    }

    @Override
    public void decompress(Bytes src, Bytes.Mutable dst) {
        if (src == dst) {
            return;
        }

        if (src.length() != dst.length()) {
            throw new DecompressorException("src.length() (" + src.length() + ") and dst.length() (" + dst.length() + ") do not match");
        }

        src.copyTo(dst, 0);
    }

    @Override
    public Bytes decompress(Bytes src, int size) {
        if (src.length() != size) {
            throw new DecompressorException("src.length() (" + src.length() + ") and size (" + size + ") do not match");
        }
        return src;
    }
}
