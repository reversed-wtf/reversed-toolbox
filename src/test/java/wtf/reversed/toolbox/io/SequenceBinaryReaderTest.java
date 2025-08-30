package wtf.reversed.toolbox.io;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SequenceBinaryReaderTest {
    @Test
    void testReader() throws IOException {
        // Internally, SequenceBinaryReader uses a 4KB buffer, so allocate enough
        // data so it won't fully fit
        byte[] data = new byte[0x40000];
        Random random = new Random(42);
        random.nextBytes(data);

        // Arbitrary-sized chunks
        List<BinaryReader> readers = List.of(
            BinaryReader.wrap(data, 0, 16383),
            BinaryReader.wrap(data, 16383, 16385),
            BinaryReader.wrap(data, 32768, 32768),
            BinaryReader.wrap(data, 65536, 131072),
            BinaryReader.wrap(data, 196608, 65536)
        );

        try (BinaryReader reader = BinaryReader.of(readers)) {
            assertEquals(data.length, reader.size());

            // Read back one byte at a time, so it can't perform a bulk read operation
            byte[] output = new byte[data.length];
            for (int i = 0; i < output.length; i++) {
                output[i] = reader.readByte();
            }

            assertArrayEquals(data, output);
        }
    }
}
