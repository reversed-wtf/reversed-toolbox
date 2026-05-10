package wtf.reversed.toolbox.io;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import wtf.reversed.toolbox.collect.*;

import java.io.*;
import java.nio.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static org.assertj.core.api.Assertions.*;

class BitSourceTest {
    private static final Endian BIG = new Endian("big", true, BitSource::big);
    private static final Endian LITTLE = new Endian("little", false, BitSource::little);

    static Stream<Endian> endians() {
        return Stream.of(BIG, LITTLE);
    }

    private static BinarySource bytes(int... values) {
        var data = Bytes.Mutable.allocate(values.length);
        for (var i = 0; i < values.length; i++) {
            data.set(i, (byte) values[i]);
        }
        return BinarySource.wrap(data);
    }

    @Nested
    class BigEndian {
        @Test
        void testReadsHighBitsFirstWithinByte() throws IOException {
            try (var bits = BitSource.big(bytes(0xAB))) {
                assertThat(bits.read(4)).isEqualTo(0xA);
                assertThat(bits.read(4)).isEqualTo(0xB);
            }
        }

        @Test
        void testReadOneReturnsBitsCorrectly() throws IOException {
            try (var bits = BitSource.big(bytes(0xAB))) {
                assertThat(bits.readOne()).isEqualTo(1);
                assertThat(bits.readOne()).isEqualTo(0);
                assertThat(bits.readOne()).isEqualTo(1);
                assertThat(bits.readOne()).isEqualTo(0);
                assertThat(bits.readOne()).isEqualTo(1);
                assertThat(bits.readOne()).isEqualTo(0);
                assertThat(bits.readOne()).isEqualTo(1);
                assertThat(bits.readOne()).isEqualTo(1);
            }
        }

        @Test
        void testReadFlagInterpretsBitsAsBoolean() throws IOException {
            try (var bits = BitSource.big(bytes(0xAB))) {
                assertThat(bits.readFlag()).isTrue();
                assertThat(bits.readFlag()).isFalse();
                assertThat(bits.readFlag()).isTrue();
                assertThat(bits.readFlag()).isFalse();
                assertThat(bits.readFlag()).isTrue();
                assertThat(bits.readFlag()).isFalse();
                assertThat(bits.readFlag()).isTrue();
                assertThat(bits.readFlag()).isTrue();
            }
        }

        @Test
        void testReadsAcrossByteBoundary() throws IOException {
            try (var bits = BitSource.big(bytes(0xAB, 0xCD))) {
                assertThat(bits.read(12)).isEqualTo(0xABC);
                assertThat(bits.read(4)).isEqualTo(0xD);
            }
        }

        @Test
        void testReadsUpTo31Bits() throws IOException {
            try (var bits = BitSource.big(bytes(0x12, 0x34, 0x56, 0x78))) {
                assertThat(bits.read(31)).isEqualTo(0x1234_5678 >> 1);
            }
        }

        @Test
        void testReadsLongsUpTo57Bits() throws IOException {
            try (var bits = BitSource.big(bytes(0x12, 0x34, 0x56, 0x78, 0x9A, 0xBC, 0xDE, 0xF0))) {
                assertThat(bits.readLong(57)).isEqualTo(0x1234_5678_9ABC_DEF0L >> 7);
            }
        }

        @Test
        void testPeekPastEndZeroPads() throws IOException {
            try (var bits = BitSource.big(bytes(0xAB))) {
                assertThat(bits.peek(12)).isEqualTo(0xAB0);
                assertThat(bits.read(8)).isEqualTo(0xAB);
            }
        }

        @Test
        void testSkipCanCrossByteBoundaries() throws IOException {
            try (var bits = BitSource.big(bytes(0xAB, 0xCD))) {
                bits.peekLong(16);
                bits.skip(12);
                assertThat(bits.read(4)).isEqualTo(0xD);
            }
        }

        @Test
        void testBulkRefillStitchesAcrossLeftover() throws IOException {
            var data = new int[16];
            for (var i = 0; i < data.length; i++) {
                data[i] = i;
            }
            try (var bits = BitSource.big(bytes(data))) {
                bits.readLong(56);
                assertThat(bits.read(16)).isEqualTo(0x0708);
            }
        }
    }

    @Nested
    class LittleEndian {
        @Test
        void testReadsLowBitsFirstWithinByte() throws IOException {
            try (var bits = BitSource.little(bytes(0xAB))) {
                assertThat(bits.read(4)).isEqualTo(0xB);
                assertThat(bits.read(4)).isEqualTo(0xA);
            }
        }

        @Test
        void testReadOneReturnsBitsCorrectly() throws IOException {
            try (var bits = BitSource.little(bytes(0xAB))) {
                assertThat(bits.readOne()).isEqualTo(1);
                assertThat(bits.readOne()).isEqualTo(1);
                assertThat(bits.readOne()).isEqualTo(0);
                assertThat(bits.readOne()).isEqualTo(1);
                assertThat(bits.readOne()).isEqualTo(0);
                assertThat(bits.readOne()).isEqualTo(1);
                assertThat(bits.readOne()).isEqualTo(0);
                assertThat(bits.readOne()).isEqualTo(1);
            }
        }

        @Test
        void testReadFlagInterpretsBitsAsBoolean() throws IOException {
            try (var bits = BitSource.little(bytes(0xAB))) {
                assertThat(bits.readFlag()).isTrue();
                assertThat(bits.readFlag()).isTrue();
                assertThat(bits.readFlag()).isFalse();
                assertThat(bits.readFlag()).isTrue();
                assertThat(bits.readFlag()).isFalse();
                assertThat(bits.readFlag()).isTrue();
                assertThat(bits.readFlag()).isFalse();
                assertThat(bits.readFlag()).isTrue();
            }
        }

        @Test
        void testReadsAcrossByteBoundary() throws IOException {
            try (var bits = BitSource.little(bytes(0xAB, 0xCD))) {
                assertThat(bits.read(12)).isEqualTo(0xDAB);
                assertThat(bits.read(4)).isEqualTo(0xC);
            }
        }

        @Test
        void testReadsUpTo31Bits() throws IOException {
            try (var bits = BitSource.little(bytes(0x12, 0x34, 0x56, 0x78))) {
                assertThat(bits.read(31)).isEqualTo(0x7856_3412 & ((1 << 31) - 1));
            }
        }

        @Test
        void testReadsLongsUpTo57Bits() throws IOException {
            try (var bits = BitSource.little(bytes(0x12, 0x34, 0x56, 0x78, 0x9A, 0xBC, 0xDE, 0xF0))) {
                assertThat(bits.readLong(57)).isEqualTo(0xF0DE_BC9A_7856_3412L & ((1L << 57) - 1));
            }
        }

        @Test
        void testPeekPastEndZeroPads() throws IOException {
            try (var bits = BitSource.little(bytes(0xAB))) {
                assertThat(bits.peek(12)).isEqualTo(0xAB);
                assertThat(bits.read(8)).isEqualTo(0xAB);
            }
        }

        @Test
        void testSkipCanCrossByteBoundaries() throws IOException {
            try (var bits = BitSource.little(bytes(0xAB, 0xCD))) {
                bits.peekLong(16);
                bits.skip(12);
                assertThat(bits.read(4)).isEqualTo(0xC);
            }
        }

        @Test
        void testBulkRefillStitchesAcrossLeftover() throws IOException {
            var data = new int[16];
            for (var i = 0; i < data.length; i++) {
                data[i] = i;
            }
            try (var bits = BitSource.little(bytes(data))) {
                bits.readLong(56);
                assertThat(bits.read(16)).isEqualTo(0x0807);
            }
        }
    }

    @Nested
    class Common {
        static Stream<Arguments> testReadsBytesUnchangedRegardlessOfSourceOrder() {
            return Stream.of(
                Arguments.of(BIG, ByteOrder.BIG_ENDIAN),
                Arguments.of(BIG, ByteOrder.LITTLE_ENDIAN),
                Arguments.of(LITTLE, ByteOrder.BIG_ENDIAN),
                Arguments.of(LITTLE, ByteOrder.LITTLE_ENDIAN)
            );
        }

        @ParameterizedTest
        @MethodSource("wtf.reversed.toolbox.io.BitSourceTest#endians")
        void testThrowsOnNullSource(Endian endian) {
            assertThatNullPointerException()
                .isThrownBy(() -> endian.factory().apply(null))
                .withMessage("'source' must not be null");
        }

        @ParameterizedTest
        @MethodSource("wtf.reversed.toolbox.io.BitSourceTest#endians")
        void testPeekDoesNotAdvancePosition(Endian endian) throws IOException {
            try (var bits = endian.factory().apply(bytes(0xAB, 0xCD))) {
                assertThat(bits.peek(8)).isEqualTo(0xAB);
                assertThat(bits.peek(8)).isEqualTo(0xAB);
                assertThat(bits.read(8)).isEqualTo(0xAB);
                assertThat(bits.read(8)).isEqualTo(0xCD);
            }
        }

        @ParameterizedTest
        @MethodSource("wtf.reversed.toolbox.io.BitSourceTest#endians")
        void testPeekLongSupportsUpTo57Bits(Endian endian) throws IOException {
            try (var bits = endian.factory().apply(bytes(0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF))) {
                assertThat(bits.peekLong(57)).isEqualTo((1L << 57) - 1);
                assertThat(bits.readLong(57)).isEqualTo((1L << 57) - 1);
            }
        }

        @ParameterizedTest
        @MethodSource("wtf.reversed.toolbox.io.BitSourceTest#endians")
        void testPeekOnEmptySourceReturnsZero(Endian endian) throws IOException {
            try (var bits = endian.factory().apply(bytes())) {
                assertThat(bits.peek(8)).isZero();
                assertThat(bits.peekLong(57)).isZero();
            }
        }

        @ParameterizedTest
        @MethodSource("wtf.reversed.toolbox.io.BitSourceTest#endians")
        void testSkipDiscardsBufferedBits(Endian endian) throws IOException {
            try (var bits = endian.factory().apply(bytes(0xAB, 0xCD))) {
                bits.peekLong(16);
                bits.skip(8);
                assertThat(bits.read(8)).isEqualTo(0xCD);
            }
        }

        @ParameterizedTest
        @MethodSource("wtf.reversed.toolbox.io.BitSourceTest#endians")
        void testAlignToByteConsumesRemainingBitsOfCurrentByte(Endian endian) throws IOException {
            try (var bits = endian.factory().apply(bytes(0xAB, 0xCD))) {
                bits.read(3);
                bits.alignToByte();
                assertThat(bits.read(8)).isEqualTo(0xCD);
            }
        }

        @ParameterizedTest
        @MethodSource("wtf.reversed.toolbox.io.BitSourceTest#endians")
        void testAlignToByteIsNoopWhenAlreadyAligned(Endian endian) throws IOException {
            try (var bits = endian.factory().apply(bytes(0xAB, 0xCD))) {
                bits.read(8);
                bits.alignToByte();
                assertThat(bits.read(8)).isEqualTo(0xCD);
            }
        }

        @ParameterizedTest
        @MethodSource("wtf.reversed.toolbox.io.BitSourceTest#endians")
        void testAlignToByteIsNoopWhenBufferEmpty(Endian endian) throws IOException {
            try (var bits = endian.factory().apply(bytes(0xAB))) {
                bits.alignToByte();
                assertThat(bits.read(8)).isEqualTo(0xAB);
            }
        }

        @ParameterizedTest
        @MethodSource("wtf.reversed.toolbox.io.BitSourceTest#endians")
        void testReadZeroReturnsZeroWithoutFetchingBytes(Endian endian) throws IOException {
            try (var bits = endian.factory().apply(bytes(0xAB))) {
                assertThat(bits.read(0)).isZero();
                assertThat(bits.peek(0)).isZero();
                assertThat(bits.readLong(0)).isZero();
                assertThat(bits.peekLong(0)).isZero();
                bits.skip(0);
                assertThat(bits.read(8)).isEqualTo(0xAB);
            }
        }

        @ParameterizedTest
        @MethodSource("wtf.reversed.toolbox.io.BitSourceTest#endians")
        void testReadZeroAfterPartialReadStillReturnsZero(Endian endian) throws IOException {
            try (var bits = endian.factory().apply(bytes(0xAB, 0xCD))) {
                bits.read(4);
                assertThat(bits.peekLong(0)).isZero();
                assertThat(bits.readLong(0)).isZero();
            }
        }

        @ParameterizedTest
        @MethodSource("wtf.reversed.toolbox.io.BitSourceTest#endians")
        void testReadPastEndThrows(Endian endian) throws IOException {
            try (var bits = endian.factory().apply(bytes(0xAB))) {
                assertThatExceptionOfType(EOFException.class)
                    .isThrownBy(() -> bits.read(12));
            }
        }

        @ParameterizedTest
        @MethodSource("wtf.reversed.toolbox.io.BitSourceTest#endians")
        void testReadOnEmptySourceThrows(Endian endian) throws IOException {
            try (var bits = endian.factory().apply(bytes())) {
                assertThatExceptionOfType(EOFException.class)
                    .isThrownBy(bits::readOne);
            }
        }

        @ParameterizedTest
        @MethodSource("wtf.reversed.toolbox.io.BitSourceTest#endians")
        void testBitsReadStartsAtZero(Endian endian) throws IOException {
            try (var bits = endian.factory().apply(bytes(0xAB, 0xCD))) {
                assertThat(bits.bitsRead()).isZero();
            }
        }

        @ParameterizedTest
        @MethodSource("wtf.reversed.toolbox.io.BitSourceTest#endians")
        void testBitsReadAdvancesWithReads(Endian endian) throws IOException {
            try (var bits = endian.factory().apply(bytes(0xAB, 0xCD, 0xEF))) {
                bits.read(3);
                assertThat(bits.bitsRead()).isEqualTo(3);
                bits.read(9);
                assertThat(bits.bitsRead()).isEqualTo(12);
                bits.skip(4);
                assertThat(bits.bitsRead()).isEqualTo(16);
            }
        }

        @ParameterizedTest
        @MethodSource("wtf.reversed.toolbox.io.BitSourceTest#endians")
        void testBitsReadIsUnaffectedByPeek(Endian endian) throws IOException {
            try (var bits = endian.factory().apply(bytes(0xAB, 0xCD))) {
                bits.peek(8);
                assertThat(bits.bitsRead()).isZero();
                bits.read(4);
                bits.peek(16);
                assertThat(bits.bitsRead()).isEqualTo(4);
            }
        }

        @ParameterizedTest
        @MethodSource("wtf.reversed.toolbox.io.BitSourceTest#endians")
        void testBitsRemainingReflectsTotalAtStart(Endian endian) throws IOException {
            try (var bits = endian.factory().apply(bytes(0xAB, 0xCD, 0xEF))) {
                assertThat(bits.bitsRemaining()).isEqualTo(24);
            }
        }

        @ParameterizedTest
        @MethodSource("wtf.reversed.toolbox.io.BitSourceTest#endians")
        void testBitsRemainingDecreasesWithReads(Endian endian) throws IOException {
            try (var bits = endian.factory().apply(bytes(0xAB, 0xCD, 0xEF))) {
                bits.read(3);
                assertThat(bits.bitsRemaining()).isEqualTo(21);
                bits.read(13);
                assertThat(bits.bitsRemaining()).isEqualTo(8);
                bits.read(8);
                assertThat(bits.bitsRemaining()).isZero();
            }
        }

        @ParameterizedTest
        @MethodSource("wtf.reversed.toolbox.io.BitSourceTest#endians")
        void testBitsReadAndRemainingSumToTotal(Endian endian) throws IOException {
            try (var bits = endian.factory().apply(bytes(0xAB, 0xCD, 0xEF, 0x12))) {
                bits.read(11);
                assertThat(bits.bitsRead() + bits.bitsRemaining()).isEqualTo(32);
            }
        }

        @ParameterizedTest
        @MethodSource("wtf.reversed.toolbox.io.BitSourceTest#endians")
        void testReadRejectsNegativeCount(Endian endian) throws IOException {
            try (var bits = endian.factory().apply(bytes(0xAB))) {
                assertThatIllegalArgumentException().isThrownBy(() -> bits.read(-1));
            }
        }

        @ParameterizedTest
        @MethodSource("wtf.reversed.toolbox.io.BitSourceTest#endians")
        void testReadRejectsCountAbove31(Endian endian) throws IOException {
            try (var bits = endian.factory().apply(bytes(0xAB))) {
                assertThatIllegalArgumentException().isThrownBy(() -> bits.read(32));
            }
        }

        @ParameterizedTest
        @MethodSource("wtf.reversed.toolbox.io.BitSourceTest#endians")
        void testReadLongRejectsNegativeCount(Endian endian) throws IOException {
            try (var bits = endian.factory().apply(bytes(0xAB))) {
                assertThatIllegalArgumentException().isThrownBy(() -> bits.readLong(-1));
            }
        }

        @ParameterizedTest
        @MethodSource("wtf.reversed.toolbox.io.BitSourceTest#endians")
        void testReadLongRejectsCountAbove57(Endian endian) throws IOException {
            try (var bits = endian.factory().apply(bytes(0xAB))) {
                assertThatIllegalArgumentException().isThrownBy(() -> bits.readLong(58));
            }
        }

        @ParameterizedTest
        @MethodSource("wtf.reversed.toolbox.io.BitSourceTest#endians")
        void testPeekRejectsNegativeCount(Endian endian) throws IOException {
            try (var bits = endian.factory().apply(bytes(0xAB))) {
                assertThatIllegalArgumentException().isThrownBy(() -> bits.peek(-1));
            }
        }

        @ParameterizedTest
        @MethodSource("wtf.reversed.toolbox.io.BitSourceTest#endians")
        void testPeekRejectsCountAbove31(Endian endian) throws IOException {
            try (var bits = endian.factory().apply(bytes(0xAB))) {
                assertThatIllegalArgumentException().isThrownBy(() -> bits.peek(32));
            }
        }

        @ParameterizedTest
        @MethodSource("wtf.reversed.toolbox.io.BitSourceTest#endians")
        void testPeekLongRejectsNegativeCount(Endian endian) throws IOException {
            try (var bits = endian.factory().apply(bytes(0xAB))) {
                assertThatIllegalArgumentException().isThrownBy(() -> bits.peekLong(-1));
            }
        }

        @ParameterizedTest
        @MethodSource("wtf.reversed.toolbox.io.BitSourceTest#endians")
        void testPeekLongRejectsCountAbove57(Endian endian) throws IOException {
            try (var bits = endian.factory().apply(bytes(0xAB))) {
                assertThatIllegalArgumentException().isThrownBy(() -> bits.peekLong(58));
            }
        }

        @ParameterizedTest
        @MethodSource("wtf.reversed.toolbox.io.BitSourceTest#endians")
        void testSkipRejectsNegativeCount(Endian endian) throws IOException {
            try (var bits = endian.factory().apply(bytes(0xAB))) {
                assertThatIllegalArgumentException().isThrownBy(() -> bits.skip(-1));
            }
        }

        @ParameterizedTest
        @MethodSource("wtf.reversed.toolbox.io.BitSourceTest#endians")
        void testSkipRejectsCountAbove57(Endian endian) throws IOException {
            try (var bits = endian.factory().apply(bytes(0xAB))) {
                assertThatIllegalArgumentException().isThrownBy(() -> bits.skip(58));
            }
        }

        @ParameterizedTest
        @MethodSource("wtf.reversed.toolbox.io.BitSourceTest#endians")
        void testBulkRefillReadsMatchByteContents(Endian endian) throws IOException {
            var data = new int[64];
            for (var i = 0; i < data.length; i++) {
                data[i] = (i * 17) & 0xFF;
            }
            try (var bits = endian.factory().apply(bytes(data))) {
                for (var i = 0; i < data.length; i++) {
                    int first = bits.read(4);
                    int second = bits.read(4);
                    int reconstructed = endian.big() ? (first << 4) | second : first | (second << 4);
                    assertThat(reconstructed).as("byte %d", i).isEqualTo(data[i]);
                }
            }
        }

        @ParameterizedTest
        @MethodSource("wtf.reversed.toolbox.io.BitSourceTest#endians")
        void testBulkRefillSingleByteFitFromMaxLeftover(Endian endian) throws IOException {
            var data = new int[16];
            Arrays.fill(data, 0xFF);
            try (var bits = endian.factory().apply(bytes(data))) {
                bits.read(8);
                assertThat(bits.readLong(57)).isEqualTo((1L << 57) - 1);
            }
        }

        @ParameterizedTest
        @MethodSource
        void testReadsBytesUnchangedRegardlessOfSourceOrder(Endian endian, ByteOrder sourceOrder) throws IOException {
            var data = Bytes.Mutable.allocate(16);
            for (var i = 0; i < 16; i++) {
                data.set(i, (byte) i);
            }
            var source = BinarySource.wrap(data).order(sourceOrder);
            try (var bits = endian.factory().apply(source)) {
                for (var i = 0; i < 16; i++) {
                    assertThat(bits.read(8)).as("byte %d", i).isEqualTo(i);
                }
            }
        }
    }

    private record Endian(String name, boolean big, Function<BinarySource, BitSource> factory) {
        @Override
        public String toString() {
            return name;
        }
    }
}
