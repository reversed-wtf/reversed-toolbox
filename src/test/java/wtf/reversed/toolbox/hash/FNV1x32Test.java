package wtf.reversed.toolbox.hash;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

class FNV1x32Test {
    @Test
    void hashShouldReturnCorrectValue() {
        assertThat(HashFunction.fnv1x32().hash("").asInt()).isEqualTo(0x811C_9DC5);
        assertThat(HashFunction.fnv1x32().hash("The quick brown fox jumps over the lazy dog").asInt()).isEqualTo(0xE9C8_6C6E);
    }
}
