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
        while (target.hasRemaining()) {
            if (source == null || source.remaining() == 0) {
                var entry = sources.floorEntry(position);
                long base = entry.getKey();
                source = entry.getValue();
                source.position(position - base);
            }
            if (source.remaining() == 0) {
                throw new EOFException();
            }
            int read = Math.min(Math.toIntExact(source.remaining()), target.remaining());
            source.readBytes(Bytes.Mutable.wrap(target.array(), target.position(), read));
            target.position(target.position() + read);
            position += read;
        }
        return target.position();
    }

    @Override
    public void close() throws IOException {
        super.close();
        for (BinarySource source : sources.values()) {
            source.close();
        }
    }
}
