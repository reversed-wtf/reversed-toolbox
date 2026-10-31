package wtf.reversed.toolbox.io;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.*;
import wtf.reversed.toolbox.collect.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class BufferedBinarySourceTest {
    private static final int BUFFER_SIZE = 0x2000;

    @TempDir
    Path directory;

    @Test
    void bulkReadAfterExhaustingBuffer() throws IOException {
        byte[] data = data(BUFFER_SIZE * 2);
        try (var source = open(data)) {
            source.readByte();
            source.readBytes(Bytes.allocate(BUFFER_SIZE - 1));
            var target = Bytes.allocate(BUFFER_SIZE);
            source.readBytes(target);
            assertArrayEquals(Arrays.copyOfRange(data, BUFFER_SIZE, data.length), target.toArray());
            assertEquals(data.length, source.position());
        }
    }

    @Test
    void sliceStopsAtItsEnd() throws IOException {
        byte[] data = data(32);
        try (var source = open(data); var slice = source.slice(10, 2)) {
            assertEquals(data[10], slice.readByte());
            assertThrows(EOFException.class, slice::readShort);
            assertThrows(EOFException.class, () -> slice.readBytes(Bytes.allocate(2)));
            assertEquals(1, slice.position());
            assertEquals(data[11], slice.readByte());
            assertThrows(EOFException.class, slice::readByte);
        }
    }

    @Test
    void primitiveAndBulkReadsAccumulateShortReads() throws IOException {
        byte[] data = data(BUFFER_SIZE * 2);
        var source = new BufferedBinarySource(data.length) {
            @Override
            int readImpl(Bytes.Mutable target, long position) {
                int length = (int) Math.min(3, Math.min(target.length(), data.length - position));
                Bytes.wrap(data, (int) position, length).copyTo(target, 0);
                return length;
            }

            @Override
            public void close() {
            }
        };
        try (source) {
            assertEquals(Bytes.wrap(data).getLong(0), source.readLong());
            var target = Bytes.allocate(data.length - 8);
            source.readBytes(target);
            assertArrayEquals(Arrays.copyOfRange(data, 8, data.length), target.toArray());
            assertEquals(data.length, source.position());
        }
    }

    private BinarySource open(byte[] data) throws IOException {
        return BinarySource.open(Files.write(directory.resolve("source.bin"), data));
    }

    private static byte[] data(int length) {
        byte[] data = new byte[length];
        new Random(42).nextBytes(data);
        return data;
    }
}
