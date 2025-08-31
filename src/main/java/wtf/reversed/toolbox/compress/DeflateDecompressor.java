package wtf.reversed.toolbox.compress;

import java.io.*;
import java.nio.*;
import java.util.zip.*;

final class DeflateDecompressor extends Decompressor {
    @Override
    public void decompress(ByteBuffer src, ByteBuffer dst) throws IOException {
        try {
            Inflater inflater = new Inflater();
            inflater.setInput(src);
            inflater.inflate(dst);
            inflater.end();
        } catch (DataFormatException e) {
            throw new IOException(e);
        }
    }
}
