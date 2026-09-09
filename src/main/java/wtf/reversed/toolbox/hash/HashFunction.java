package wtf.reversed.toolbox.hash;

import wtf.reversed.toolbox.collect.*;

import java.nio.charset.*;

/**
 * A function that maps an arbitrary sequence of bytes to a fixed-size {@link HashCode}.
 */
public interface HashFunction {
    /**
     * Returns a CRC hash function for the given algorithm parameters.
     *
     * @param algorithm the CRC algorithm description
     */
    static HashFunction crc(CRCAlgorithm algorithm) {
        return new CRC(algorithm);
    }

    /**
     * Returns a hash function implementing Google's {@code FarmHash Fingerprint64}.
     */
    static HashFunction farmHashFingerprint64() {
        return FarmHashFingerprint64.INSTANCE;
    }

    /**
     * Returns a hash function implementing the 32-bit FNV-1 algorithm.
     */
    static HashFunction fnv1_32() {
        return FNV1_32.INSTANCE;
    }

    /**
     * Returns a hash function implementing the 64-bit FNV-1a algorithm.
     */
    static HashFunction fnv1a_64() {
        return FNV1A_64.INSTANCE;
    }

    /**
     * Returns a hash function implementing the 64-bit "B" variant of MurmurHash 2.
     *
     * @param seed the seed value mixed into the initial state
     */
    static HashFunction murmur64B(long seed) {
        return new MurmurHash64B(seed);
    }

    /**
     * Returns a hash function implementing the 128-bit x64 variant of MurmurHash 3.
     *
     * @param seed the seed value mixed into the initial state
     */
    static HashFunction murmur3(int seed) {
        return new MurmurHash3x64(seed);
    }

    /**
     * Returns a hash function backed by a {@link java.security.MessageDigest}.
     *
     * @param algorithm the standard digest algorithm name (see {@link StandardHashes})
     */
    static HashFunction standard(String algorithm) {
        return new StandardHash(algorithm);
    }

    /**
     * Returns a hash function implementing 32-bit xxHash.
     *
     * @param seed the seed value mixed into the initial state
     */
    static HashFunction xxHash32(int seed) {
        return new XXHash32(seed);
    }

    /**
     * Returns a hash function implementing 64-bit xxHash.
     *
     * @param seed the seed value mixed into the initial state
     */
    static HashFunction xxHash64(long seed) {
        return new XXHash64(seed);
    }

    /**
     * Hashes the given byte sequence.
     *
     * @param input the bytes to hash
     * @return the resulting hash code
     */
    HashCode hash(Bytes input);

    /**
     * Hashes the UTF-8 encoding of the given character sequence.
     *
     * @param input the characters to hash
     * @return the resulting hash code
     */
    default HashCode hash(CharSequence input) {
        return hash(input, StandardCharsets.UTF_8);
    }

    /**
     * Hashes the given character sequence encoded with the given charset.
     *
     * @param input   the characters to hash
     * @param charset the charset used to encode {@code input}
     * @return the resulting hash code
     */
    default HashCode hash(CharSequence input, Charset charset) {
        return hash(Bytes.wrap(input.toString().getBytes(charset)));
    }
}
