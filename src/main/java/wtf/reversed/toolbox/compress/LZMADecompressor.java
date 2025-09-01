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
        int offset = 0;
        byte[] buffer = new byte[4096];
        try (var in = new LZMAInputStream(src.asInputStream())) {
            while (true) {
                int read = in.read(buffer);
                if (read <= 0) {
                    break;
                }

                Bytes.wrap(buffer, 0, read).copyTo(dst, offset);
                offset += read;
            }
        }
    }
}
