package wtf.reversed.toolbox.io;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.*;
import wtf.reversed.toolbox.collect.*;

import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

class BinarySourceTest {
    @Test
    void testInitialPositionIsZero() throws IOException {
        try (var source = bytes(0x01, 0x02, 0x03)) {
            assertThat(source.position()).isZero();
            assertThat(source.size()).isEqualTo(3);
            assertThat(source.remaining()).isEqualTo(3);
        }
    }

    @Test
    void testRemainingDecreasesAsBytesRead() throws IOException {
        try (var source = bytes(0x01, 0x02, 0x03)) {
            source.readByte();
            assertThat(source.remaining()).isEqualTo(2);
            source.readByte();
            assertThat(source.remaining()).isEqualTo(1);
            source.readByte();
            assertThat(source.remaining()).isZero();
        }
    }

    @Test
    void testPositionSetAdvancesCursor() throws IOException {
        try (var source = bytes(0x01, 0x02, 0x03, 0x04)) {
            source.position(2);
            assertThat(source.position()).isEqualTo(2);
            assertThat(source.readByte()).isEqualTo((byte) 0x03);
        }
    }

    @Test
    void testPositionRejectsNegative() throws IOException {
        try (var source = bytes(0x01, 0x02)) {
            assertThatIllegalArgumentException().isThrownBy(() -> source.position(-1));
        }
    }

    @Test
    void testPositionRejectsBeyondSize() throws IOException {
        try (var source = bytes(0x01, 0x02)) {
            assertThatIllegalArgumentException().isThrownBy(() -> source.position(3));
        }
    }

    @Test
    void testPositionAtSizeIsAllowed() throws IOException {
        try (var source = bytes(0x01, 0x02)) {
            source.position(2);
            assertThat(source.position()).isEqualTo(2);
            assertThat(source.remaining()).isZero();
        }
    }

    @Test
    void testSkipAdvancesCursor() throws IOException {
        try (var source = bytes(0x01, 0x02, 0x03, 0x04)) {
            source.skip(2);
            assertThat(source.position()).isEqualTo(2);
            assertThat(source.readByte()).isEqualTo((byte) 0x03);
        }
    }

    @Test
    void testSkipRejectsNegative() throws IOException {
        try (var source = bytes(0x01, 0x02)) {
            assertThatIllegalArgumentException().isThrownBy(() -> source.skip(-1));
        }
    }

    @Test
    void testReadShortLittleEndianByDefault() throws IOException {
        try (var source = bytes(0x34, 0x12)) {
            assertThat(source.order()).isEqualTo(ByteOrder.LITTLE_ENDIAN);
            assertThat(source.readShort()).isEqualTo((short) 0x1234);
        }
    }

    @Test
    void testReadShortBigEndian() throws IOException {
        try (var source = bytes(0x12, 0x34)) {
            source.order(ByteOrder.BIG_ENDIAN);
            assertThat(source.readShort()).isEqualTo((short) 0x1234);
        }
    }

    @Test
    void testReadIntBothEndians() throws IOException {
        try (var source = bytes(0x78, 0x56, 0x34, 0x12, 0x12, 0x34, 0x56, 0x78)) {
            assertThat(source.readInt()).isEqualTo(0x12345678);
            source.order(ByteOrder.BIG_ENDIAN);
            assertThat(source.readInt()).isEqualTo(0x12345678);
        }
    }

    @Test
    void testReadLongBothEndians() throws IOException {
        try (var source = bytes(
            0x07, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01, 0x00,
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07)) {
            assertThat(source.readLong()).isEqualTo(0x0001020304050607L);
            source.order(ByteOrder.BIG_ENDIAN);
            assertThat(source.readLong()).isEqualTo(0x0001020304050607L);
        }
    }

    @Test
    void testReadFloat() throws IOException {
        var bits = Float.floatToRawIntBits(1.5f);
        try (var source = bytes(bits & 0xFF, (bits >>> 8) & 0xFF, (bits >>> 16) & 0xFF, (bits >>> 24) & 0xFF)) {
            assertThat(source.readFloat()).isEqualTo(1.5f);
        }
    }

    @Test
    void testReadDouble() throws IOException {
        var bits = Double.doubleToRawLongBits(1.5);
        var data = new int[8];
        for (var i = 0; i < 8; i++) {
            data[i] = (int) ((bits >>> (i * 8)) & 0xFF);
        }
        try (var source = bytes(data)) {
            assertThat(source.readDouble()).isEqualTo(1.5);
        }
    }

    @Test
    void testReadHalfRoundtripsToFloat() throws IOException {
        try (var source = bytes(0x00, 0x3C)) {
            assertThat(source.readHalf()).isEqualTo(1.0f);
        }
    }

    @Test
    void testReadBytesZeroReturnsEmpty() throws IOException {
        try (var source = bytes(0x01, 0x02)) {
            assertThat(source.readBytes(0).length()).isZero();
            assertThat(source.position()).isZero();
        }
    }

    @Test
    void testReadBytesCount() throws IOException {
        try (var source = bytes(0x01, 0x02, 0x03, 0x04)) {
            var read = source.readBytes(3);
            assertThat(read.length()).isEqualTo(3);
            assertThat(read.get(0)).isEqualTo((byte) 0x01);
            assertThat(read.get(1)).isEqualTo((byte) 0x02);
            assertThat(read.get(2)).isEqualTo((byte) 0x03);
            assertThat(source.position()).isEqualTo(3);
        }
    }

    @Test
    void testReadBytesNegativeCountRejected() throws IOException {
        try (var source = bytes(0x01, 0x02)) {
            assertThatIllegalArgumentException().isThrownBy(() -> source.readBytes(-1));
        }
    }

    @Test
    void testReadBytesBeyondRemainingThrows() throws IOException {
        try (var source = bytes(0x01, 0x02)) {
            assertThatIOException().isThrownBy(() -> source.readBytes(5));
        }
    }

    @Test
    void testReadShortsCount() throws IOException {
        try (var source = bytes(0x01, 0x00, 0x02, 0x00, 0x03, 0x00)) {
            var read = source.readShorts(3);
            assertThat(read.length()).isEqualTo(3);
            assertThat(read.get(0)).isEqualTo((short) 1);
            assertThat(read.get(1)).isEqualTo((short) 2);
            assertThat(read.get(2)).isEqualTo((short) 3);
        }
    }

    @Test
    void testReadIntsCount() throws IOException {
        try (var source = bytes(0x01, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00)) {
            var read = source.readInts(2);
            assertThat(read.length()).isEqualTo(2);
            assertThat(read.get(0)).isEqualTo(1);
            assertThat(read.get(1)).isEqualTo(2);
        }
    }

    @Test
    void testReadLongsCount() throws IOException {
        try (var source = bytes(
            0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)) {
            var read = source.readLongs(2);
            assertThat(read.length()).isEqualTo(2);
            assertThat(read.get(0)).isEqualTo(1L);
            assertThat(read.get(1)).isEqualTo(2L);
        }
    }

    @Test
    void testReadLongAsIntFitsInRange() throws IOException {
        try (var source = bytes(0x05, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)) {
            assertThat(source.readLongAsInt()).isEqualTo(5);
        }
    }

    @Test
    void testReadLongAsIntOverflowThrows() throws IOException {
        try (var source = bytes(0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00)) {
            assertThatExceptionOfType(ArithmeticException.class)
                .isThrownBy(source::readLongAsInt);
        }
    }

    @Test
    void testReadBoolByteFalse() throws IOException {
        try (var source = bytes(0x00)) {
            assertThat(source.readBool(BoolFormat.BYTE)).isFalse();
        }
    }

    @Test
    void testReadBoolByteTrue() throws IOException {
        try (var source = bytes(0x01)) {
            assertThat(source.readBool(BoolFormat.BYTE)).isTrue();
        }
    }

    @Test
    void testReadBoolShort() throws IOException {
        try (var source = bytes(0x01, 0x00)) {
            assertThat(source.readBool(BoolFormat.SHORT)).isTrue();
        }
    }

    @Test
    void testReadBoolInt() throws IOException {
        try (var source = bytes(0x00, 0x00, 0x00, 0x00)) {
            assertThat(source.readBool(BoolFormat.INT)).isFalse();
        }
    }

    @Test
    void testReadBoolInvalidValueThrows() throws IOException {
        try (var source = bytes(0x02)) {
            assertThatIOException().isThrownBy(() -> source.readBool(BoolFormat.BYTE));
        }
    }

    @Test
    void testReadStringByteLength() throws IOException {
        try (var source = bytes(0x05, 'h', 'e', 'l', 'l', 'o')) {
            assertThat(source.readString(StringFormat.BYTE_LENGTH)).isEqualTo("hello");
        }
    }

    @Test
    void testReadStringShortLength() throws IOException {
        try (var source = bytes(0x03, 0x00, 'a', 'b', 'c')) {
            assertThat(source.readString(StringFormat.SHORT_LENGTH)).isEqualTo("abc");
        }
    }

    @Test
    void testReadStringIntLength() throws IOException {
        try (var source = bytes(0x02, 0x00, 0x00, 0x00, 'h', 'i')) {
            assertThat(source.readString(StringFormat.INT_LENGTH)).isEqualTo("hi");
        }
    }

    @Test
    void testReadStringNullTerminatedUtf8() throws IOException {
        try (var source = bytes('a', 'b', 'c', 0x00, 'x')) {
            assertThat(source.readString(StringFormat.NULL_TERM)).isEqualTo("abc");
            assertThat(source.position()).isEqualTo(4);
        }
    }

    @Test
    void testReadStringNullTerminatedUtf16() throws IOException {
        try (var source = bytes('a', 0x00, 'b', 0x00, 0x00, 0x00)) {
            assertThat(source.readString(StringFormat.NULL_TERM, StandardCharsets.UTF_16LE)).isEqualTo("ab");
        }
    }

    @Test
    void testReadStringFixedLength() throws IOException {
        try (var source = bytes('h', 'i')) {
            assertThat(source.readString(2)).isEqualTo("hi");
        }
    }

    @Test
    void testReadStringFixedLengthZeroIsEmpty() throws IOException {
        try (var source = bytes('a', 'b')) {
            assertThat(source.readString(0)).isEmpty();
            assertThat(source.position()).isZero();
        }
    }

    @Test
    void testReadStringIntLengthWithNegativeFromStreamThrows() throws IOException {
        try (var source = bytes(0xFF, 0xFF, 0xFF, 0xFF)) {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> source.readString(StringFormat.INT_LENGTH));
        }
    }

    @Test
    void testReadStringsList() throws IOException {
        try (var source = bytes(0x01, 'a', 0x01, 'b')) {
            var strings = source.readStrings(2, StringFormat.BYTE_LENGTH);
            assertThat(strings).containsExactly("a", "b");
        }
    }

    @Test
    void testReadObject() throws IOException {
        try (var source = bytes(0x05, 0x00, 0x00, 0x00)) {
            assertThat(source.readObject(BinarySource::readInt)).isEqualTo(5);
        }
    }

    @Test
    void testReadObjects() throws IOException {
        try (var source = bytes(0x01, 0x00, 0x02, 0x00, 0x03, 0x00)) {
            var values = source.readObjects(3, BinarySource::readShort);
            assertThat(values).containsExactly((short) 1, (short) 2, (short) 3);
        }
    }

    @Test
    void testReadObjectsZeroCountReturnsEmpty() throws IOException {
        try (var source = bytes(0x01, 0x02)) {
            assertThat(source.readObjects(0, BinarySource::readByte)).isEmpty();
        }
    }

    @Test
    void testExpectByteSucceeds() throws IOException {
        try (var source = bytes(0x42)) {
            assertThatNoException().isThrownBy(() -> source.expectByte((byte) 0x42));
        }
    }

    @Test
    void testExpectByteMismatchThrows() throws IOException {
        try (var source = bytes(0x42)) {
            assertThatIOException().isThrownBy(() -> source.expectByte((byte) 0x43));
        }
    }

    @Test
    void testExpectShortMismatchThrows() throws IOException {
        try (var source = bytes(0x01, 0x00)) {
            assertThatIOException().isThrownBy(() -> source.expectShort((short) 0x1234));
        }
    }

    @Test
    void testExpectIntMismatchThrows() throws IOException {
        try (var source = bytes(0x01, 0x00, 0x00, 0x00)) {
            assertThatIOException().isThrownBy(() -> source.expectInt(0x12345678));
        }
    }

    @Test
    void testExpectLongMismatchThrows() throws IOException {
        try (var source = bytes(0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)) {
            assertThatIOException().isThrownBy(() -> source.expectLong(0x12345678L));
        }
    }

    @Test
    void testExpectFloatMatchesNaN() throws IOException {
        var bits = Float.floatToRawIntBits(Float.NaN);
        try (var source = bytes(bits & 0xFF, (bits >>> 8) & 0xFF, (bits >>> 16) & 0xFF, (bits >>> 24) & 0xFF)) {
            assertThatNoException().isThrownBy(() -> source.expectFloat(Float.NaN));
        }
    }

    @Test
    void testExpectDoubleHandlesNaN() throws IOException {
        var bits = Double.doubleToRawLongBits(Double.NaN);
        var data = new int[8];
        for (var i = 0; i < 8; i++) {
            data[i] = (int) ((bits >>> (i * 8)) & 0xFF);
        }
        try (var source = bytes(data)) {
            assertThatNoException().isThrownBy(() -> source.expectDouble(Double.NaN));
        }
    }

    @Test
    void testExpectEndAtEnd() throws IOException {
        try (var source = bytes(0x01)) {
            source.readByte();
            assertThatNoException().isThrownBy(source::expectEnd);
        }
    }

    @Test
    void testExpectEndThrowsIfRemaining() throws IOException {
        try (var source = bytes(0x01, 0x02)) {
            source.readByte();
            assertThatIOException().isThrownBy(source::expectEnd);
        }
    }

    @Test
    void testEnsureRemainingSufficient() throws IOException {
        try (var source = bytes(0x01, 0x02, 0x03)) {
            assertThatNoException().isThrownBy(() -> source.ensureRemaining(3));
        }
    }

    @Test
    void testEnsureRemainingInsufficientThrows() throws IOException {
        try (var source = bytes(0x01, 0x02)) {
            assertThatIOException().isThrownBy(() -> source.ensureRemaining(5));
        }
    }

    @Test
    void testReadByteAtEndThrowsEofForBytesSource() throws IOException {
        try (var source = bytes(0x01)) {
            source.readByte();
            assertThatExceptionOfType(IOException.class)
                .isThrownBy(source::readByte);
        }
    }

    @Test
    void testOrderRoundtrip() throws IOException {
        try (var source = bytes(0x00)) {
            assertThat(source.order()).isEqualTo(ByteOrder.LITTLE_ENDIAN);
            source.order(ByteOrder.BIG_ENDIAN);
            assertThat(source.order()).isEqualTo(ByteOrder.BIG_ENDIAN);
            source.order(ByteOrder.LITTLE_ENDIAN);
            assertThat(source.order()).isEqualTo(ByteOrder.LITTLE_ENDIAN);
        }
    }

    @Test
    void testOrderRejectsNull() throws IOException {
        try (var source = bytes(0x00)) {
            assertThatNullPointerException().isThrownBy(() -> source.order(null));
        }
    }

    @Test
    void testWrapRejectsNullBytes() {
        assertThatNullPointerException().isThrownBy(() -> BinarySource.wrap(null));
    }

    @Test
    void testFileSourceReadsSequentially(@TempDir Path tempDir) throws IOException {
        var path = tempDir.resolve("test.bin");
        Files.write(path, new byte[]{0x01, 0x02, 0x03, 0x04, 0x05});
        try (var source = BinarySource.open(path)) {
            assertThat(source.size()).isEqualTo(5);
            assertThat(source.readByte()).isEqualTo((byte) 0x01);
            assertThat(source.readByte()).isEqualTo((byte) 0x02);
            assertThat(source.readByte()).isEqualTo((byte) 0x03);
            assertThat(source.readByte()).isEqualTo((byte) 0x04);
            assertThat(source.readByte()).isEqualTo((byte) 0x05);
        }
    }

    @Test
    void testFileSourceRepeatedReadPastEndStayClean(@TempDir Path tempDir) throws IOException {
        var path = tempDir.resolve("eof.bin");
        Files.write(path, new byte[]{0x01});
        try (var source = BinarySource.open(path)) {
            source.readByte();
            assertThatExceptionOfType(EOFException.class).isThrownBy(source::readByte);
            assertThatExceptionOfType(EOFException.class).isThrownBy(source::readByte);
        }
    }

    @Test
    void testFileSourcePositionThenReadAfterEof(@TempDir Path tempDir) throws IOException {
        var path = tempDir.resolve("eof2.bin");
        Files.write(path, new byte[]{0x01, 0x02, 0x03});
        try (var source = BinarySource.open(path)) {
            source.position(3);
            assertThatExceptionOfType(EOFException.class).isThrownBy(source::readByte);
            source.position(0);
            assertThat(source.readByte()).isEqualTo((byte) 0x01);
        }
    }

    @Test
    void testBufferedRefillReturnsPartialAtEof() throws IOException {
        try (var source = new TestBufferedSource(new byte[]{1, 2, 3, 4})) {
            assertThatExceptionOfType(EOFException.class).isThrownBy(source::readLong);
            assertThatExceptionOfType(EOFException.class).isThrownBy(source::readLong);
        }
    }

    @Test
    void testSequenceReadsAcrossSources() throws IOException {
        var first = bytes(0x01, 0x02);
        var second = bytes(0x03, 0x04);
        var third = bytes(0x05, 0x06);
        try (var seq = BinarySource.sequence(List.of(first, second, third))) {
            assertThat(seq.size()).isEqualTo(6);
            for (var i = 1; i <= 6; i++) {
                assertThat(seq.readByte()).as("byte %d", i).isEqualTo((byte) i);
            }
        }
    }

    @Test
    void testSequenceWithEmptyMembersStillReadsCleanly() throws IOException {
        try (var seq = BinarySource.sequence(List.of(
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
        try (var seq = BinarySource.sequence(List.of(bytes(0x01, 0x02), bytes(0x03)))) {
            seq.readByte();
            seq.readByte();
            seq.readByte();
            assertThatExceptionOfType(EOFException.class).isThrownBy(seq::readByte);
        }
    }

    private static BinarySource bytes(int... values) {
        var data = Bytes.Mutable.allocate(values.length);
        for (var i = 0; i < values.length; i++) {
            data.set(i, (byte) values[i]);
        }
        return BinarySource.wrap(data);
    }

    private static final class TestBufferedSource extends BufferedBinarySource {
        private final byte[] data;

        TestBufferedSource(byte[] data) {
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
