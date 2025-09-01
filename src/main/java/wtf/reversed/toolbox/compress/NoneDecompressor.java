package wtf.reversed.toolbox.compress;

import wtf.reversed.toolbox.collect.*;

import java.io.*;

final class NoneDecompressor implements Decompressor {
    static final NoneDecompressor INSTANCE = new NoneDecompressor();

    private NoneDecompressor() {
    }

    @Override
    public void decompress(Bytes src, MutableBytes dst) throws IOException {
        if (src == dst) {
            return;
        }

        if (src.size() != dst.size()) {
            throw new IOException("src.size() (" + src.size() + ") and dst.size() (" + dst.size() + ") do not match");
        }

        src.copyTo(dst, 0);
    }
}
