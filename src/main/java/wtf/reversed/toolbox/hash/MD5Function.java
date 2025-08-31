package wtf.reversed.toolbox.hash;

import wtf.reversed.toolbox.collect.*;

import java.security.*;

final class MD5Function implements HashFunction {
    private static final ThreadLocal<MessageDigest> DIGEST = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new ExceptionInInitializerError(e);
        }
    });

    @Override
    public HashCode hash(Bytes input) {
        var digest = DIGEST.get();
        digest.update(input.asBuffer());
        return HashCode.ofBytes(digest.digest());
    }
}
