package wtf.reversed.toolbox.io;

import org.junit.jupiter.api.*;
import wtf.reversed.toolbox.collect.*;

import java.io.*;
import java.nio.*;

import static org.junit.jupiter.api.Assertions.*;

class BytesBinarySourceTest {
    @Test
    void testReader() throws IOException {
        Bytes.Mutable bytes = Bytes.Mutable.allocate(16)
            .set(0, (byte) 0)
            .set(1, (byte) 1)
            .setShort(2, (short) 2)
            .setInt(4, 3)
            .setLong(8, 4);

        try (BinarySource source = BinarySource.wrap(bytes)) {
            assertEquals(0, source.position());
            assertEquals(16, source.size());
            assertEquals(16, source.remaining());
            assertNotEquals(0, source.remaining());

            assertEquals(0, source.readByte());
            assertEquals(1, source.readByte());
            assertEquals(2, source.readShort());
            assertEquals(3, source.readInt());
            assertEquals(4, source.readLong());

            assertEquals(16, source.position());
            assertEquals(0, source.remaining());
            assertEquals(0, source.remaining());

            source.position(8);
            assertEquals(8, source.position());
            assertEquals(8, source.remaining());

            assertEquals(4, source.readLong());
            assertEquals(0, source.remaining());
        }
    }

    @Test
    void testEndian() throws IOException {
        Bytes.Mutable buffer = Bytes.Mutable.allocate(12)
            .setInt(0, 0x10203040)
            .setInt(4, Integer.reverseBytes(0x10203040))
            .setInt(8, 0x10203040);

        try (BinarySource source = BinarySource.wrap(buffer)) {
            source.order(ByteOrder.nativeOrder());
            assertEquals(0x10203040, source.readInt());

            source.order(ByteOrder.BIG_ENDIAN);
            assertEquals(0x10203040, source.readInt());

            source.order(ByteOrder.LITTLE_ENDIAN);
            assertEquals(0x10203040, source.readInt());
        }
    }
}
