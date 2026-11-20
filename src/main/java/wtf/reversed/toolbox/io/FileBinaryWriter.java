package wtf.reversed.toolbox.io;

import wtf.reversed.toolbox.collect.*;
import wtf.reversed.toolbox.util.*;

import java.io.*;
import java.nio.channels.*;
import java.nio.file.*;

import static java.nio.file.StandardOpenOption.*;
import static wtf.reversed.toolbox.io.FileBinarySource.*;

final class FileBinaryWriter extends BufferedBinaryWriter {
    private final Path path;
    private FileChannel channel;

    private FileBinaryWriter(Path path, FileChannel channel) {
        this.path = path;
        this.channel = channel;
    }

    static FileBinaryWriter create(Path path) throws IOException {
        Check.nonNull(path, "path");
        var channel = newChannel(path, WRITE, CREATE, TRUNCATE_EXISTING);
        trySetUninterruptible(channel);
        return new FileBinaryWriter(path, channel);
    }

    @Override
    int writeImpl(Bytes source, long position) throws IOException {
        if (channel == null) {
            throw new IOException("Writer is closed");
        }
        if (!channel.isOpen()) {
            channel = newChannel(path, WRITE);
        }
        return Math.max(channel.write(source.asBuffer(), position), 0);
    }

    @Override
    public void close() throws IOException {
        if (channel == null) {
            return;
        }
        // The regular try (channel) would capture the current channel and close it,
        // but if writeImpl reassigns channel, the new channel would not be closed.
        try (Closeable _ = () -> this.channel.close()) {
            flush();
        } finally {
            channel = null;
            reset();
        }
    }

    private static FileChannel newChannel(Path path, OpenOption... options) throws IOException {
        return FileChannel.open(path, options);
    }
}
