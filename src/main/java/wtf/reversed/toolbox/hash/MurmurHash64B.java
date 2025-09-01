package wtf.reversed.toolbox.hash;

import wtf.reversed.toolbox.collect.*;

record MurmurHash64B(long seed) implements HashFunction {
    private static final int M32 = 0x5bd1e995;
    private static final int R32 = 24;

    @Override
    public HashCode hash(Bytes input) {
        var length = input.size();
        var offset = 0;

        int h1 = (int) (seed) ^ length;
        int h2 = (int) (seed >>> 32);

        while (offset + 8 <= length) {
            h1 = round(h1, input.getInt(offset));
            h2 = round(h2, input.getInt(offset + 4));
            offset += 8;
        }

        if (offset + 4 <= length) {
            h1 = round(h1, input.getInt(offset));
            offset += 4;
        }

        if (length > offset) {
            h2 ^= readRemainingInt(input, offset);
            h2 *= M32;
        }

        h1 = (h1 ^ (h2 >>> 18)) * M32;
        h2 = (h2 ^ (h1 >>> 22)) * M32;
        h1 = (h1 ^ (h2 >>> 17)) * M32;
        h2 = (h2 ^ (h1 >>> 19)) * M32;

        long l1 = Integer.toUnsignedLong(h1);
        long l2 = Integer.toUnsignedLong(h2);
        return HashCode.ofLong(l1 << 32 | l2);
    }

    private int round(int h, int k) {
        k *= M32;
        k ^= k >>> R32;
        k *= M32;
        h *= M32;
        h ^= k;
        return h;
    }

    private int readRemainingInt(Bytes input, int offset) {
        if (offset + 4 <= input.size()) {
            return input.getInt(offset);
        }

        int result = 0;
        for (int i = offset; i < input.size(); i++) {
            result |= input.getUnsignedByte(i) << (i * 8);
        }
        return result;
    }
}
