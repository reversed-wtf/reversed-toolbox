package wtf.reversed.toolbox.hash;

import java.security.*;

final class Md5Function extends HashFunction {
    private static final ThreadLocal<MessageDigest> DIGEST = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new ExceptionInInitializerError(e);
        }
    });

    @Override
    public HashCode hash(byte[] input, int off, int len) {
        MessageDigest digest = DIGEST.get();
        digest.update(input, off, len);
        return HashCode.ofBytes(digest.digest());
    }
}
