package wtf.reversed.toolbox.hash;

import wtf.reversed.toolbox.collect.*;

import static wtf.reversed.toolbox.hash.MurmurShared.*;

record MurmurHash64B(long seed) implements HashFunction {
    @Override
    public HashCode hash(Bytes input) {
        int length = input.length();
        int offset = 0;

        int h1 = (int) seed ^ length;
        int h2 = (int) (seed >>> 32);

        while (offset <= length - 8) {
            h1 = mmix(h1, input.getInt(offset));
            h2 = mmix(h2, input.getInt(offset + 4));
            offset += 8;
        }

        if (offset <= length - 4) {
            h1 = mmix(h1, input.getInt(offset));
            offset += 4;
        }

        if (length > offset) {
            h2 ^= readRemainingInt(input, offset);
            h2 *= M;
        }

        h1 = (h1 ^ (h2 >>> 18)) * M;
        h2 = (h2 ^ (h1 >>> 22)) * M;
        h1 = (h1 ^ (h2 >>> 17)) * M;
        h2 = (h2 ^ (h1 >>> 19)) * M;

        return HashCode.ofLong((long) h1 << 32 | Integer.toUnsignedLong(h2));
    }
}
