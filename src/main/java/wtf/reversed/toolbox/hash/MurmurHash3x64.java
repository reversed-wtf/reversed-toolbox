package wtf.reversed.toolbox.hash;

import wtf.reversed.toolbox.collect.*;

import java.nio.*;

final class MurmurHash3x64 implements HashFunction {
    private final int seed;

    MurmurHash3x64(int seed) {
        this.seed = seed;
    }

    @Override
    public HashCode hash(Bytes input) {
        var h1 = Integer.toUnsignedLong(seed);
        var h2 = Integer.toUnsignedLong(seed);

        int offset = 0;
        int length = input.size();

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

        // TODO: Replace with MutableBytes if they get the putLong methods
        return HashCode.ofBytes(ByteBuffer
            .allocate(16)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putLong(0, h1)
            .putLong(8, h2)
            .array());
    }

    private static long readRemainingLong(Bytes input, int offset) {
        if (offset + 8 >= input.size()) {
            return input.getLong(offset);
        }

        long result = 0;
        for (int i = offset; i < input.size(); i++) {
            result |= (long) input.getUnsignedByte(i) << (i * 8);
        }
        return result;
    }

    private static byte[] mmh3(byte[] data, int off, int len, long seed) {
        var src = ByteBuffer.wrap(data, off, len).order(ByteOrder.LITTLE_ENDIAN);
        var dst = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);

        var h1 = seed;
        var h2 = seed;

        while (src.remaining() >= 16) {
            h1 = (Long.rotateLeft(h1 ^ mixK1(src.getLong()), 27) + h2) * 5 + 0x52dce729;
            h2 = (Long.rotateLeft(h2 ^ mixK2(src.getLong()), 31) + h1) * 5 + 0x38495ab5;
        }

        if (src.hasRemaining()) {
            dst.put(src);
            h1 ^= mixK1(dst.getLong(0));
            h2 ^= mixK2(dst.getLong(8));
        }

        h1 ^= len;
        h2 ^= len;

        h1 += h2;
        h2 += h1;

        h1 = fmix64(h1);
        h2 = fmix64(h2);

        h1 += h2;
        h2 += h1;

        return dst.putLong(0, h1).putLong(8, h2).array();
    }

    private static long mixK1(long k1) {
        return Long.rotateLeft(k1 * 0x87c37b91114253d5L, 31) * 0x4cf5ad432745937fL;
    }

    private static long mixK2(long k2) {
        return Long.rotateLeft(k2 * 0x4cf5ad432745937fL, 33) * 0x87c37b91114253d5L;
    }

    private static long fmix64(long hash) {
        hash ^= (hash >>> 33);
        hash *= 0xff51afd7ed558ccdL;
        hash ^= (hash >>> 33);
        hash *= 0xc4ceb9fe1a85ec53L;
        hash ^= (hash >>> 33);
        return hash;
    }
}
