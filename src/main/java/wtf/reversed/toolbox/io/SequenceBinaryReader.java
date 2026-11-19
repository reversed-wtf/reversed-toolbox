package wtf.reversed.toolbox.io;

import wtf.reversed.toolbox.collect.*;
import wtf.reversed.toolbox.util.*;

import java.io.*;
import java.util.*;

final class SequenceBinaryReader extends BufferedBinaryReader {
    private final NavigableMap<Long, BinaryReader> readers = new TreeMap<>(Long::compareUnsigned);
    private BinaryReader reader;

    SequenceBinaryReader(List<? extends BinaryReader> readers) {
        Check.nonEmpty(readers, "readers");
        super(readers.stream().mapToLong(BinaryReader::size).sum()); // This is weird

        long offset = 0;
        for (BinaryReader reader : readers) {
            this.readers.put(offset, reader);
            offset += reader.size();
        }
    }

    @Override
    int readImpl(Bytes.Mutable target, long position) throws IOException {
        reposition(position);
        int read = 0;
        while (read < target.length()) {
            if (reader.remaining() == 0) {
                reposition(position + read);
                if (reader.remaining() == 0) {
                    break;
                }
            }
            int remaining = target.length() - read;
            int size = Math.min(Math.toIntExact(reader.remaining()), remaining);
            reader.readBytes(target.slice(read, size));
            read += size;
        }
        return read;
    }

    private void reposition(long position) {
        var entry = readers.floorEntry(position);
        long base = entry.getKey();
        reader = entry.getValue();
        reader.position(position - base);
    }

    @Override
    public void close() throws IOException {
        reset();
        for (BinaryReader reader : readers.values()) {
            reader.close();
        }
    }
}
