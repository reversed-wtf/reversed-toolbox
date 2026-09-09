package wtf.reversed.toolbox.hash;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

class FNV1A_64Test {
    @Test
    void hashShouldReturnCorrectValue() {
        assertThat(HashFunction.fnv1a_64().hash("").asLong()).isEqualTo(0xcbf29ce484222325L);
        assertThat(HashFunction.fnv1a_64().hash("The quick brown fox jumps over the lazy dog").asLong()).isEqualTo(0xf3f9b7f5e7e47110L);
    }
}
