package wtf.reversed.toolbox.hash;

import wtf.reversed.toolbox.collect.*;

record MurmurHash3x64(int seed) implements HashFunction {
    private static final long C1 = 0x87c37b91114253d5L;
    private static final long C2 = 0x4cf5ad432745937fL;

    @Override
    public HashCode hash(Bytes input) {
        var h1 = Integer.toUnsignedLong(seed);
        var h2 = Integer.toUnsignedLong(seed);

        int offset = 0;
        int length = input.length();

        while (offset + 16 <= length) {
            h1 = (Long.rotateLeft(h1 ^ mixK1(input.getLong(offset)), 27) + h2) * 5 + 0x52dce729;
            h2 = (Long.rotateLeft(h2 ^ mixK2(input.getLong(offset + 8)), 31) + h1) * 5 + 0x38495ab5;
            offset += 16;
        }

        if (length > offset) {
            h1 ^= mixK1(readRemainingLong(input, offset));
            h2 ^= mixK2(readRemainingLong(input, offset + 8));
        }

        h1 ^= length;
        h2 ^= length;

        h1 += h2;
        h2 += h1;

        h1 = fmix64(h1);
        h2 = fmix64(h2);

        h1 += h2;
        h2 += h1;

        Bytes.Mutable bytes = Bytes.Mutable.allocate(16)
            .setLong(0, h1)
            .setLong(8, h2);

        return HashCode.ofBytes(bytes);
    }

    private long mixK1(long k1) {
        k1 *= C1;
        k1 = Long.rotateLeft(k1, 31);
        k1 *= C2;
        return k1;
    }

    private long mixK2(long k2) {
        k2 = k2 * C2;
        k2 = Long.rotateLeft(k2, 33);
        k2 = k2 * C1;
        return k2;
    }

    private long fmix64(long hash) {
        hash ^= (hash >>> 33);
        hash *= 0xff51afd7ed558ccdL;
        hash ^= (hash >>> 33);
        hash *= 0xc4ceb9fe1a85ec53L;
        hash ^= (hash >>> 33);
        return hash;
    }

    private long readRemainingLong(Bytes input, int offset) {
        if (offset + 8 <= input.length()) {
            return input.getLong(offset);
        }

        long result = 0;
        for (int i = offset; i < input.length(); i++) {
            result |= (long) input.getUnsigned(i) << (i * 8);
        }
        return result;
    }
}
