package wtf.reversed.toolbox.io;

import wtf.reversed.toolbox.collect.*;
import wtf.reversed.toolbox.util.*;

import java.io.*;
import java.util.*;

final class SequenceBinarySource extends BufferedBinarySource {
    private final NavigableMap<Long, BinarySource> sources = new TreeMap<>(Long::compareUnsigned);
    private BinarySource source;

    SequenceBinarySource(List<? extends BinarySource> sources) {
        super(sources.stream().mapToLong(BinarySource::size).sum());
        Check.argument(!sources.isEmpty(), "At least one reader must be provided");
        long offset = 0;
        for (BinarySource source : sources) {
            this.sources.put(offset, source);
            offset += source.size();
        }
    }

    @Override
    int readImpl(Bytes.Mutable target, long position) throws IOException {
        reposition(position);
        int read = 0;
        while (read < target.length()) {
            if (source.remaining() == 0) {
                reposition(position + read);
                if (source.remaining() == 0) {
                    break;
                }
            }
            int remaining = target.length() - read;
            int size = Math.min(Math.toIntExact(source.remaining()), remaining);
            source.readBytes(target.slice(read, size));
            read += size;
        }
        return read;
    }

    private void reposition(long position) {
        var entry = sources.floorEntry(position);
        long base = (long) entry.getKey();
        source = entry.getValue();
        source.position(position - base);
    }

    @Override
    public void close() throws IOException {
        reset();
        for (BinarySource source : sources.values()) {
            source.close();
        }
    }
}
