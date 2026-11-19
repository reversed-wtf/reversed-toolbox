package wtf.reversed.toolbox.io;

import wtf.reversed.toolbox.collect.*;

import java.io.*;

final class SliceBinaryReader extends BufferedBinaryReader {
    private final BufferedBinaryReader reader;
    private final long offset;

    SliceBinaryReader(BufferedBinaryReader reader, long offset, long length) {
        super(length);
        this.reader = reader;
        this.offset = offset;
    }

    @Override
    int readImpl(Bytes.Mutable target, long position) throws IOException {
        assert position >= 0 && target.length() <= size - position : "read past end of slice";
        return reader.readImpl(target, offset + position);
    }

    @Override
    public void close() {
        // Do nothing
    }
}
