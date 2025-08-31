package wtf.reversed.toolbox.hash;

import wtf.reversed.toolbox.collect.*;

import java.nio.charset.*;

public sealed interface HashFunction
    permits FNV1a64, FarmHashFingerprint64, MD5Function, MurmurHash3x64, MurmurHash64B, XXHash32, XXHash64 {

    static HashFunction farmHashFingerprint64() {
        return FarmHashFingerprint64.INSTANCE;
    }

    static HashFunction fnv1a64() {
        return new FNV1a64();
    }

    static HashFunction md5() {
        return MD5Function.INSTANCE;
    }

    static HashFunction murmur64B(long seed) {
        return new MurmurHash64B(seed);
    }

    static HashFunction murmur3(int seed) {
        return new MurmurHash3x64(seed);
    }

    static HashFunction xxHash32(int seed) {
        return new XXHash32(seed);
    }

    static HashFunction xxHash64(long seed) {
        return new XXHash64(seed);
    }

    HashCode hash(Bytes input);

    default HashCode hash(CharSequence input) {
        return hash(input, StandardCharsets.UTF_8);
    }

    default HashCode hash(CharSequence input, Charset charset) {
        return hash(Bytes.wrap(input.toString().getBytes(charset)));
    }
}
