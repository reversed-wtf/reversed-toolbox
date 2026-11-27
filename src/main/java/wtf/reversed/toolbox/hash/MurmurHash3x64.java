package wtf.reversed.toolbox.hash;

import wtf.reversed.toolbox.collect.*;

import static wtf.reversed.toolbox.hash.MurmurShared.*;

record MurmurHash3x64(int seed) implements HashFunction {
    private static final long C1 = 0x87C3_7B91_1142_53D5L;
    private static final long C2 = 0x4CF5_AD43_2745_937FL;

    @Override
    public HashCode hash(Bytes input) {
        int length = input.length();
        int offset = 0;

        long h1 = Integer.toUnsignedLong(seed);
        long h2 = h1;

        while (offset <= length - 16) {
            h1 = (Long.rotateLeft(h1 ^ mixK1(input.getLong(offset)), 27) + h2) * 5 + 0x52DC_E729;
            h2 = (Long.rotateLeft(h2 ^ mixK2(input.getLong(offset + 8)), 31) + h1) * 5 + 0x3849_5AB5;
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

        Bytes.Mutable bytes = Bytes.allocate(16)
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
        k2 *= C2;
        k2 = Long.rotateLeft(k2, 33);
        k2 *= C1;
        return k2;
    }

    private long fmix64(long k) {
        k ^= k >>> 33;
        k *= 0xFF51_AFD7_ED55_8CCDL;
        k ^= k >>> 33;
        k *= 0xC4CE_B9FE_1A85_EC53L;
        k ^= k >>> 33;
        return k;
    }
}
