package wtf.reversed.toolbox.compress;

import wtf.reversed.toolbox.collect.*;
import wtf.reversed.toolbox.util.*;

abstract sealed class LZDecompressor implements Decompressor
    permits FastLZDecompressor, LZ4Decompressor {
    LZDecompressor() {
    }

    void copyLiteral(Bytes src, int srcOff, MutableBytes dst, int dstOff, int len) {
        Check.fromIndexSize(srcOff, len, src.size());
        Check.fromIndexSize(dstOff, len, dst.size());

        src.subList(srcOff, srcOff + len).copyTo(dst, dstOff);
    }

    void copyReference(MutableBytes dst, int dstOff, int offset, int length) {
        Check.fromIndexSize(dstOff, length, dst.size());

        int dstPos = dstOff - offset;
        if (offset <= 0 || dstPos < 0) {
            throw new IllegalArgumentException("Invalid match");
        }
        if (offset == 1) {
            dst.fill(dstOff, dstOff + length, dst.getByte(dstOff - 1));
        } else if (offset >= length) {
            dst.slice(dstPos, dstPos + length).copyTo(dst, dstOff);
        } else {
            for (int i = 0; i < length; i++) {
                dst.setByte(dstOff + i, dst.getByte(dstPos + i));
            }
        }
    }
}
