package wtf.reversed.toolbox.hash;

import wtf.reversed.toolbox.collect.*;

final class FNV1A_64 implements HashFunction {
    static final FNV1A_64 INSTANCE = new FNV1A_64();

    private static final long FNV_BASIS = 0xcbf29ce484222325L;
    private static final long FNV_PRIME = 0x00000100000001b3L;

    private FNV1A_64() {
    }

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
