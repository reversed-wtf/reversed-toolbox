package wtf.reversed.toolbox.io;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import wtf.reversed.toolbox.collect.*;

import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

class BinaryWriterTest {
    @Test
    void testInitialStateAndByteOrder() {
        try (var writer = new BytesBinaryWriter()) {
            assertThat(writer.position()).isZero();
            assertThat(writer.toBytes().toArray()).isEmpty();
            assertThat(writer.order()).isEqualTo(ByteOrder.LITTLE_ENDIAN);
            assertThat(writer.order(ByteOrder.BIG_ENDIAN)).isSameAs(writer);
            assertThat(writer.order()).isEqualTo(ByteOrder.BIG_ENDIAN);
            writer.order(ByteOrder.LITTLE_ENDIAN);
            assertThat(writer.order()).isEqualTo(ByteOrder.LITTLE_ENDIAN);
            assertThatNullPointerException().isThrownBy(() -> writer.order(null));
        }
    }

    @Test
    void testPositionAndOverwrite() {
        try (var writer = new BytesBinaryWriter()) {
            writer.writeBytes(Bytes.wrap(new byte[]{1, 2, 3, 4}));
            assertThat(writer.position()).isEqualTo(4);
            assertThat(writer.position(1)).isSameAs(writer);
            writer.writeByte((byte) 5);
            assertThat(writer.position()).isEqualTo(2);
            assertThat(writer.toBytes().toArray()).containsExactly(1, 5, 3, 4);
            assertThatIllegalArgumentException().isThrownBy(() -> writer.position(-1));
            assertThat(writer.position()).isEqualTo(2);
        }
    }

    @Test
    void testSeekBeyondEndAndBufferGrowth() {
        try (var writer = new BytesBinaryWriter(0)) {
            writer.writeByte((byte) 1);
            writer.position(100);
            assertThat(writer.toBytes().toArray()).containsExactly(1);
            writer.writeLong(0x12345678L);
            assertThat(writer.position()).isEqualTo(108);
            assertThat(writer.toBytes().toArray()).containsExactly(
                ByteBuffer.allocate(108).order(ByteOrder.LITTLE_ENDIAN).put((byte) 1).putLong(100, 0x12345678L).array()
            );
        }
    }

    @Test
    void testSnapshotsAreIndependent() {
        try (var writer = new BytesBinaryWriter(1)) {
            writer.writeByte((byte) 1);
            var snapshot = writer.toBytes();
            writer.position(0);
            writer.writeInt(0x12345678);
            assertThat(snapshot.toArray()).containsExactly(1);
            assertThat(writer.toBytes().toArray()).containsExactly(0x78, 0x56, 0x34, 0x12);
        }
    }

    @ParameterizedTest
    @MethodSource("byteOrders")
    void testWriteIntegerPrimitives(ByteOrder order) {
        try (var writer = new BytesBinaryWriter()) {
            writer.order(order);
            writer.writeByte((byte) 0xff);
            writer.writeShort((short) 0x1234);
            writer.writeInt(0x12345678);
            writer.writeLong(0x0001020304050607L);

            var expected = ByteBuffer.allocate(15).order(order)
                .put((byte) 0xff).putShort((short) 0x1234).putInt(0x12345678).putLong(0x0001020304050607L);
            assertThat(writer.toBytes().toArray()).containsExactly(expected.array());
            assertThat(writer.position()).isEqualTo(15);
        }
    }

    @ParameterizedTest
    @MethodSource("byteOrders")
    void testWriteFloatingPointPrimitives(ByteOrder order) throws IOException {
        try (var writer = new BytesBinaryWriter()) {
            writer.order(order);
            writer.writeHalf(1.0f);
            writer.writeFloat(1.5f);
            writer.writeDouble(-2.5);

            var expected = ByteBuffer.allocate(14).order(order).putShort((short) 0x3c00).putFloat(1.5f).putDouble(-2.5);
            assertThat(writer.toBytes().toArray()).containsExactly(expected.array());
            assertThat(writer.position()).isEqualTo(14);
        }
    }

    @ParameterizedTest
    @MethodSource("byteOrders")
    void testWriteBooleans(ByteOrder order) throws IOException {
        try (var writer = new BytesBinaryWriter()) {
            writer.order(order);
            writer.writeBool(false, BoolFormat.BYTE);
            writer.writeBool(true, BoolFormat.BYTE);
            writer.writeBool(false, BoolFormat.SHORT);
            writer.writeBool(true, BoolFormat.SHORT);
            writer.writeBool(false, BoolFormat.INT);
            writer.writeBool(true, BoolFormat.INT);

            var expected = ByteBuffer.allocate(14).order(order)
                .put((byte) 0).put((byte) 1).putShort((short) 0).putShort((short) 1).putInt(0).putInt(1);
            assertThat(writer.toBytes().toArray()).containsExactly(expected.array());
        }
    }

    @ParameterizedTest
    @MethodSource("byteOrders")
    void testWriteIntegerArrays(ByteOrder order) throws IOException {
        try (var writer = new BytesBinaryWriter()) {
            writer.order(order);
            writer.writeShorts(Shorts.copyOf(new short[]{0x1234, -1}));
            writer.writeInts(Ints.copyOf(new int[]{0x12345678, -2}));
            writer.writeLongs(Longs.copyOf(new long[]{0x0001020304050607L, -3}));

            var expected = ByteBuffer.allocate(28).order(order)
                .putShort((short) 0x1234).putShort((short) -1)
                .putInt(0x12345678).putInt(-2)
                .putLong(0x0001020304050607L).putLong(-3);
            assertThat(writer.toBytes().toArray()).containsExactly(expected.array());
            assertThat(writer.position()).isEqualTo(28);
        }
    }

    @ParameterizedTest
    @MethodSource("byteOrders")
    void testWriteFloatingPointArrays(ByteOrder order) throws IOException {
        try (var writer = new BytesBinaryWriter()) {
            writer.order(order);
            writer.writeHalfs(Floats.copyOf(new float[]{1.0f, -2.0f}));
            writer.writeFloats(Floats.copyOf(new float[]{1.5f, -2.5f}));
            writer.writeDoubles(Doubles.copyOf(new double[]{1.5, -2.5}));

            var expected = ByteBuffer.allocate(28).order(order)
                .putShort((short) 0x3c00).putShort((short) 0xc000)
                .putFloat(1.5f).putFloat(-2.5f).putDouble(1.5).putDouble(-2.5);
            assertThat(writer.toBytes().toArray()).containsExactly(expected.array());
            assertThat(writer.position()).isEqualTo(28);
        }
    }

    @ParameterizedTest
    @MethodSource("byteOrders")
    void testEmptyWritesDoNotAdvancePosition(ByteOrder order) throws IOException {
        try (var writer = new BytesBinaryWriter()) {
            writer.order(order);
            writer.position(10);
            writer.writeBytes(Bytes.empty());
            writer.writeShorts(Shorts.empty());
            writer.writeInts(Ints.empty());
            writer.writeLongs(Longs.empty());
            writer.writeHalfs(Floats.empty());
            writer.writeFloats(Floats.empty());
            writer.writeDoubles(Doubles.empty());
            writer.writeZeros(0);
            assertThat(writer.position()).isEqualTo(10);
            assertThat(writer.toBytes().toArray()).isEmpty();
        }
    }

    @ParameterizedTest
    @MethodSource("byteOrders")
    void testWriteLengthPrefixedStrings(ByteOrder order) throws IOException {
        try (var writer = new BytesBinaryWriter()) {
            writer.order(order);
            writer.writeString("hello", StringFormat.BYTE_LENGTH);
            writer.writeString("abc", StringFormat.SHORT_LENGTH);
            writer.writeString("hi", StringFormat.INT_LENGTH);

            var expected = ByteBuffer.allocate(17).order(order)
                .put((byte) 5).put(new byte[]{'h', 'e', 'l', 'l', 'o'})
                .putShort((short) 3).put(new byte[]{'a', 'b', 'c'})
                .putInt(2).put(new byte[]{'h', 'i'});
            assertThat(writer.toBytes().toArray()).containsExactly(expected.array());
            assertThat(writer.position()).isEqualTo(17);
        }
    }

    @Test
    void testStringLengthCountsEncodedBytes() throws IOException {
        try (var writer = new BytesBinaryWriter()) {
            writer.writeString("café", StringFormat.BYTE_LENGTH);
            writer.writeString("AB", StringFormat.SHORT_LENGTH, StandardCharsets.UTF_16LE);
            assertThat(writer.toBytes().toArray()).containsExactly(5, 'c', 'a', 'f', 0xc3, 0xa9, 4, 0, 'A', 0, 'B', 0);
        }
    }

    @Test
    void testOversizedStringsAreRejectedBeforeWriting() {
        try (var writer = new BytesBinaryWriter()) {
            assertThatIllegalArgumentException().isThrownBy(() -> writer.writeString("a".repeat(256), StringFormat.BYTE_LENGTH));
            assertThatIllegalArgumentException().isThrownBy(() -> writer.writeString("a".repeat(65536), StringFormat.SHORT_LENGTH));
            assertThat(writer.position()).isZero();
            assertThat(writer.toBytes().toArray()).isEmpty();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"UTF-8", "UTF-16LE", "UTF-16BE", "UTF-32LE", "UTF-32BE"})
    void testWriteNullTerminatedStrings(String charsetName) throws IOException {
        var charset = Charset.forName(charsetName);
        try (var writer = new BytesBinaryWriter()) {
            writer.writeString("AB", StringFormat.NULL_TERM, charset);
            writer.writeString("", StringFormat.NULL_TERM, charset);
            assertThat(writer.toBytes().toArray()).containsExactly("AB\0\0".getBytes(charset));
        }
    }

    @Test
    void testWriteFixedLengthStrings() throws IOException {
        try (var writer = new BytesBinaryWriter()) {
            writer.writeString("hi", 2);
            writer.writeString("a", 4);
            writer.writeString("abcd", 2);
            writer.writeString("abc", 0);
            writer.writeString("", 2);
            assertThat(writer.toBytes().toArray()).containsExactly('h', 'i', 'a', 0, 0, 0, 'a', 'b', 0, 0);
            assertThat(writer.position()).isEqualTo(10);
        }
    }

    @Test
    void testWriteFixedLengthStringsWithCharset() throws IOException {
        try (var writer = new BytesBinaryWriter()) {
            writer.writeString("AB", 6, StandardCharsets.UTF_16LE);
            writer.writeString("AB", 2, StandardCharsets.UTF_16LE);
            assertThat(writer.toBytes().toArray()).containsExactly('A', 0, 'B', 0, 0, 0, 'A', 0);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 7, 8, 15, 16, 17})
    void testWriteZeros(int count) throws IOException {
        try (var writer = new BytesBinaryWriter()) {
            writer.writeBytes(Bytes.allocate(count).fill((byte) 0xff));
            writer.position(0);
            writer.writeZeros(count);
            assertThat(writer.toBytes().toArray()).containsExactly(new byte[count]);
            assertThat(writer.position()).isEqualTo(count);
            assertThatIllegalArgumentException().isThrownBy(() -> writer.writeZeros(-1));
            assertThat(writer.position()).isEqualTo(count);
        }
    }

    @Test
    void testOutputStreamWritesBytesAndSlices() throws IOException {
        try (var writer = new BytesBinaryWriter()) {
            try (var stream = writer.asOutputStream()) {
                stream.write(0x1ff);
                stream.write(new byte[]{1, 2, 3, 4}, 1, 2);
            }
            writer.writeByte((byte) 5);
            assertThat(writer.toBytes().toArray()).containsExactly(0xff, 2, 3, 5);
        }
    }

    @Test
    void testCharacterWriterUsesCharset() throws IOException {
        try (var writer = new BytesBinaryWriter()) {
            try (var text = writer.asWriter(StandardCharsets.UTF_16LE)) {
                text.write("café");
            }
            assertThat(writer.toBytes().toArray()).containsExactly("café".getBytes(StandardCharsets.UTF_16LE));
        }
    }

    @Test
    void testFileWriterTruncatesAndFlushesOnClose(@TempDir Path directory) throws IOException {
        var path = directory.resolve("writer.bin");
        Files.write(path, new byte[]{1, 2, 3, 4, 5});
        try (var writer = BinaryWriter.open(path)) {
            assertThat(Files.size(path)).isZero();
            writer.writeInt(0x12345678);
        }
        assertThat(Files.readAllBytes(path)).containsExactly(0x78, 0x56, 0x34, 0x12);
    }

    @Test
    void testStreamCloseFlushesWithoutClosingFileWriter(@TempDir Path directory) throws IOException {
        var path = directory.resolve("writer.bin");
        try (var writer = BinaryWriter.open(path)) {
            try (var stream = writer.asOutputStream()) {
                stream.write(new byte[]{1, 2});
                stream.flush();
                assertThat(Files.readAllBytes(path)).containsExactly(1, 2);
                stream.write(3);
            }
            assertThat(Files.readAllBytes(path)).containsExactly(1, 2, 3);
            writer.writeByte((byte) 4);
        }
        assertThat(Files.readAllBytes(path)).containsExactly(1, 2, 3, 4);
    }

    static List<ByteOrder> byteOrders() {
        return List.of(ByteOrder.LITTLE_ENDIAN, ByteOrder.BIG_ENDIAN);
    }
}
