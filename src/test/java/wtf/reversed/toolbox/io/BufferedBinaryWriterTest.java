package wtf.reversed.toolbox.io;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import wtf.reversed.toolbox.collect.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

class BufferedBinaryWriterTest {
    private static final int BUFFER_SIZE = 0x2000;

    @TempDir
    Path directory;

    @Test
    void seekingAndEmptyWritesDoNotFlush() throws IOException {
        var path = directory.resolve("writer.bin");
        try (var writer = BinaryWriter.open(path)) {
            writer.position(16);
            writer.writeInt(0x12345678);
            for (long position : new long[]{64, Long.MAX_VALUE, 2}) {
                writer.position(position);
                writer.writeBytes(Bytes.empty());
                assertThat(writer.position()).isEqualTo(position);
                assertThat(Files.size(path)).isZero();
            }

            writer.flush();
            assertThat(writer.position()).isEqualTo(2);
            var expected = Bytes.allocate(20).setInt(16, 0x12345678);
            assertThat(Files.readAllBytes(path)).containsExactly(expected.toArray());
        }
    }

    @Test
    void seekingAwayAndBackKeepsBufferAvailableForOverwrites() throws IOException {
        var path = directory.resolve("writer.bin");
        try (var writer = BinaryWriter.open(path)) {
            writer.position(16);
            writer.writeLong(0x0102030405060708L);
            writer.position(Long.MAX_VALUE);
            writer.position(18);
            writer.writeShort((short) 0x1122);
            assertThat(writer.position()).isEqualTo(20);
            assertThat(Files.size(path)).isZero();
            writer.position(100);
        }

        var expected = Bytes.allocate(24).setLong(16, 0x0102030405060708L).setShort(18, (short) 0x1122);
        assertThat(Files.readAllBytes(path)).containsExactly(expected.toArray());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 4, 8})
    void primitiveWriteBeforeBufferFlushesPendingData(int width) throws IOException {
        var path = directory.resolve("writer.bin");
        try (var writer = BinaryWriter.open(path)) {
            writer.position(16);
            writer.writeLong(0x0102030405060708L);
            writer.position(0);
            assertThat(Files.size(path)).isZero();

            switch (width) {
                case 1 -> writer.writeByte((byte) 1);
                case 2 -> writer.writeShort((short) 0x0101);
                case 4 -> writer.writeInt(0x01010101);
                case 8 -> writer.writeLong(0x0101010101010101L);
                default -> throw new AssertionError();
            }

            assertThat(writer.position()).isEqualTo(width);
            var flushed = Bytes.allocate(24).setLong(16, 0x0102030405060708L);
            assertThat(Files.readAllBytes(path)).containsExactly(flushed.toArray());
        }

        var expected = Bytes.allocate(24).setLong(16, 0x0102030405060708L);
        expected.slice(0, width).fill((byte) 1);
        assertThat(Files.readAllBytes(path)).containsExactly(expected.toArray());
    }

    @Test
    void bulkOverwritePreservesBufferedTailAndFlushPreservesPosition() throws IOException {
        var path = directory.resolve("writer.bin");
        try (var writer = BinaryWriter.open(path)) {
            writer.writeLong(0x0102030405060708L);
            writer.position(100);
            writer.position(2);
            writer.writeBytes(Bytes.wrap(new byte[]{0x11, 0x22}));
            assertThat(Files.size(path)).isZero();
            writer.flush();
            assertThat(writer.position()).isEqualTo(4);
            writer.writeByte((byte) 0x33);
        }

        var expected = Bytes.allocate(8).setLong(0, 0x0102030405060708L).setShort(2, (short) 0x2211).set(4, (byte) 0x33);
        assertThat(Files.readAllBytes(path)).containsExactly(expected.toArray());
    }

    @ParameterizedTest
    @ValueSource(ints = {3, BUFFER_SIZE, BUFFER_SIZE + 1})
    void bulkWriteAfterForwardSeekPreservesGapAndSubsequentPosition(int length) throws IOException {
        var path = directory.resolve("writer.bin");
        var data = Bytes.allocate(length).fill((byte) 0x5a);
        try (var writer = BinaryWriter.open(path)) {
            writer.writeInt(0x12345678);
            writer.position(64);
            assertThat(Files.size(path)).isZero();
            writer.writeBytes(data);
            assertThat(writer.position()).isEqualTo(64 + length);
            writer.writeByte((byte) 0x7f);
        }

        var expected = Bytes.allocate(65 + length).setInt(0, 0x12345678).set(64 + length, (byte) 0x7f);
        data.copyTo(expected, 64);
        assertThat(Files.readAllBytes(path)).containsExactly(expected.toArray());
    }

    @Test
    void primitiveCrossingBufferBoundaryFlushesAtOriginalOffset() throws IOException {
        var path = directory.resolve("writer.bin");
        var data = Bytes.allocate(BUFFER_SIZE - 1).fill((byte) 0x5a);
        try (var writer = BinaryWriter.open(path)) {
            writer.writeBytes(data);
            writer.position(Long.MAX_VALUE);
            writer.position(BUFFER_SIZE - 1);
            assertThat(Files.size(path)).isZero();
            writer.writeInt(0x12345678);
            assertThat(writer.position()).isEqualTo(BUFFER_SIZE + 3);
            assertThat(Files.readAllBytes(path)).containsExactly(data.toArray());
        }

        var expected = Bytes.allocate(BUFFER_SIZE + 3).setInt(BUFFER_SIZE - 1, 0x12345678);
        data.copyTo(expected, 0);
        assertThat(Files.readAllBytes(path)).containsExactly(expected.toArray());
    }

    @Test
    void failedDeferredFlushCanBeRetriedWithoutLosingPositionOrData() throws IOException {
        var writes = new ArrayList<Write>();
        var writer = new BufferedBinaryWriter() {
            private boolean fail = true;

            @Override
            int writeImpl(Bytes source, long position) throws IOException {
                if (fail) {
                    fail = false;
                    throw new IOException("Test failure");
                }
                int count = Math.min(3, source.length());
                writes.add(new Write(position, Bytes.wrap(source.slice(0, count).toArray())));
                return count;
            }

            @Override
            public void close() throws IOException {
                flush();
            }
        };

        long target = (1L << 32) + 16;
        try (writer) {
            writer.writeInt(0x12345678);
            writer.position(target);
            assertThat(writes).isEmpty();
            assertThatIOException().isThrownBy(() -> writer.writeByte((byte) 42));
            assertThat(writer.position()).isEqualTo(target);
            assertThat(writes).isEmpty();
            writer.writeByte((byte) 42);
            assertThat(writer.position()).isEqualTo(target + 1);
        }

        assertThat(writes).containsExactly(
            new Write(0, Bytes.wrap(new byte[]{0x78, 0x56, 0x34})),
            new Write(3, Bytes.wrap(new byte[]{0x12})),
            new Write(target, Bytes.wrap(new byte[]{42}))
        );
    }

    private record Write(long position, Bytes bytes) {
    }
}
