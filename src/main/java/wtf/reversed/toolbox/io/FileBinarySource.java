package wtf.reversed.toolbox.io;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;

final class FileBinarySource extends BufferedBinarySource {
    private final FileChannel channel;

    private FileBinarySource(FileChannel channel) throws IOException {
        super(channel.size());
        this.channel = channel;
    }

    static FileBinarySource create(Path path) throws IOException {
        return new FileBinarySource(FileChannel.open(path, StandardOpenOption.READ));
    }

    @Override
    int readImpl(ByteBuffer target, long position) throws IOException {
        return channel.read(target, position);
    }

    @Override
    public void close() throws IOException {
        super.close();
        channel.close();
    }
}
