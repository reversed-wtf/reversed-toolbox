package wtf.reversed.toolbox.io;

import org.junit.jupiter.api.*;
import wtf.reversed.toolbox.collect.*;

import java.io.*;
import java.nio.*;

import static org.junit.jupiter.api.Assertions.*;

class BytesBinaryReaderTest {
    @Test
    void testReader() throws IOException {
        Bytes.Mutable bytes = Bytes.allocate(16)
            .set(0, (byte) 0)
            .set(1, (byte) 1)
            .setShort(2, (short) 2)
            .setInt(4, 3)
            .setLong(8, 4);

        try (BinaryReader reader = BinaryReader.wrap(bytes)) {
            assertEquals(0, reader.position());
            assertEquals(16, reader.size());
            assertEquals(16, reader.remaining());
            assertNotEquals(0, reader.remaining());

            assertEquals(0, reader.readByte());
            assertEquals(1, reader.readByte());
            assertEquals(2, reader.readShort());
            assertEquals(3, reader.readInt());
            assertEquals(4, reader.readLong());

            assertEquals(16, reader.position());
            assertEquals(0, reader.remaining());
            assertEquals(0, reader.remaining());

            reader.position(8);
            assertEquals(8, reader.position());
            assertEquals(8, reader.remaining());

            assertEquals(4, reader.readLong());
            assertEquals(0, reader.remaining());
        }
    }

    @Test
    void testEndian() throws IOException {
        Bytes.Mutable buffer = Bytes.allocate(12)
            .setInt(0, 0x10203040)
            .setInt(4, Integer.reverseBytes(0x10203040))
            .setInt(8, 0x10203040);

        try (BinaryReader reader = BinaryReader.wrap(buffer)) {
            reader.order(ByteOrder.nativeOrder());
            assertEquals(0x10203040, reader.readInt());

            reader.order(ByteOrder.BIG_ENDIAN);
            assertEquals(0x10203040, reader.readInt());

            reader.order(ByteOrder.LITTLE_ENDIAN);
            assertEquals(0x10203040, reader.readInt());
        }
    }
}
