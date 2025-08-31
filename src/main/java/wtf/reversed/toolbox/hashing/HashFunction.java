package wtf.reversed.toolbox.hashing;

import java.nio.charset.*;

public abstract sealed class HashFunction
    permits Fnv1aFunction, Murmur3Function, Md5Function {

    public static HashFunction fnv1a_64() {
        return new Fnv1aFunction();
    }

    public static HashFunction murmur3_128(int seed) {
        return new Murmur3Function(seed);
    }

    public static HashFunction md5() {
        return new Md5Function();
    }

    public abstract HashCode hash(byte[] input, int off, int len);

    public HashCode hash(byte[] input) {
        return hash(input, 0, input.length);
    }

    public HashCode hash(CharSequence input, Charset charset) {
        return hash(input.toString().getBytes(charset));
    }

    public HashCode hash(CharSequence input) {
        return hash(input, StandardCharsets.UTF_8);
    }
}
