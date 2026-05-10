package wtf.reversed.toolbox.io;

import wtf.reversed.toolbox.collect.*;
import wtf.reversed.toolbox.util.*;

import java.io.*;
import java.nio.channels.*;
import java.nio.file.*;

final class FileBinarySource extends BufferedBinarySource {
    private final FileChannel channel;

    private FileBinarySource(FileChannel channel) throws IOException {
        super(channel.size());
        this.channel = channel;
    }

    static FileBinarySource create(Path path) throws IOException {
        Check.nonNull(path, "path");
        return new FileBinarySource(FileChannel.open(path, StandardOpenOption.READ));
    }

    @Override
    int readImpl(Bytes.Mutable target, long position) throws IOException {
        return Math.max(channel.read(target.asMutableBuffer(), position), 0);
    }

    @Override
    public void close() throws IOException {
        reset();
        channel.close();
    }
}
