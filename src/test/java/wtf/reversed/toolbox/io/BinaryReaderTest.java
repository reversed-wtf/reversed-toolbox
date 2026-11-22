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

class BinaryReaderTest {
    @Test
    void testInitialPositionIsZero() throws IOException {
        try (var reader = bytes(0x01, 0x02, 0x03)) {
            assertThat(reader.position()).isZero();
            assertThat(reader.size()).isEqualTo(3);
            assertThat(reader.remaining()).isEqualTo(3);
        }
    }

    @Test
    void testRemainingDecreasesAsBytesRead() throws IOException {
        try (var reader = bytes(0x01, 0x02, 0x03)) {
            reader.readByte();
            assertThat(reader.remaining()).isEqualTo(2);
            reader.readByte();
            assertThat(reader.remaining()).isEqualTo(1);
            reader.readByte();
            assertThat(reader.remaining()).isZero();
        }
    }

    @Test
    void testPositionSetAdvancesCursor() throws IOException {
        try (var reader = bytes(0x01, 0x02, 0x03, 0x04)) {
            reader.position(2);
            assertThat(reader.position()).isEqualTo(2);
            assertThat(reader.readByte()).isEqualTo((byte) 0x03);
        }
    }

    @Test
    void testPositionRejectsNegative() throws IOException {
        try (var reader = bytes(0x01, 0x02)) {
            assertThatIllegalArgumentException().isThrownBy(() -> reader.position(-1));
        }
    }

    @Test
    void testPositionRejectsBeyondSize() throws IOException {
        try (var reader = bytes(0x01, 0x02)) {
            assertThatIllegalArgumentException().isThrownBy(() -> reader.position(3));
        }
    }

    @Test
    void testPositionAtSizeIsAllowed() throws IOException {
        try (var reader = bytes(0x01, 0x02)) {
            reader.position(2);
            assertThat(reader.position()).isEqualTo(2);
            assertThat(reader.remaining()).isZero();
        }
    }

    @Test
    void testSkipAdvancesCursor() throws IOException {
        try (var reader = bytes(0x01, 0x02, 0x03, 0x04)) {
            reader.skip(2);
            assertThat(reader.position()).isEqualTo(2);
            assertThat(reader.readByte()).isEqualTo((byte) 0x03);
        }
    }

    @Test
    void testSkipRejectsNegative() throws IOException {
        try (var reader = bytes(0x01, 0x02)) {
            assertThatIllegalArgumentException().isThrownBy(() -> reader.skip(-1));
        }
    }

    @Test
    void testReadShortLittleEndianByDefault() throws IOException {
        try (var reader = bytes(0x34, 0x12)) {
            assertThat(reader.order()).isEqualTo(ByteOrder.LITTLE_ENDIAN);
            assertThat(reader.readShort()).isEqualTo((short) 0x1234);
        }
    }

    @Test
    void testReadShortBigEndian() throws IOException {
        try (var reader = bytes(0x12, 0x34)) {
            reader.order(ByteOrder.BIG_ENDIAN);
            assertThat(reader.readShort()).isEqualTo((short) 0x1234);
        }
    }

    @Test
    void testReadIntBothEndians() throws IOException {
        try (var reader = bytes(0x78, 0x56, 0x34, 0x12, 0x12, 0x34, 0x56, 0x78)) {
            assertThat(reader.readInt()).isEqualTo(0x12345678);
            reader.order(ByteOrder.BIG_ENDIAN);
            assertThat(reader.readInt()).isEqualTo(0x12345678);
        }
    }

    @Test
    void testReadLongBothEndians() throws IOException {
        try (var reader = bytes(
            0x07, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01, 0x00,
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07)) {
            assertThat(reader.readLong()).isEqualTo(0x0001020304050607L);
            reader.order(ByteOrder.BIG_ENDIAN);
            assertThat(reader.readLong()).isEqualTo(0x0001020304050607L);
        }
    }

    @Test
    void testReadUnsignedByte() throws IOException {
        try (var reader = bytes(0x7F, 0x80, 0xFF)) {
            assertThat(reader.readUnsignedByte()).isEqualTo(0x7F);
            assertThat(reader.readUnsignedByte()).isEqualTo(0x80);
            assertThat(reader.readUnsignedByte()).isEqualTo(0xFF);
            assertThat(reader.position()).isEqualTo(3);
        }
    }

    @Test
    void testReadUnsignedShortBothEndians() throws IOException {
        try (var reader = bytes(0xFE, 0xFF, 0x80, 0x01)) {
            assertThat(reader.readUnsignedShort()).isEqualTo(0xFFFE);
            reader.order(ByteOrder.BIG_ENDIAN);
            assertThat(reader.readUnsignedShort()).isEqualTo(0x8001);
        }
    }

    @Test
    void testReadUnsignedIntBothEndians() throws IOException {
        try (var reader = bytes(0xFE, 0xFF, 0xFF, 0xFF, 0x80, 0x00, 0x00, 0x01)) {
            assertThat(reader.readUnsignedInt()).isEqualTo(0xFFFF_FFFEL);
            reader.order(ByteOrder.BIG_ENDIAN);
            assertThat(reader.readUnsignedInt()).isEqualTo(0x8000_0001L);
        }
    }

    @Test
    void testReadUnsignedIntBeyondRemainingThrows() throws IOException {
        try (var reader = bytes(0x01, 0x02, 0x03)) {
            assertThatExceptionOfType(EOFException.class).isThrownBy(reader::readUnsignedInt);
        }
    }

    @Test
    void testReadFloat() throws IOException {
        var bits = Float.floatToRawIntBits(1.5f);
        try (var reader = bytes(bits & 0xFF, (bits >>> 8) & 0xFF, (bits >>> 16) & 0xFF, (bits >>> 24) & 0xFF)) {
            assertThat(reader.readFloat()).isEqualTo(1.5f);
        }
    }

    @Test
    void testReadDouble() throws IOException {
        var bits = Double.doubleToRawLongBits(1.5);
        var data = new int[8];
        for (var i = 0; i < 8; i++) {
            data[i] = (int) ((bits >>> (i * 8)) & 0xFF);
        }
        try (var reader = bytes(data)) {
            assertThat(reader.readDouble()).isEqualTo(1.5);
        }
    }

    @Test
    void testReadHalfRoundtripsToFloat() throws IOException {
        try (var reader = bytes(0x00, 0x3C)) {
            assertThat(reader.readHalf()).isEqualTo(1.0f);
        }
    }

    @Test
    void testReadBytesZeroReturnsEmpty() throws IOException {
        try (var reader = bytes(0x01, 0x02)) {
            assertThat(reader.readBytes(0).length()).isZero();
            assertThat(reader.position()).isZero();
        }
    }

    @Test
    void testReadBytesCount() throws IOException {
        try (var reader = bytes(0x01, 0x02, 0x03, 0x04)) {
            var read = reader.readBytes(3);
            assertThat(read.length()).isEqualTo(3);
            assertThat(read.get(0)).isEqualTo((byte) 0x01);
            assertThat(read.get(1)).isEqualTo((byte) 0x02);
            assertThat(read.get(2)).isEqualTo((byte) 0x03);
            assertThat(reader.position()).isEqualTo(3);
        }
    }

    @Test
    void testReadBytesNegativeCountRejected() throws IOException {
        try (var reader = bytes(0x01, 0x02)) {
            assertThatIllegalArgumentException().isThrownBy(() -> reader.readBytes(-1));
        }
    }

    @Test
    void testReadBytesBeyondRemainingThrows() throws IOException {
        try (var reader = bytes(0x01, 0x02)) {
            assertThatIOException().isThrownBy(() -> reader.readBytes(5));
        }
    }

    @Test
    void testReadRemaining() throws IOException {
        try (var reader = bytes(0x01, 0x02, 0x03, 0x04)) {
            reader.readByte();
            var read = reader.readRemaining();
            assertThat(read.length()).isEqualTo(3);
            assertThat(read.get(0)).isEqualTo((byte) 0x02);
            assertThat(read.get(2)).isEqualTo((byte) 0x04);
            assertThat(reader.remaining()).isZero();
            assertThat(reader.readRemaining().length()).isZero();
        }
    }

    @Test
    void testReadShortsCount() throws IOException {
        try (var reader = bytes(0x01, 0x00, 0x02, 0x00, 0x03, 0x00)) {
            var read = reader.readShorts(3);
            assertThat(read.length()).isEqualTo(3);
            assertThat(read.get(0)).isEqualTo((short) 1);
            assertThat(read.get(1)).isEqualTo((short) 2);
            assertThat(read.get(2)).isEqualTo((short) 3);
        }
    }

    @Test
    void testReadIntsCount() throws IOException {
        try (var reader = bytes(0x01, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00)) {
            var read = reader.readInts(2);
            assertThat(read.length()).isEqualTo(2);
            assertThat(read.get(0)).isEqualTo(1);
            assertThat(read.get(1)).isEqualTo(2);
        }
    }

    @Test
    void testReadLongsCount() throws IOException {
        try (var reader = bytes(
            0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)) {
            var read = reader.readLongs(2);
            assertThat(read.length()).isEqualTo(2);
            assertThat(read.get(0)).isEqualTo(1L);
            assertThat(read.get(1)).isEqualTo(2L);
        }
    }

    @Test
    void testReadLongAsIntFitsInRange() throws IOException {
        try (var reader = bytes(0x05, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)) {
            assertThat(reader.readLongAsInt()).isEqualTo(5);
        }
    }

    @Test
    void testReadLongAsIntOverflowThrows() throws IOException {
        try (var reader = bytes(0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00)) {
            assertThatExceptionOfType(ArithmeticException.class)
                .isThrownBy(reader::readLongAsInt);
        }
    }

    @Test
    void testReadBoolByteFalse() throws IOException {
        try (var reader = bytes(0x00)) {
            assertThat(reader.readBool(BoolFormat.BYTE)).isFalse();
        }
    }

    @Test
    void testReadBoolByteTrue() throws IOException {
        try (var reader = bytes(0x01)) {
            assertThat(reader.readBool(BoolFormat.BYTE)).isTrue();
        }
    }

    @Test
    void testReadBoolShort() throws IOException {
        try (var reader = bytes(0x01, 0x00)) {
            assertThat(reader.readBool(BoolFormat.SHORT)).isTrue();
        }
    }

    @Test
    void testReadBoolInt() throws IOException {
        try (var reader = bytes(0x00, 0x00, 0x00, 0x00)) {
            assertThat(reader.readBool(BoolFormat.INT)).isFalse();
        }
    }

    @Test
    void testReadBoolInvalidValueThrows() throws IOException {
        try (var reader = bytes(0x02)) {
            assertThatIOException().isThrownBy(() -> reader.readBool(BoolFormat.BYTE));
        }
    }

    @Test
    void testReadStringByteLength() throws IOException {
        try (var reader = bytes(0x05, 'h', 'e', 'l', 'l', 'o')) {
            assertThat(reader.readString(StringFormat.BYTE_LENGTH)).isEqualTo("hello");
        }
    }

    @Test
    void testReadStringShortLength() throws IOException {
        try (var reader = bytes(0x03, 0x00, 'a', 'b', 'c')) {
            assertThat(reader.readString(StringFormat.SHORT_LENGTH)).isEqualTo("abc");
        }
    }

    @Test
    void testReadStringIntLength() throws IOException {
        try (var reader = bytes(0x02, 0x00, 0x00, 0x00, 'h', 'i')) {
            assertThat(reader.readString(StringFormat.INT_LENGTH)).isEqualTo("hi");
        }
    }

    @Test
    void testReadStringNullTerminatedUtf8() throws IOException {
        try (var reader = bytes('a', 'b', 'c', 0x00, 'x')) {
            assertThat(reader.readString(StringFormat.NULL_TERM)).isEqualTo("abc");
            assertThat(reader.position()).isEqualTo(4);
        }
    }

    @Test
    void testReadStringNullTerminatedUtf16() throws IOException {
        try (var reader = bytes('a', 0x00, 'b', 0x00, 0x00, 0x00)) {
            assertThat(reader.readString(StringFormat.NULL_TERM, StandardCharsets.UTF_16LE)).isEqualTo("ab");
        }
    }

    @Test
    void testReadStringFixedLength() throws IOException {
        try (var reader = bytes('h', 'i')) {
            assertThat(reader.readString(2)).isEqualTo("hi");
        }
    }

    @Test
    void testReadStringFixedLengthZeroIsEmpty() throws IOException {
        try (var reader = bytes('a', 'b')) {
            assertThat(reader.readString(0)).isEmpty();
            assertThat(reader.position()).isZero();
        }
    }

    @Test
    void testReadCStringDefaultsToUtf8AndIgnoresGarbageAfterNull() throws IOException {
        try (var reader = bytes('c', 'a', 'f', 0xC3, 0xA9, 0x00, 0xCD, 0xCD, 0x01, 0x00, 0x00, 0x00)) {
            assertThat(reader.readCString(8)).isEqualTo("café");
            assertThat(reader.position()).isEqualTo(8);
            assertThat(reader.readInt()).isEqualTo(1);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cStringNullPositions")
    void testReadCStringNullPosition(String name, byte[] data, String expected) throws IOException {
        try (var reader = BinaryReader.wrap(Bytes.wrap(data))) {
            assertThat(reader.readCString(data.length)).isEqualTo(expected);
            assertThat(reader.position()).isEqualTo(data.length);
        }
    }

    static List<Arguments> cStringNullPositions() {
        return List.of(
            Arguments.of("no null uses whole buffer", new byte[]{'a', 'b', 'c'}, "abc"),
            Arguments.of("leading null is empty", new byte[]{0x00, 'a', 'b', 'c'}, ""),
            Arguments.of("zero size is empty", new byte[0], "")
        );
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"UTF-8", "UTF-16LE", "UTF-16BE", "UTF-32LE"})
    void testReadCStringTerminatorWidth(String charsetName) throws IOException {
        var charset = Charset.forName(charsetName);
        var data = "AB\0Z".getBytes(charset);
        try (var reader = BinaryReader.wrap(Bytes.wrap(data))) {
            assertThat(reader.readCString(data.length, charset)).isEqualTo("AB");
            assertThat(reader.position()).isEqualTo(data.length);
        }
    }

    @Test
    void testReadCStringUtf16IgnoresMisalignedZeroPair() throws IOException {
        // 'A' (41 00) followed by U+0100 (00 01): bytes 1..2 are 00 00 but straddle two code units
        try (var reader = bytes(0x41, 0x00, 0x00, 0x01, 0x00, 0x00)) {
            assertThat(reader.readCString(6, StandardCharsets.UTF_16LE)).isEqualTo("AĀ");
        }
    }

    @Test
    void testReadCStringUtf16WithOddSizeDoesNotReadPastBuffer() throws IOException {
        try (var reader = bytes('A', 0x00, 'B', 0x2A)) {
            assertThat(reader.readCString(3, StandardCharsets.UTF_16LE)).startsWith("A");
            assertThat(reader.position()).isEqualTo(3);
            assertThat(reader.readByte()).isEqualTo((byte) 0x2A);
        }
    }

    @Test
    void testReadCStringBeyondEndThrowsWithoutConsuming() throws IOException {
        try (var reader = bytes('a', 'b')) {
            assertThatExceptionOfType(EOFException.class)
                .isThrownBy(() -> reader.readCString(3));
            assertThat(reader.position()).isZero();
        }
    }

    @Test
    void testReadStringIntLengthWithNegativeFromStreamThrows() throws IOException {
        try (var reader = bytes(0xFF, 0xFF, 0xFF, 0xFF)) {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> reader.readString(StringFormat.INT_LENGTH));
        }
    }

    @Test
    void testReadStringsList() throws IOException {
        try (var reader = bytes(0x01, 'a', 0x01, 'b')) {
            var strings = reader.readStrings(2, StringFormat.BYTE_LENGTH);
            assertThat(strings).containsExactly("a", "b");
        }
    }

    @Test
    void testReadObject() throws IOException {
        try (var reader = bytes(0x05, 0x00, 0x00, 0x00)) {
            assertThat(reader.readObject(BinaryReader::readInt)).isEqualTo(5);
        }
    }

    @Test
    void testReadObjects() throws IOException {
        try (var reader = bytes(0x01, 0x00, 0x02, 0x00, 0x03, 0x00)) {
            var values = reader.readObjects(3, BinaryReader::readShort);
            assertThat(values).containsExactly((short) 1, (short) 2, (short) 3);
        }
    }

    @Test
    void testReadObjectsZeroCountReturnsEmpty() throws IOException {
        try (var reader = bytes(0x01, 0x02)) {
            assertThat(reader.readObjects(0, BinaryReader::readByte)).isEmpty();
        }
    }

    @Test
    void testExpectByteSucceeds() throws IOException {
        try (var reader = bytes(0x42)) {
            assertThatNoException().isThrownBy(() -> reader.expectByte((byte) 0x42));
        }
    }

    @Test
    void testExpectByteMismatchThrows() throws IOException {
        try (var reader = bytes(0x42)) {
            assertThatIOException().isThrownBy(() -> reader.expectByte((byte) 0x43));
        }
    }

    @Test
    void testExpectShortMismatchThrows() throws IOException {
        try (var reader = bytes(0x01, 0x00)) {
            assertThatIOException().isThrownBy(() -> reader.expectShort((short) 0x1234));
        }
    }

    @Test
    void testExpectIntMismatchThrows() throws IOException {
        try (var reader = bytes(0x01, 0x00, 0x00, 0x00)) {
            assertThatIOException().isThrownBy(() -> reader.expectInt(0x12345678));
        }
    }

    @Test
    void testExpectLongMismatchThrows() throws IOException {
        try (var reader = bytes(0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)) {
            assertThatIOException().isThrownBy(() -> reader.expectLong(0x12345678L));
        }
    }

    @Test
    void testExpectFloatMatchesNaN() throws IOException {
        var bits = Float.floatToRawIntBits(Float.NaN);
        try (var reader = bytes(bits & 0xFF, (bits >>> 8) & 0xFF, (bits >>> 16) & 0xFF, (bits >>> 24) & 0xFF)) {
            assertThatNoException().isThrownBy(() -> reader.expectFloat(Float.NaN));
        }
    }

    @Test
    void testExpectDoubleHandlesNaN() throws IOException {
        var bits = Double.doubleToRawLongBits(Double.NaN);
        var data = new int[8];
        for (var i = 0; i < 8; i++) {
            data[i] = (int) ((bits >>> (i * 8)) & 0xFF);
        }
        try (var reader = bytes(data)) {
            assertThatNoException().isThrownBy(() -> reader.expectDouble(Double.NaN));
        }
    }

    @Test
    void testExpectEndAtEnd() throws IOException {
        try (var reader = bytes(0x01)) {
            reader.readByte();
            assertThatNoException().isThrownBy(reader::expectEnd);
        }
    }

    @Test
    void testExpectEndThrowsIfRemaining() throws IOException {
        try (var reader = bytes(0x01, 0x02)) {
            reader.readByte();
            assertThatIOException().isThrownBy(reader::expectEnd);
        }
    }

    @Test
    void testExpectZerosConsumesZeros() throws IOException {
        try (var reader = bytes(0x00, 0x00, 0x00, 0x05)) {
            reader.expectZeros(0);
            reader.expectZeros(3);
            assertThat(reader.position()).isEqualTo(3);
        }
    }

    @Test
    void testExpectZerosReportsPositionOfNonZeroByte() throws IOException {
        try (var reader = bytes(0x00, 0x00, 0x07, 0x00)) {
            assertThatIOException()
                .isThrownBy(() -> reader.expectZeros(4))
                .withMessageContaining("position 2");
        }
    }

    @Test
    void testExpectZerosBeyondRemainingThrowsWithoutConsuming() throws IOException {
        try (var reader = bytes(0x00, 0x00)) {
            assertThatExceptionOfType(EOFException.class).isThrownBy(() -> reader.expectZeros(3));
            assertThat(reader.position()).isZero();
        }
    }

    @Test
    void testExpectZerosRejectsNegative() throws IOException {
        try (var reader = bytes(0x00)) {
            assertThatIllegalArgumentException().isThrownBy(() -> reader.expectZeros(-1));
        }
    }

    @Test
    void testExpectPosition() throws IOException {
        try (var reader = bytes(0x01, 0x02, 0x03)) {
            reader.readByte();
            assertThatNoException().isThrownBy(() -> reader.expectPosition(1));
            assertThatIOException().isThrownBy(() -> reader.expectPosition(2));
            assertThat(reader.position()).isEqualTo(1);
        }
    }

    @Test
    void testEnsureRemainingSufficient() throws IOException {
        try (var reader = bytes(0x01, 0x02, 0x03)) {
            assertThatNoException().isThrownBy(() -> reader.ensureRemaining(3));
        }
    }

    @Test
    void testEnsureRemainingInsufficientThrows() throws IOException {
        try (var reader = bytes(0x01, 0x02)) {
            assertThatIOException().isThrownBy(() -> reader.ensureRemaining(5));
        }
    }

    @Test
    void testReadByteAtEndThrowsEofForBytesReader() throws IOException {
        try (var reader = bytes(0x01)) {
            reader.readByte();
            assertThatExceptionOfType(IOException.class)
                .isThrownBy(reader::readByte);
        }
    }

    @Test
    void testOrderRoundtrip() throws IOException {
        try (var reader = bytes(0x00)) {
            assertThat(reader.order()).isEqualTo(ByteOrder.LITTLE_ENDIAN);
            reader.order(ByteOrder.BIG_ENDIAN);
            assertThat(reader.order()).isEqualTo(ByteOrder.BIG_ENDIAN);
            reader.order(ByteOrder.LITTLE_ENDIAN);
            assertThat(reader.order()).isEqualTo(ByteOrder.LITTLE_ENDIAN);
        }
    }

    @Test
    void testOrderRejectsNull() throws IOException {
        try (var reader = bytes(0x00)) {
            assertThatNullPointerException().isThrownBy(() -> reader.order(null));
        }
    }

    @Test
    void testWrapRejectsNullBytes() {
        assertThatNullPointerException().isThrownBy(() -> BinaryReader.wrap(null));
    }

    @Test
    void testFileSourceReadsSequentially(@TempDir Path tempDir) throws IOException {
        var path = tempDir.resolve("test.bin");
        Files.write(path, new byte[]{0x01, 0x02, 0x03, 0x04, 0x05});
        try (var reader = BinaryReader.open(path)) {
            assertThat(reader.size()).isEqualTo(5);
            assertThat(reader.readByte()).isEqualTo((byte) 0x01);
            assertThat(reader.readByte()).isEqualTo((byte) 0x02);
            assertThat(reader.readByte()).isEqualTo((byte) 0x03);
            assertThat(reader.readByte()).isEqualTo((byte) 0x04);
            assertThat(reader.readByte()).isEqualTo((byte) 0x05);
        }
    }

    @Test
    void testFileSourceRepeatedReadPastEndStayClean(@TempDir Path tempDir) throws IOException {
        var path = tempDir.resolve("eof.bin");
        Files.write(path, new byte[]{0x01});
        try (var reader = BinaryReader.open(path)) {
            reader.readByte();
            assertThatExceptionOfType(EOFException.class).isThrownBy(reader::readByte);
            assertThatExceptionOfType(EOFException.class).isThrownBy(reader::readByte);
        }
    }

    @Test
    void testFileSourcePositionThenReadAfterEof(@TempDir Path tempDir) throws IOException {
        var path = tempDir.resolve("eof2.bin");
        Files.write(path, new byte[]{0x01, 0x02, 0x03});
        try (var reader = BinaryReader.open(path)) {
            reader.position(3);
            assertThatExceptionOfType(EOFException.class).isThrownBy(reader::readByte);
            reader.position(0);
            assertThat(reader.readByte()).isEqualTo((byte) 0x01);
        }
    }

    @Test
    void testBufferedRefillReturnsPartialAtEof() {
        try (var reader = new TestBufferedReader(new byte[]{1, 2, 3, 4})) {
            assertThatExceptionOfType(EOFException.class).isThrownBy(reader::readLong);
            assertThatExceptionOfType(EOFException.class).isThrownBy(reader::readLong);
        }
    }

    @Test
    void testSequenceReadsAcrossSources() throws IOException {
        var first = bytes(0x01, 0x02);
        var second = bytes(0x03, 0x04);
        var third = bytes(0x05, 0x06);
        try (var seq = BinaryReader.sequence(List.of(first, second, third))) {
            assertThat(seq.size()).isEqualTo(6);
            for (var i = 1; i <= 6; i++) {
                assertThat(seq.readByte()).as("byte %d", i).isEqualTo((byte) i);
            }
        }
    }

    @Test
    void testSequenceWithEmptyMembersStillReadsCleanly() throws IOException {
        try (var seq = BinaryReader.sequence(List.of(
            bytes(0x01, 0x02),
            bytes(),
            bytes(0x03, 0x04),
            bytes(),
            bytes(0x05)))) {
            assertThat(seq.size()).isEqualTo(5);
            for (var i = 1; i <= 5; i++) {
                assertThat(seq.readByte()).as("byte %d", i).isEqualTo((byte) i);
            }
        }
    }

    @Test
    void testSequenceReadPastEndThrowsCleanly() throws IOException {
        try (var seq = BinaryReader.sequence(List.of(bytes(0x01, 0x02), bytes(0x03)))) {
            seq.readByte();
            seq.readByte();
            seq.readByte();
            assertThatExceptionOfType(EOFException.class).isThrownBy(seq::readByte);
        }
    }


    @Test
    void testSliceOnBytesReader() throws IOException {
        try (var reader = bytes(0x00, 0x11, 0x22, 0x33, 0x44, 0x55)) {
            var slice = reader.slice(2, 3);
            assertThat(slice.size()).isEqualTo(3);
            assertThat(slice.position()).isZero();
            assertThat(slice.readByte()).isEqualTo((byte) 0x22);
            assertThat(slice.readByte()).isEqualTo((byte) 0x33);
            assertThat(slice.readByte()).isEqualTo((byte) 0x44);
            assertThatExceptionOfType(EOFException.class).isThrownBy(slice::readByte);
        }
    }

    @Test
    void testSliceDoesNotAffectParentPosition() throws IOException {
        try (var reader = bytes(0x00, 0x11, 0x22, 0x33, 0x44, 0x55)) {
            reader.readByte();
            var slice = reader.slice(2, 3);
            slice.readByte();
            slice.readByte();
            assertThat(reader.position()).isEqualTo(1);
            assertThat(reader.readByte()).isEqualTo((byte) 0x11);
        }
    }

    @Test
    void testSliceFullRange() throws IOException {
        try (var reader = bytes(0x01, 0x02, 0x03)) {
            var slice = reader.slice(0, 3);
            assertThat(slice.size()).isEqualTo(3);
            assertThat(slice.readByte()).isEqualTo((byte) 0x01);
            assertThat(slice.readByte()).isEqualTo((byte) 0x02);
            assertThat(slice.readByte()).isEqualTo((byte) 0x03);
        }
    }

    @Test
    void testSliceEmpty() throws IOException {
        try (var reader = bytes(0x01, 0x02, 0x03)) {
            var slice = reader.slice(1, 0);
            assertThat(slice.size()).isZero();
            assertThat(slice.remaining()).isZero();
            assertThatExceptionOfType(EOFException.class).isThrownBy(slice::readByte);
        }
    }

    @Test
    void testSliceRejectsNegativeOffset() throws IOException {
        try (var reader = bytes(0x01, 0x02)) {
            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> reader.slice(-1, 1));
        }
    }

    @Test
    void testSliceRejectsRangeBeyondEnd() throws IOException {
        try (var reader = bytes(0x01, 0x02)) {
            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> reader.slice(1, 5));
        }
    }

    @Test
    void testSliceOfSlice() throws IOException {
        try (var reader = bytes(0x00, 0x11, 0x22, 0x33, 0x44, 0x55)) {
            var slice1 = reader.slice(1, 4);
            var slice2 = slice1.slice(1, 2);
            assertThat(slice2.readByte()).isEqualTo((byte) 0x22);
            assertThat(slice2.readByte()).isEqualTo((byte) 0x33);
            assertThatExceptionOfType(EOFException.class).isThrownBy(slice2::readByte);
        }
    }

    @Test
    void testSliceOnFileReader(@TempDir Path tempDir) throws IOException {
        var path = tempDir.resolve("slice.bin");
        Files.write(path, new byte[]{0x10, 0x20, 0x30, 0x40, 0x50, 0x60});
        try (var reader = BinaryReader.open(path)) {
            var slice = reader.slice(2, 3);
            assertThat(slice.size()).isEqualTo(3);
            assertThat(slice.readByte()).isEqualTo((byte) 0x30);
            assertThat(slice.readByte()).isEqualTo((byte) 0x40);
            assertThat(slice.readByte()).isEqualTo((byte) 0x50);
            assertThat(reader.position()).isZero();
            assertThat(reader.readByte()).isEqualTo((byte) 0x10);
        }
    }

    @Test
    void testSliceOnSequenceSpansSubSources() throws IOException {
        try (var seq = BinaryReader.sequence(java.util.List.of(
            bytes(0x10, 0x20, 0x30),
            bytes(0x40, 0x50, 0x60)))) {
            var slice = seq.slice(2, 3);
            assertThat(slice.readByte()).isEqualTo((byte) 0x30);
            assertThat(slice.readByte()).isEqualTo((byte) 0x40);
            assertThat(slice.readByte()).isEqualTo((byte) 0x50);
        }
    }

    @Test
    void testInterleavedSlicesOnFileSourceDontInterfere(@TempDir Path tempDir) throws IOException {
        var path = tempDir.resolve("interleave.bin");
        Files.write(path, new byte[]{0x10, 0x20, 0x30, 0x40, 0x50, 0x60});
        try (var reader = BinaryReader.open(path)) {
            var sliceA = reader.slice(0, 3);
            var sliceB = reader.slice(3, 3);
            assertThat(sliceA.readByte()).isEqualTo((byte) 0x10);
            assertThat(sliceB.readByte()).isEqualTo((byte) 0x40);
            assertThat(sliceA.readByte()).isEqualTo((byte) 0x20);
            assertThat(sliceB.readByte()).isEqualTo((byte) 0x50);
            assertThat(sliceA.readByte()).isEqualTo((byte) 0x30);
            assertThat(sliceB.readByte()).isEqualTo((byte) 0x60);
        }
    }

    @Test
    void testCloseSliceDoesNotClosePartFileReader(@TempDir Path tempDir) throws IOException {
        var path = tempDir.resolve("slice-close.bin");
        Files.write(path, new byte[]{0x10, 0x20, 0x30, 0x40});
        try (var reader = BinaryReader.open(path)) {
            var slice = reader.slice(0, 2);
            slice.close();
            assertThat(reader.readByte()).isEqualTo((byte) 0x10);
        }
    }

    @Test
    void testSliceInheritsByteOrder() throws IOException {
        try (var reader = bytes(0x00, 0x00, 0x00, 0x01)) {
            reader.order(ByteOrder.BIG_ENDIAN);
            var slice = reader.slice(0, 4);
            assertThat(slice.order()).isEqualTo(ByteOrder.BIG_ENDIAN);
            assertThat(slice.readInt()).isEqualTo(1);
        }
    }

    @Test
    void testSliceOnFileSourceInheritsByteOrder(@TempDir Path tempDir) throws IOException {
        var path = tempDir.resolve("slice-order.bin");
        Files.write(path, new byte[]{0x00, 0x00, 0x00, 0x00, 0x01});
        try (var reader = BinaryReader.open(path)) {
            reader.order(ByteOrder.BIG_ENDIAN);
            var slice = reader.slice(1, 4);
            assertThat(slice.order()).isEqualTo(ByteOrder.BIG_ENDIAN);
            assertThat(slice.readInt()).isEqualTo(1);
        }
    }

    @Test
    void testSliceByteOrderIsIndependentAfterCreation() throws IOException {
        try (var reader = bytes(0x00, 0x01)) {
            reader.order(ByteOrder.BIG_ENDIAN);
            var slice = reader.slice(0, 2);
            reader.order(ByteOrder.LITTLE_ENDIAN);
            assertThat(slice.order()).isEqualTo(ByteOrder.BIG_ENDIAN);
            slice.order(ByteOrder.LITTLE_ENDIAN);
            assertThat(slice.readShort()).isEqualTo((short) 0x0100);
            reader.order(ByteOrder.BIG_ENDIAN);
            assertThat(reader.readShort()).isEqualTo((short) 0x0001);
        }
    }

    @Test
    void testReadSliceAdvancesParent() throws IOException {
        try (var reader = bytes(0x00, 0x11, 0x22, 0x33, 0x44, 0x55)) {
            reader.readByte();
            var slice = reader.readSlice(3);
            assertThat(reader.position()).isEqualTo(4);
            assertThat(slice.size()).isEqualTo(3);
            assertThat(slice.position()).isZero();
            assertThat(slice.readByte()).isEqualTo((byte) 0x11);
            assertThat(reader.readByte()).isEqualTo((byte) 0x44);
        }
    }

    @Test
    void testReadSliceOnFileSourceInheritsByteOrder(@TempDir Path tempDir) throws IOException {
        var path = tempDir.resolve("read-slice.bin");
        Files.write(path, new byte[]{0x7F, 0x00, 0x00, 0x00, 0x01, 0x10});
        try (var reader = BinaryReader.open(path)) {
            reader.order(ByteOrder.BIG_ENDIAN);
            reader.readByte();
            var slice = reader.readSlice(4);
            assertThat(reader.readByte()).isEqualTo((byte) 0x10);
            assertThat(slice.readInt()).isEqualTo(1);
        }
    }

    @Test
    void testReadSliceBeyondRemainingThrowsWithoutConsuming() throws IOException {
        try (var reader = bytes(0x01, 0x02, 0x03)) {
            reader.readByte();
            assertThatExceptionOfType(EOFException.class).isThrownBy(() -> reader.readSlice(3));
            assertThat(reader.position()).isEqualTo(1);
        }
    }

    private static BinaryReader bytes(int... values) {
        var data = Bytes.Mutable.allocate(values.length);
        for (var i = 0; i < values.length; i++) {
            data.set(i, (byte) values[i]);
        }
        return BinaryReader.wrap(data);
    }

    private static final class TestBufferedReader extends BufferedBinaryReader {
        private final byte[] data;

        TestBufferedReader(byte[] data) {
            super(data.length);
            this.data = data;
        }

        @Override
        int readImpl(Bytes.Mutable target, long position) {
            int available = (int) Math.min(target.length(), data.length - position);
            if (available <= 0) {
                return 0;
            }
            for (var i = 0; i < available; i++) {
                target.set(i, data[(int) (position + i)]);
            }
            return available;
        }

        @Override
        public void close() {
        }
    }
}
