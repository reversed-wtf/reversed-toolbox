package wtf.reversed.toolbox.hash;

import wtf.reversed.toolbox.collect.*;

import java.security.*;

final class MessageDigestHashFunction implements HashFunction {
    private final MessageDigest digest;

    MessageDigestHashFunction(String algorithm) {
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
