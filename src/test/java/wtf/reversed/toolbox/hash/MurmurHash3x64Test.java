package wtf.reversed.toolbox.hash;

import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import wtf.reversed.toolbox.collect.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

class MurmurHash3x64Test {
    @ParameterizedTest
    @CsvFileSource(resources = "MurmurHash3x64.csv")
    void testMurmurHash3x64(int length, String seedString, String expectedString) {
        int seed = Integer.parseUnsignedInt(seedString, 16);

        Bytes buffer = XXHashGenerator.generate(length);
        byte[] actual = new MurmurHash3x64(seed).hash(buffer).asBuffer().array();
        byte[] expected = HexFormat.of().parseHex(expectedString);
        assertThat(actual).isEqualTo(expected);
    }
}
