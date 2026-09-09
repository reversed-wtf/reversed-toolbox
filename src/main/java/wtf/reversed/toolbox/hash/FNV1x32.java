package wtf.reversed.toolbox.hash;

import wtf.reversed.toolbox.collect.*;

final class FNV1x32 implements HashFunction {
    static final FNV1x32 INSTANCE = new FNV1x32();

    private static final int FNV_BASIS = 0x811C_9DC5;
    private static final int FNV_PRIME = 0x0100_0193;

    private FNV1x32() {
    }

    @Override
    public HashCode hash(Bytes input) {
        int hash = FNV_BASIS;
        for (int i = 0, len = input.length(); i < len; i++) {
            hash *= FNV_PRIME;
            hash ^= input.getUnsigned(i);
        }
        return HashCode.ofInt(hash);
    }
}
