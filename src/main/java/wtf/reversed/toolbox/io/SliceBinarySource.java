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
        int length = (int) Math.min(target.length(), size - position);
        if (length <= 0) {
            return 0;
        }
        return source.readImpl(target.slice(0, length), offset + position);
    }

    @Override
    public void close() {
        // Do nothing
    }
}
