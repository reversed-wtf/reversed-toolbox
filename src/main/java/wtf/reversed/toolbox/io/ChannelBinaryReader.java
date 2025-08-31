package wtf.reversed.toolbox.io;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;

final class ChannelBinaryReader extends BufferedBinaryReader {
    private final SeekableByteChannel channel;

    ChannelBinaryReader(SeekableByteChannel channel) throws IOException {
        super(channel.size());
        this.channel = channel;
    }

    @Override
    protected void readImpl(ByteBuffer dst) throws IOException {
        while (dst.hasRemaining()) {
            if (channel.read(dst) < 0) {
                throw new EOFException();
            }
        }
    }

    @Override
    protected void positionImpl(long position) throws IOException {
        channel.position(position);
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }
}
