package wtf.reversed.toolbox.hash;

import wtf.reversed.toolbox.collect.*;

record XXHash32(int seed) implements HashFunction {
    private static final int PRIME32_1 = 0x9e3779b1;
    private static final int PRIME32_2 = 0x85ebca77;
    private static final int PRIME32_3 = 0xc2b2ae3d;
    private static final int PRIME32_4 = 0x27d4eb2f;
    private static final int PRIME32_5 = 0x165667b1;

    @Override
    public HashCode hash(Bytes input) {
        var length = input.size();
        var offset = 0;

        int acc;
        if (offset + 16 <= length) {
            // Step 1: Initialize internal accumulators
            int acc1 = seed + PRIME32_1 + PRIME32_2;
            int acc2 = seed + PRIME32_2;
            int acc3 = seed;
            int acc4 = seed - PRIME32_1;

            // Step 2: Process stripes
            do {
                acc1 = round(acc1, input.getInt(offset));
                acc2 = round(acc2, input.getInt(offset + 4));
                acc3 = round(acc3, input.getInt(offset + 8));
                acc4 = round(acc4, input.getInt(offset + 12));
                offset += 16;
            } while (offset + 16 <= length);

            // Step 3: Accumulator convergence
            acc = Integer.rotateLeft(acc1, 1)
                + Integer.rotateLeft(acc2, 7)
                + Integer.rotateLeft(acc3, 12)
                + Integer.rotateLeft(acc4, 18);
        } else {
            // Special case: input is less than 16 bytes
            acc = seed + PRIME32_5;
        }

        // Step 4: Add input length
        acc = acc + length;

        // Step 5: Consume remaining input
        while (offset + 4 <= length) {
            int lane = input.getInt(offset);
            acc = acc + (lane * PRIME32_3);
            acc = Integer.rotateLeft(acc, 17) * PRIME32_4;
            offset += 4;
        }

        while (offset + 1 <= length) {
            int lane = input.getUnsignedByte(offset);
            acc = acc + (lane * PRIME32_5);
            acc = Integer.rotateLeft(acc, 11) * PRIME32_1;
            offset++;
        }

        // Step 6: Final mix (avalanche)
        acc = acc ^ (acc >>> 15);
        acc = acc * PRIME32_2;
        acc = acc ^ (acc >>> 13);
        acc = acc * PRIME32_3;
        acc = acc ^ (acc >>> 16);

        return HashCode.ofInt(acc);
    }

    private static int round(int acc, int lane) {
        acc = acc + (lane * PRIME32_2);
        acc = Integer.rotateLeft(acc, 13);
        return acc * PRIME32_1;
    }
}
