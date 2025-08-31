package wtf.reversed.toolbox.hash;

import wtf.reversed.toolbox.collect.*;

import java.nio.charset.*;

public sealed interface HashFunction
    permits FNV1a64, MurmurHash3x64, MD5Function {

    static HashFunction fnv1a64() {
        return new FNV1a64();
    }

    static HashFunction murmur3(int seed) {
        return new MurmurHash3x64(seed);
    }

    static HashFunction md5() {
        return new MD5Function();
    }

    HashCode hash(Bytes input);

    default HashCode hash(CharSequence input) {
        return hash(input, StandardCharsets.UTF_8);
    }

    default HashCode hash(CharSequence input, Charset charset) {
        return hash(Bytes.wrap(input.toString().getBytes(charset)));
    }
}
