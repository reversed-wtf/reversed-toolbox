package wtf.reversed.toolbox.compress;

import org.tukaani.xz.*;
import wtf.reversed.toolbox.collect.*;

import java.io.*;

final class LZMADecompressor implements Decompressor {
    static final LZMADecompressor INSTANCE = new LZMADecompressor();

    private LZMADecompressor() {
    }

    @Override
    public void decompress(Bytes src, Bytes.Mutable dst) {
        try (var in = new LZMAInputStream(src.asInputStream())) {
            dst.fillFrom(in);
            if (in.read() != -1) {
                throw new DecompressorException("LZMA stream did not end with EOF");
            }
        } catch (IOException e) {
            throw new DecompressorException("Failed to decompress LZMA data", e);
        }
    }
}
