package wtf.reversed.toolbox.hash;

import wtf.reversed.toolbox.collect.*;

final class FarmHashFingerprint64 implements HashFunction {
    static final FarmHashFingerprint64 INSTANCE = new FarmHashFingerprint64();

    // Some primes between 2^63 and 2^64 for various uses.
    private static final long K0 = 0xC3A5_C85C_97CB_3127L;
    private static final long K1 = 0xB492_B66F_BE98_F273L;
    private static final long K2 = 0x9AE1_6A3B_2F90_404FL;
    private static final long SEED = 81;

    private FarmHashFingerprint64() {
    }

    @Override
    public HashCode hash(Bytes src) {
        return HashCode.ofLong(fingerprint(src));
    }

    private static long fingerprint(Bytes s) {
        int length = s.length();
        if (length <= 32) {
            if (length <= 16) {
                return hashLength0to16(s);
            } else {
                return hashLength17to32(s);
            }
        } else if (length <= 64) {
            return hashLength33to64(s);
        } else {
            return hashLength65Plus(s);
        }
    }

    private static long shiftMix(long val) {
        return val ^ (val >>> 47);
    }

    private static long hashLength16(long u, long v, long mul) {
        long a = (u ^ v) * mul;
        a ^= (a >>> 47);
        long b = (v ^ a) * mul;
        b ^= (b >>> 47);
        b *= mul;
        return b;
    }

    private static long weakHashLength32WithSeedsLo(Bytes s, int offset, long a) {
        long w = s.getLong(offset);
        long x = s.getLong(offset + 8);
        long y = s.getLong(offset + 16);
        long z = s.getLong(offset + 24);
        return a + w + x + y + z;
    }

    private static long weakHashLength32WithSeedsHi(Bytes s, int offset, long a, long b) {
        long w = s.getLong(offset);
        long x = s.getLong(offset + 8);
        long y = s.getLong(offset + 16);
        long z = s.getLong(offset + 24);

        long aw = a + w;
        return Long.rotateRight(b + aw + z, 21)
            + Long.rotateRight(aw + x + y, 44)
            + aw;
    }

    private static long hashLength0to16(Bytes s) {
        int length = s.length();
        if (length >= 8) {
            long mul = K2 + length * 2L;
            long a = s.getLong(0) + K2;
            long b = s.getLong(s.length() - 8);
            long c = Long.rotateRight(b, 37) * mul + a;
            long d = (Long.rotateRight(a, 25) + b) * mul;
            return hashLength16(c, d, mul);
        }
        if (length >= 4) {
            long mul = K2 + length * 2L;
            long a = s.getUnsignedInt(0);
            long b = s.getUnsignedInt(s.length() - 4);
            return hashLength16(length + (a << 3), b, mul);
        }
        if (length > 0) {
            int a = s.getUnsigned(0);
            int b = s.getUnsigned(length >>> 1);
            int c = s.getUnsigned(length - 1);

            int y = a + (b << 8);
            int z = length + (c << 2);
            return shiftMix(y * K2 ^ z * K0) * K2;
        }
        return K2;
    }

    private static long hashLength17to32(Bytes s) {
        int length = s.length();
        long mul = K2 + length * 2L;
        long a = s.getLong(0) * K1;
        long b = s.getLong(8);
        long c = s.getLong(length - 8) * mul;
        long d = s.getLong(length - 16) * K2;

        return hashLength16(Long.rotateRight(a + b, 43) + Long.rotateRight(c, 30) + d, a + Long.rotateRight(b + K2, 18) + c, mul);
    }

    private static long hashLength33to64(Bytes s) {
        int length = s.length();
        long mul = K2 + length * 2L;
        long a = s.getLong(0) * K2;
        long b = s.getLong(8);
        long c = s.getLong(length - 8) * mul;
        long d = s.getLong(length - 16) * K2;
        long y = Long.rotateRight(a + b, 43) + Long.rotateRight(c, 30) + d;
        long z = hashLength16(y, a + Long.rotateRight(b + K2, 18) + c, mul);
        long e = s.getLong(16) * mul;
        long f = s.getLong(24);
        long g = (y + s.getLong(length - 32)) * mul;
        long h = (z + s.getLong(length - 24)) * mul;

        return hashLength16(Long.rotateRight(e + f, 43) + Long.rotateRight(g, 30) + h, e + Long.rotateRight(f + a, 18) + g, mul);
    }

    private static long hashLength65Plus(Bytes s) {
        // For strings over 64 bytes we loop.  Internal state consists of
        // 56 bytes: v, w, x, y, and z.
        int length = s.length();

        long x = SEED;
        @SuppressWarnings("NumericOverflow")
        long y = SEED * K1 + 113;
        long z = shiftMix(y * K2 + 113) * K2;
        long vLo = 0, vHi = 0;
        long wLo = 0, wHi = 0;
        x = x * K2 + s.getLong(0);

        int offset = 0;
        do {
            x = Long.rotateRight(x + y + vLo + s.getLong(offset + 8), 37) * K1;
            y = Long.rotateRight(y + vHi + s.getLong(offset + 48), 42) * K1;
            x ^= wHi;
            y += vLo + s.getLong(offset + 40);
            z = Long.rotateRight(z + wLo, 33) * K1;
            long t = s.getLong(offset + 16);
            vLo = weakHashLength32WithSeedsLo(s, offset, vHi * K1);
            vHi = weakHashLength32WithSeedsHi(s, offset, vHi * K1, x + wLo);
            wLo = weakHashLength32WithSeedsLo(s, offset + 32, z + wHi);
            wHi = weakHashLength32WithSeedsHi(s, offset + 32, z + wHi, y + t);
            t = x;
            x = z;
            z = t;
            offset += 64;
        } while (offset + 64 < length);

        long mul = K1 + ((z & 0xff) << 1);
        // Make offset point to the last 64 bytes of input.
        offset = s.length() - 64;
        wLo += ((length - 1) & 63);
        vLo += wLo;
        wLo += vLo;
        x = Long.rotateRight(x + y + vLo + s.getLong(offset + 8), 37) * mul;
        y = Long.rotateRight(y + vHi + s.getLong(offset + 48), 42) * mul;
        x ^= wHi * 9;
        y += vLo * 9 + s.getLong(offset + 40);
        z = Long.rotateRight(z + wLo, 33) * mul;
        long t = s.getLong(offset + 16);
        vLo = weakHashLength32WithSeedsLo(s, offset, vHi * mul);
        vHi = weakHashLength32WithSeedsHi(s, offset, vHi * mul, x + wLo);
        wLo = weakHashLength32WithSeedsLo(s, offset + 32, z + wHi);
        wHi = weakHashLength32WithSeedsHi(s, offset + 32, z + wHi, y + t);
        return hashLength16(hashLength16(vLo, wLo, mul) + shiftMix(y) * K0 + x, hashLength16(vHi, wHi, mul) + z, mul);
    }
}
