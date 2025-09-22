package wtf.reversed.toolbox.compress;

import org.tukaani.xz.*;
import wtf.reversed.toolbox.collect.*;

import java.io.*;

final class LZMADecompressor implements Decompressor {
    static final LZMADecompressor INSTANCE = new LZMADecompressor();

    private LZMADecompressor() {
    }

    @Override
    public void decompress(Bytes src, MutableBytes dst) throws IOException {
        try (var is = new LZMAInputStream(src.asInputStream())) {
            is.transferTo(dst.asOutputStream());
        }
    }
}
