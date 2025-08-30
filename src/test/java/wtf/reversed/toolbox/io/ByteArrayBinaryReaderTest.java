package wtf.reversed.toolbox.io;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

class ByteArrayBinaryReaderTest {
    @Test
    void testReader() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(16)
            .order(ByteOrder.nativeOrder())
            .put((byte) 0)
            .put((byte) 1)
            .putShort((short) 2)
            .putInt(3)
            .putLong(4)
            .flip();

        try (BinaryReader reader = BinaryReader.wrap(buffer)) {
            assertEquals(0, reader.position());
            assertEquals(16, reader.size());
            assertEquals(16, reader.remaining());
            assertFalse(reader.isDrained());

            assertEquals(0, reader.readByte());
            assertEquals(1, reader.readByte());
            assertEquals(2, reader.readShort());
            assertEquals(3, reader.readInt());
            assertEquals(4, reader.readLong());

            assertEquals(16, reader.position());
            assertEquals(0, reader.remaining());
            assertTrue(reader.isDrained());

            reader.position(8);
            assertEquals(8, reader.position());
            assertEquals(8, reader.remaining());

            assertEquals(4, reader.readLong());
            assertTrue(reader.isDrained());
        }
    }

    @Test
    void testEndian() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(12)
            .order(ByteOrder.nativeOrder())
            .putInt(0x10203040)
            .order(ByteOrder.BIG_ENDIAN)
            .putInt(0x10203040)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(0x10203040)
            .flip();

        try (BinaryReader reader = BinaryReader.wrap(buffer)) {
            reader.order(ByteOrder.nativeOrder());
            assertEquals(0x10203040, reader.readInt());

            reader.order(ByteOrder.BIG_ENDIAN);
            assertEquals(0x10203040, reader.readInt());

            reader.order(ByteOrder.LITTLE_ENDIAN);
            assertEquals(0x10203040, reader.readInt());
        }
    }

    @Test
    void testWrapDirectBuffer() {
        assertThrows(IllegalArgumentException.class, () -> BinaryReader.wrap(ByteBuffer.allocateDirect(10)));
    }
}
