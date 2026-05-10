package wtf.reversed.toolbox.io;

import wtf.reversed.toolbox.collect.*;
import wtf.reversed.toolbox.util.*;

import java.io.*;

final class BytesBinarySource extends BinarySource {
    private final Bytes bytes;
    private int position = 0;

    BytesBinarySource(Bytes bytes) {
        super(Check.nonNull(bytes, "bytes").length());
        this.bytes = bytes;
    }

    @Override
    public long position() {
        return position;
    }

    @Override
    public BinarySource position(long position) {
        this.position = Math.toIntExact(Check.position(position, size, "position"));
        return this;
    }

    @Override
    public BinarySource slice(long offset, long length) {
        Bytes sliced = bytes.slice(Math.toIntExact(offset), Math.toIntExact(length));
        return new BytesBinarySource(sliced);
    }

    @Override
    public void readBytes(Bytes.Mutable target) throws IOException {
        ensureRemaining(target.length());
        bytes.slice(position, target.length()).copyTo(target, 0);
        position += target.length();
    }

    @Override
    public byte readByte() throws IOException {
        ensureRemaining(Byte.BYTES);
        byte result = bytes.get(position);
        position += Byte.BYTES;
        return result;
    }

    @Override
    public short readShort() throws IOException {
        ensureRemaining(Short.BYTES);
        short result = bytes.getShort(position);
        position += Short.BYTES;
        return bigEndian ? Short.reverseBytes(result) : result;
    }

    @Override
    public int readInt() throws IOException {
        ensureRemaining(Integer.BYTES);
        int result = bytes.getInt(position);
        position += Integer.BYTES;
        return bigEndian ? Integer.reverseBytes(result) : result;
    }

    @Override
    public long readLong() throws IOException {
        ensureRemaining(Long.BYTES);
        long result = bytes.getLong(position);
        position += Long.BYTES;
        return bigEndian ? Long.reverseBytes(result) : result;
    }

    @Override
    public void close() {
        // nothing to do
    }
}
