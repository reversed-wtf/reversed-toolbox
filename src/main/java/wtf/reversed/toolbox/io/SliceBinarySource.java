package wtf.reversed.toolbox.io;

import wtf.reversed.toolbox.collect.*;

import java.io.*;

final class SliceBinarySource extends BufferedBinarySource {
    private final BufferedBinarySource source;
    private final long offset;

    SliceBinarySource(BufferedBinarySource source, long offset, long length) {
        super(length);
        this.source = source;
        this.offset = offset;
    }

    @Override
    int readImpl(Bytes.Mutable target, long position) throws IOException {
        return source.readImpl(target, offset + position);
    }

    @Override
    public void close() {
        // Do nothing
    }
}
