package wtf.reversed.toolbox.io;

import org.junit.jupiter.api.*;
import wtf.reversed.toolbox.collect.*;

import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SequenceBinarySourceTest {
    @Test
    void testSource() throws IOException {
        // Internally, SequenceBinaryReader uses a 4KB buffer, so allocate enough
        // data so it won't fully fit
        byte[] data = new byte[0x40000];
        Random random = new Random(42);
        random.nextBytes(data);

        // Arbitrary-sized chunks
        List<BinarySource> sources = List.of(
            BinarySource.wrap(Bytes.wrap(data, 0, 16383)),
            BinarySource.wrap(Bytes.wrap(data, 16383, 16385)),
            BinarySource.wrap(Bytes.wrap(data, 32768, 32768)),
            BinarySource.wrap(Bytes.wrap(data, 65536, 131072)),
            BinarySource.wrap(Bytes.wrap(data, 196608, 65536))
        );

        try (BinarySource source = BinarySource.sequence(sources)) {
            assertEquals(data.length, source.size());

            // Read back one byte at a time, so it can't perform a bulk read operation
            byte[] output = new byte[data.length];
            for (int i = 0; i < output.length; i++) {
                output[i] = source.readByte();
            }

            assertArrayEquals(data, output);
        }
    }
}
