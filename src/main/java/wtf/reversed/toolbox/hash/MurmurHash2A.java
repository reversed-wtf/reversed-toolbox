package wtf.reversed.toolbox.hash;

import wtf.reversed.toolbox.collect.*;

import static wtf.reversed.toolbox.hash.MurmurShared.*;

record MurmurHash2A(int seed) implements HashFunction {
    @Override
    public HashCode hash(Bytes input) {
        int length = input.length();
        int offset = 0;

        int h = seed;

        while (offset <= length - 4) {
            h = mmix(h, input.getInt(offset));
            offset += 4;
        }

        h = mmix(h, readRemainingInt(input, offset));
        h = mmix(h, length);

        h ^= h >>> 13;
        h *= M;
        h ^= h >>> 15;

        return HashCode.ofInt(h);
    }
}
