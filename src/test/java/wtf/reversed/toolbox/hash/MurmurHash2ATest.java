package wtf.reversed.toolbox.hash;

import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import wtf.reversed.toolbox.collect.*;

import static org.assertj.core.api.Assertions.*;

class MurmurHash2ATest {
    @ParameterizedTest
    @CsvSource({
        "0,  00000000, 00000000",
        "0,  9E3779B1, D82DB789",
        "1,  9E3779B1, FCB5CE8B",
        "2,  9E3779B1, 59AEB21C",
        "3,  9E3779B1, C25558F2",
        "4,  9E3779B1, 6DC4F7A4",
        "5,  9E3779B1, B5EBBA74",
        "7,  9E3779B1, 41F870D3",
        "8,  9E3779B1, 561CDD87",
        "9,  9E3779B1, C4202FDB",
        "12, 9E3779B1, 1E1F018E",
        "13, 00000000, ADDC5E45",
        "13, 9E3779B1, 69D45318",
        "16, 9E3779B1, 9C57EBA1",
        "31, 9E3779B1, D3B6B880",
    })
    void testMurmurHash2A(int length, String seedString, String expectedString) {
        int seed = Integer.parseUnsignedInt(seedString, 16);

        Bytes buffer = XXHashGenerator.generate(length);
        int actual = new MurmurHash2A(seed).hash(buffer).asInt();
        int expected = Integer.parseUnsignedInt(expectedString, 16);
        assertThat(actual).isEqualTo(expected);
    }
}
