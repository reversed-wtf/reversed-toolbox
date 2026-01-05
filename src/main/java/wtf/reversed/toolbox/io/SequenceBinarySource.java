package wtf.reversed.toolbox.io;

import wtf.reversed.toolbox.collect.*;

import java.io.*;
import java.util.*;

final class SequenceBinarySource extends BufferedBinarySource {
    private final NavigableMap<Long, BinarySource> sources = new TreeMap<>(Long::compareUnsigned);
    private BinarySource source;

    SequenceBinarySource(List<? extends BinarySource> sources) {
        super(sources.stream().mapToLong(BinarySource::size).sum());
        long offset = 0;
        for (BinarySource source : sources) {
            this.sources.put(offset, source);
            offset += source.size();
        }
    }

    @Override
    int readImpl(Bytes.Mutable target, long position) throws IOException {
        reposition(position);
        for (int read = 0; read < target.length(); ) {
            refill(position + read);
            int remaining = target.length() - read;
            int size = Math.min(Math.toIntExact(source.remaining()), remaining);
            source.readBytes(target.slice(read, size));
            read += size;
        }
        return target.length();
    }

    private void refill(long position) throws EOFException {
        if (source.remaining() == 0) {
            reposition(position);
            if (source.remaining() == 0) {
                throw new EOFException();
            }
        }
    }

    private void reposition(long position) {
        var entry = sources.floorEntry(position);
        var base = (long) entry.getKey();
        source = entry.getValue();
        source.position(position - base);
    }

    @Override
    public void close() throws IOException {
        super.close();
        for (BinarySource source : sources.values()) {
            source.close();
        }
    }
}
