package wtf.reversed.toolbox.hash;

import wtf.reversed.toolbox.collect.*;

final class FNV1a64 implements HashFunction {
    private static final long FNV_BASIS = 0xcbf29ce484222325L;
    private static final long FNV_PRIME = 0x00000100000001b3L;

    @Override
    public HashCode hash(Bytes input) {
        long hash = FNV_BASIS;
        for (int i = 0, len = input.length(); i < len; i++) {
            hash ^= input.getUnsigned(i);
            hash *= FNV_PRIME;
        }
        return HashCode.ofLong(hash);
    }
}
