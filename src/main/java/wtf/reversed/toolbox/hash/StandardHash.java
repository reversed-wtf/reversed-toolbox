package wtf.reversed.toolbox.hash;

import wtf.reversed.toolbox.collect.*;

import java.security.*;

final class StandardHash implements HashFunction {
    private final MessageDigest digest;

    StandardHash(String algorithm) {
        try {
            digest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    @Override
    public HashCode hash(Bytes src) {
        digest.update(src.asBuffer());
        byte[] result = digest.digest();
        return HashCode.ofBytes(Bytes.wrap(result));
    }
}
