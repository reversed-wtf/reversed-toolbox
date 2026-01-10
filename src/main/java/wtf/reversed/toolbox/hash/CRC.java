package wtf.reversed.toolbox.hash;

import wtf.reversed.toolbox.collect.*;

import java.util.*;

/**
 * Calculates the CRC based on a description of an algorithm
 */
final class CRC implements HashFunction {
    private final CRCAlgorithm algorithm;
    private final long[] table;
    private long crc;

    CRC(CRCAlgorithm algorithm) {
        this.algorithm = Objects.requireNonNull(algorithm);
        this.table = generateTable(algorithm.width(), algorithm.poly(), algorithm.refIn());

        if (algorithm.refIn()) {
            this.crc = Long.reverse(algorithm.init()) >>> (Long.SIZE - algorithm.width());
        } else {
            this.crc = algorithm.init() << (Long.SIZE - algorithm.width());
        }
    }

    private static long[] generateTable(int width, long poly, boolean refIn) {
        poly = refIn ? Long.reverse(poly) >>> (Long.SIZE - width) : poly << (Long.SIZE - width);

        var table = new long[256];
        for (int i = 0; i < table.length; i++) {
            table[i] = crc64(poly, refIn, i);
        }
        return table;
    }

    private static long crc64(long poly, boolean refIn, long value) {
        if (refIn) {
            for (int i = 0; i < 8; i++) {
                value = (value >>> 1) ^ ((value & 1) * poly);
            }
        } else {
            value <<= Long.SIZE - 8;
            for (int i = 0; i < 8; i++) {
                value = (value << 1) ^ (((value >>> Long.SIZE - 1) & 1) * poly);
            }
        }
        return value;
    }

    @Override
    public HashCode hash(Bytes src) {
        update(src);
        return algorithm.width() <= 32
            ? HashCode.ofInt((int) getCrc())
            : HashCode.ofLong(getCrc());
    }

    private void update(Bytes bytes) {
        if (algorithm.refIn()) {
            for (int i = 0; i < bytes.length(); i++) {
                int table_index = ((int) (crc ^ bytes.getUnsigned(i)) & 0xFF);
                crc = table[table_index] ^ (crc >>> 8);
            }
        } else {
            for (int i = 0; i < bytes.length(); i++) {
                int table_index = ((int) ((crc >>> Long.SIZE - 8) ^ bytes.getUnsigned(i)) & 0xFF);
                crc = table[table_index] ^ (crc << 8);
            }
        }
    }

    private long getCrc() {
        var crc = this.crc;
        if (algorithm.refIn() ^ algorithm.refOut()) {
            crc = Long.reverse(crc);
        }
        if (!algorithm.refOut()) {
            crc >>>= Long.SIZE - algorithm.width();
        }
        return crc ^ algorithm.xorOut();
    }
}
