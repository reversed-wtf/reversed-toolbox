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
        var baos = new ByteArrayOutputStream(dst.length());
        try (var is = new LZMAInputStream(src.asInputStream())) {
            is.transferTo(baos);
        }
        if (baos.size() != dst.length()) {
            throw new IOException("Read " + baos.size() + " bytes but expected " + dst.length() + " bytes");
        }

        // TODO: Can we avoid this extra copy?
        Bytes.wrap(baos.toByteArray()).copyTo(dst, 0);
    }
}
