package wtf.reversed.toolbox.hash;

import wtf.reversed.toolbox.collect.*;

import java.security.*;

final class StandardHash implements HashFunction {
    private final MessageDigest prototype;

    StandardHash(String algorithm) {
        try {
            prototype = MessageDigest.getInstance(algorithm);
            prototype.clone(); // see if it works
        } catch (NoSuchAlgorithmException | CloneNotSupportedException e) {
            throw new UnsupportedOperationException(algorithm, e);
        }
    }

    @Override
    public HashCode hash(Bytes src) {
        MessageDigest digest;
        try {
            digest = (MessageDigest) prototype.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e); // already checked
        }
        digest.update(src.asBuffer());
        return HashCode.ofBytes(Bytes.wrap(digest.digest()));
    }
}
