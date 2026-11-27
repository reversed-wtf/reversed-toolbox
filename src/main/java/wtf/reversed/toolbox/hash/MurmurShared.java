package wtf.reversed.toolbox.hash;

import wtf.reversed.toolbox.collect.*;

final class MurmurShared {
    static final int M = 0x5BD1_E995;
    static final int R = 24;

    private MurmurShared() {
    }

    static int mmix(int h, int k) {
        k *= M;
        k ^= k >>> R;
        k *= M;
        h *= M;
        h ^= k;
        return h;
    }

    static int readRemainingInt(Bytes input, int offset) {
        int result = 0;
        for (int i = offset; i < input.length(); i++) {
            // Shift works because of implicit mask with 31
            result |= input.getUnsigned(i) << (i * 8);
        }
        return result;
    }

    static long readRemainingLong(Bytes input, int offset) {
        if (offset <= input.length() - 8) {
            return input.getLong(offset);
        }

        long result = 0;
        for (int i = offset; i < input.length(); i++) {
            // Shift works because of implicit mask with 63
            result |= (long) input.getUnsigned(i) << (i * 8);
        }
        return result;
    }
}
