package wtf.reversed.toolbox.io;

import wtf.reversed.toolbox.collect.*;
import wtf.reversed.toolbox.util.*;

import java.io.*;
import java.nio.channels.*;
import java.nio.file.*;

final class FileBinaryReader extends BufferedBinaryReader {
    private final Path path;
    private FileChannel channel;

    private FileBinaryReader(Path path, FileChannel channel) throws IOException {
        super(channel.size());
        this.path = path;
        this.channel = channel;
    }

    static FileBinaryReader create(Path path) throws IOException {
        Check.nonNull(path, "path");
        var channel = newChannel(path);
        trySetUninterruptible(channel);
        return new FileBinaryReader(path, channel);
    }

    @Override
    int readImpl(Bytes.Mutable target, long position) throws IOException {
        if (channel == null) {
            throw new IOException("Source is closed");
        }
        if (!channel.isOpen()) {
            channel = newChannel(path);
        }
        return Math.max(channel.read(target.asMutableBuffer(), position), 0);
    }

    @Override
    public void close() throws IOException {
        var channel = this.channel;
        this.channel = null;
        reset();
        if (channel != null) {
            channel.close();
        }
    }

    private static FileChannel newChannel(Path path) throws IOException {
        return FileChannel.open(path, StandardOpenOption.READ);
    }

    static void trySetUninterruptible(FileChannel channel) {
        try {
            @SuppressWarnings("Java9ReflectionClassVisibility")
            var fileChannelImpl = Class.forName("sun.nio.ch.FileChannelImpl");
            var setUninterruptible = fileChannelImpl.getDeclaredMethod("setUninterruptible");
            setUninterruptible.setAccessible(true);
            setUninterruptible.invoke(channel);
        } catch (Exception ignored) {
        }
    }
}
