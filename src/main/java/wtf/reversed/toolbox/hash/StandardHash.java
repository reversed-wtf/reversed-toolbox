package wtf.reversed.toolbox.hash;

import wtf.reversed.toolbox.collect.*;

import java.security.*;

final class StandardHash implements HashFunction {
    private final ThreadLocal<MessageDigest> digest;

    StandardHash(String algorithm) {
        digest = ThreadLocal.withInitial(() -> create(algorithm));
    }

    @Override
    public HashCode hash(Bytes src) {
        var digest = this.digest.get();
        digest.update(src.asBuffer());
        byte[] result = digest.digest();
        return HashCode.ofBytes(Bytes.wrap(result));
    }

    private static MessageDigest create(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException(algorithm, e);
        }
    }
}
