package wtf.reversed.toolbox.io;

import wtf.reversed.toolbox.collect.*;

import java.io.*;
import java.nio.*;
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
    int readImpl(ByteBuffer target, long position) throws IOException {
        reposition(position);
        int remaining = target.remaining();
        while (target.hasRemaining()) {
            refill(position);
            int size = Math.min(Math.toIntExact(source.remaining()), target.remaining());
            source.readBytes(Bytes.Mutable.wrap(target.array(), target.position(), size));
            target.position(target.position() + size);
            position += size;
        }
        return remaining;
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
