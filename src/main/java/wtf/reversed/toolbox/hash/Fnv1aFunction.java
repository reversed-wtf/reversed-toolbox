package wtf.reversed.toolbox.hash;

import wtf.reversed.toolbox.util.*;

final class Fnv1aFunction extends HashFunction {
    private static final long FNV_OFFSET_BASIS = 0xcbf29ce484222325L;
    private static final long FNV_PRIME = 0x100000001b3L;

    @Override
    public HashCode hash(byte[] input, int off, int len) {
        Check.fromIndexSize(off, len, input.length);
        long hash = FNV_OFFSET_BASIS;
        for (int i = off; i < len; i++) {
            hash ^= input[i];
            hash = hash * FNV_PRIME;
        }
        return HashCode.ofLong(hash);
    }
}
