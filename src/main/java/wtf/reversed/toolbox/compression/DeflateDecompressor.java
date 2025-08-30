package wtf.reversed.toolbox.compression;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

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
