package wtf.reversed.toolbox.compress;

import wtf.reversed.toolbox.collect.*;
import wtf.reversed.toolbox.util.*;

import java.util.*;

final class LZMADecompressor implements Decompressor {
    static final LZMADecompressor INSTANCE = new LZMADecompressor();

    private LZMADecompressor() {
    }

    @Override
    public void decompress(Bytes src, Bytes.Mutable dst) {
        try {
            LzmaDecoder.decompress(src, dst);
        } catch (DecompressorException e) {
            throw e;
        } catch (Exception e) {
            throw new DecompressorException("Failed to decompress LZMA data", e);
        }
    }

    private static final class LzmaDecoder {
        private static final int STATES = 12;
        private static final int LITERAL_STATES = 7;
        private static final int MIN_MATCH_LENGTH = 2;
        private static final int POS_STATE_BITS = 4;

        private final Settings settings;
        private final RangeDecoder rangeDecoder;
        private final LiteralDecoder literals;
        private final LengthDecoder lengths = new LengthDecoder();
        private final LengthDecoder repLengths = new LengthDecoder();
        private final DistanceDecoder distances = new DistanceDecoder();

        private final short[] isMatch = new short[STATES << POS_STATE_BITS];
        private final short[] isRep = new short[STATES];
        private final short[] isRep0 = new short[STATES];
        private final short[] isRep1 = new short[STATES];
        private final short[] isRep2 = new short[STATES];
        private final short[] isRep0Long = new short[STATES << POS_STATE_BITS];

        // Short history of recent packet types, only used as probability context;
        // states 0..6 mean the previous packet was a literal
        private int state;
        private int rep0, rep1, rep2, rep3;

        private LzmaDecoder(Settings settings, RangeDecoder rangeDecoder) {
            this.settings = settings;
            this.rangeDecoder = rangeDecoder;
            this.literals = new LiteralDecoder(settings.lc(), settings.lp());

            RangeDecoder.initBitModels(isMatch);
            RangeDecoder.initBitModels(isRep);
            RangeDecoder.initBitModels(isRep0);
            RangeDecoder.initBitModels(isRep1);
            RangeDecoder.initBitModels(isRep2);
            RangeDecoder.initBitModels(isRep0Long);
        }

        private static void decompress(Bytes src, Bytes.Mutable dst) {
            var settings = Settings.of(src.getUnsigned(0), src.getInt(1));
            long uncompressedSize = src.getLong(5);
            if (uncompressedSize != -1 && uncompressedSize != dst.length()) {
                throw new DecompressorException("Size mismatch: header=" + uncompressedSize + ", destination=" + dst.length());
            }
            new LzmaDecoder(settings, new RangeDecoder(src.slice(13)))
                .decode(dst, uncompressedSize == -1);
        }

        private void decode(Bytes.Mutable dst, boolean endMarkerRequired) {
            int dstOff = 0;
            while (true) {
                if (dstOff == dst.length() && !endMarkerRequired) {
                    if (rangeDecoder.isFinishedOk()) {
                        break;
                    }
                    endMarkerRequired = true;
                }

                int posState = dstOff & settings.pbMask();
                if (rangeDecoder.decodeBit(isMatch, (state << POS_STATE_BITS) + posState) == 0) {
                    decodeLiteral(dst, dstOff++);
                    continue;
                }

                int length = rangeDecoder.decodeBit(isRep, state) == 0 ? decodeMatch(posState) : decodeRep(posState);
                if (rep0 == -1) {                       // end marker
                    if (dstOff != dst.length()) {
                        throw new DecompressorException("End marker at wrong position: position=" + dstOff + ", expected=" + dst.length());
                    }
                    break;
                }
                if (rep0 >= settings.dictionarySize()) {
                    throw new DecompressorException("Distance exceeds dictionary size: distance=" + (rep0 + 1) + ", dictionarySize=" + settings.dictionarySize());
                }
                LZUtils.copyReference(dst, dstOff, rep0 + 1, length);
                dstOff += length;
            }
            if (!rangeDecoder.isFinishedOk()) {
                throw new DecompressorException("Range decoder did not finish cleanly");
            }
        }

        private void decodeLiteral(Bytes.Mutable dst, int dstOff) {
            byte prevByte = dstOff == 0 ? 0 : dst.get(dstOff - 1);
            byte value = state < LITERAL_STATES
                ? literals.decode(rangeDecoder, dstOff, prevByte)
                : literals.decodeMatched(rangeDecoder, dstOff, prevByte, dst.get(dstOff - rep0 - 1));
            dst.set(dstOff, value);
            state = state < 4 ? 0 : state < 10 ? state - 3 : state - 6;
        }

        private int decodeMatch(int posState) {
            int length = lengths.decode(rangeDecoder, posState);
            rep3 = rep2;
            rep2 = rep1;
            rep1 = rep0;
            rep0 = distances.decode(rangeDecoder, length);
            state = state < LITERAL_STATES ? 7 : 10;
            return MIN_MATCH_LENGTH + length;
        }

        private int decodeRep(int posState) {
            if (rangeDecoder.decodeBit(isRep0, state) == 0) {
                if (rangeDecoder.decodeBit(isRep0Long, (state << POS_STATE_BITS) + posState) == 0) {
                    // SHORTREP
                    state = state < LITERAL_STATES ? 9 : 11;
                    return 1;
                }
            } else {
                int distance = rep1;     // LONGREP[1]
                if (rangeDecoder.decodeBit(isRep1, state) != 0) {
                    distance = rep2;     // LONGREP[2]
                    if (rangeDecoder.decodeBit(isRep2, state) != 0) {
                        distance = rep3; // LONGREP[3]
                        rep3 = rep2;
                    }
                    rep2 = rep1;
                }
                rep1 = rep0;
                rep0 = distance;
            }
            state = state < LITERAL_STATES ? 8 : 11;
            return MIN_MATCH_LENGTH + repLengths.decode(rangeDecoder, posState);
        }
    }

    private record Settings(
        int lc,
        int lp,
        int pb,
        int dictionarySize
    ) {
        private static final int LZMA_DIC_MIN = 1 << 12;

        private Settings {
            Check.between(lc, 0, 8, "lc");
            Check.between(lp, 0, 4, "lp");
            Check.between(pb, 0, 4, "pb");
            Check.nonNegative(dictionarySize, "dictionarySize");
            dictionarySize = Math.max(dictionarySize, LZMA_DIC_MIN);
        }

        private static Settings of(int props, int dictionarySize) {
            Check.between(props, 0, 9 * 5 * 5 - 1, "props");
            int lc = props % 9;
            props /= 9;
            int lp = props % 5;
            int pb = props / 5;

            return new Settings(lc, lp, pb, dictionarySize);
        }

        private int pbMask() {
            return (1 << pb) - 1;
        }
    }

    private static final class RangeDecoder {
        private static final int TOP_MASK = -(1 << 24);
        private static final int NUM_BIT_MODEL_TOTAL_BITS = 11;
        private static final int BIT_MODEL_TOTAL = (1 << NUM_BIT_MODEL_TOTAL_BITS);
        private static final int NUM_MOVE_BITS = 5;

        private final Bytes source;
        private int offset = 0;
        private int range;
        private int code;

        private RangeDecoder(Bytes source) {
            this.source = source;

            if (readByte() != 0) {
                throw new DecompressorException("Invalid first byte: expected 0");
            }
            range = 0xFFFF_FFFF;
            code = Integer.reverseBytes(source.getInt(offset)); // big-endian
            offset += Integer.BYTES;
        }

        private boolean isFinishedOk() {
            return code == 0;
        }

        private int readByte() {
            return source.getUnsigned(offset++);
        }

        private int decodeDirectBits(int count) {
            int result = 0;
            for (int i = count; i != 0; i--) {
                range >>>= 1;
                int t = ((code - range) >>> 31);
                code -= range & (t - 1);
                result = (result << 1) | (1 - t);

                refill();
            }
            return result;
        }

        private int decodeBit(short[] probs, int index) {
            int prob = probs[index];
            int newBound = (range >>> NUM_BIT_MODEL_TOTAL_BITS) * prob;
            int bit;
            if (Integer.compareUnsigned(code, newBound) < 0) {
                range = newBound;
                probs[index] = (short) (prob + ((BIT_MODEL_TOTAL - prob) >>> NUM_MOVE_BITS));
                bit = 0;
            } else {
                range -= newBound;
                code -= newBound;
                probs[index] = (short) (prob - (prob >>> NUM_MOVE_BITS));
                bit = 1;
            }
            refill();
            return bit;
        }

        private int decodeBitTree(short[] probs, int base, int bits) {
            int m = 1;
            for (int bitIndex = bits; bitIndex != 0; bitIndex--) {
                m = (m << 1) + decodeBit(probs, base + m);
            }
            return m - (1 << bits);
        }

        private int decodeReverseBitTree(short[] probs, int base, int bits) {
            int m = 1;
            int symbol = 0;
            for (int i = 0; i < bits; i++) {
                int bit = decodeBit(probs, base + m);
                m = (m << 1) + bit;
                symbol |= bit << i;
            }
            return symbol;
        }

        private void refill() {
            if ((range & TOP_MASK) == 0) {
                code = (code << 8) | readByte();
                range <<= 8;
            }
        }

        private static void initBitModels(short[] probs) {
            Arrays.fill(probs, (short) (BIT_MODEL_TOTAL >>> 1));
        }
    }

    private static final class LiteralDecoder {
        private static final int CONTEXT_SIZE = 0x300;

        private final short[] probs;
        private final int lc;
        private final int posMask;

        private LiteralDecoder(int lc, int lp) {
            this.lc = lc;
            this.posMask = (1 << lp) - 1;
            this.probs = new short[CONTEXT_SIZE << (lc + lp)];
            RangeDecoder.initBitModels(probs);
        }

        private byte decode(RangeDecoder rangeDecoder, int pos, byte prevByte) {
            int offset = contextOffset(pos, prevByte);

            int symbol = 1;
            while (symbol < 0x100) {
                symbol = (symbol << 1) | rangeDecoder.decodeBit(probs, offset + symbol);
            }
            return (byte) symbol;
        }

        private byte decodeMatched(RangeDecoder rangeDecoder, int pos, byte prevByte, byte matchByte) {
            int offset = contextOffset(pos, prevByte);
            int match = Byte.toUnsignedInt(matchByte);

            int symbol = 1;
            while (symbol < 0x100) {
                match <<= 1;
                int matchBit = (match >>> 8) & 1;
                int bit = rangeDecoder.decodeBit(probs, offset + ((1 + matchBit) << 8) + symbol);
                symbol = (symbol << 1) | bit;
                if (bit != matchBit) {
                    break;
                }
            }

            while (symbol < 0x100) {
                symbol = (symbol << 1) | rangeDecoder.decodeBit(probs, offset + symbol);
            }

            return (byte) symbol;
        }

        private int contextOffset(int pos, byte prevByte) {
            return CONTEXT_SIZE * (((pos & posMask) << lc) + (Byte.toUnsignedInt(prevByte) >>> (8 - lc)));
        }
    }

    private static final class LengthDecoder {
        private static final int NUM_POS_STATES_MAX = 1 << LzmaDecoder.POS_STATE_BITS;
        private static final int LOW_BITS = 3;
        private static final int MID_BITS = 3;
        private static final int HIGH_BITS = 8;
        private static final int MID_OFFSET = 1 << LOW_BITS;
        private static final int HIGH_OFFSET = MID_OFFSET + (1 << MID_BITS);

        private final short[] choice = new short[2];
        private final short[] lowTrees = new short[NUM_POS_STATES_MAX << LOW_BITS];
        private final short[] midTrees = new short[NUM_POS_STATES_MAX << MID_BITS];
        private final short[] highTree = new short[1 << HIGH_BITS];

        private LengthDecoder() {
            RangeDecoder.initBitModels(choice);
            RangeDecoder.initBitModels(lowTrees);
            RangeDecoder.initBitModels(midTrees);
            RangeDecoder.initBitModels(highTree);
        }

        private int decode(RangeDecoder rangeDecoder, int posState) {
            if (rangeDecoder.decodeBit(choice, 0) == 0) {
                return rangeDecoder.decodeBitTree(lowTrees, posState << LOW_BITS, LOW_BITS);
            }
            if (rangeDecoder.decodeBit(choice, 1) == 0) {
                return MID_OFFSET + rangeDecoder.decodeBitTree(midTrees, posState << MID_BITS, MID_BITS);
            }
            return HIGH_OFFSET + rangeDecoder.decodeBitTree(highTree, 0, HIGH_BITS);
        }
    }

    private static final class DistanceDecoder {
        private static final int SLOT_BITS = 6;
        private static final int SLOT_TREES = 4;

        private static final int FIRST_EXTRA_BITS_SLOT = 4;
        private static final int FIRST_DIRECT_SLOT = 14;
        private static final int FULLY_MODELED_DISTANCES = 1 << (FIRST_DIRECT_SLOT / 2);
        private static final int ALIGN_BITS = 4;

        private final short[] slotTrees = new short[SLOT_TREES << SLOT_BITS];
        private final short[] extraBitTrees = new short[FULLY_MODELED_DISTANCES - FIRST_DIRECT_SLOT];
        private final short[] alignTree = new short[1 << ALIGN_BITS];

        private DistanceDecoder() {
            RangeDecoder.initBitModels(slotTrees);
            RangeDecoder.initBitModels(extraBitTrees);
            RangeDecoder.initBitModels(alignTree);
        }

        private int decode(RangeDecoder rangeDecoder, int length) {
            int slot = rangeDecoder.decodeBitTree(slotTrees, slotTreeIndex(length) << SLOT_BITS, SLOT_BITS);
            if (slot < FIRST_EXTRA_BITS_SLOT) {
                return slot;
            }

            int extraBits = (slot >> 1) - 1;
            int base = (2 | (slot & 1)) << extraBits;
            if (slot < FIRST_DIRECT_SLOT) {
                return base + rangeDecoder.decodeReverseBitTree(extraBitTrees, base - slot - 1, extraBits);
            }
            int direct = rangeDecoder.decodeDirectBits(extraBits - ALIGN_BITS) << ALIGN_BITS;
            return base + direct + rangeDecoder.decodeReverseBitTree(alignTree, 0, ALIGN_BITS);
        }

        private int slotTreeIndex(int length) {
            return length < SLOT_TREES ? length : SLOT_TREES - 1;
        }
    }
}
