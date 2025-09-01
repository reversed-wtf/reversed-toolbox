package wtf.reversed.toolbox.compress;

import wtf.reversed.toolbox.collect.*;

import java.io.*;
import java.util.zip.*;

record DeflateDecompressor(boolean nowrap) implements Decompressor {
    @Override
    public void decompress(Bytes src, MutableBytes dst) throws IOException {
        var srcBuffer = src.asBuffer();
        var dstBuffer = dst.asMutableBuffer();

        var inflater = new Inflater(nowrap);
        inflater.setInput(srcBuffer);

        while (!inflater.finished()) {
            try {
                int count = inflater.inflate(dstBuffer);
                if (count == 0) {
                    break;
                }
            } catch (DataFormatException e) {
                throw new IOException("Invalid compressed data", e);
            }
        }
        inflater.end();
    }
}
