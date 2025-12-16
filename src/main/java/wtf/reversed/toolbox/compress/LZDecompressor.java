package wtf.reversed.toolbox.compress;

import wtf.reversed.toolbox.collect.*;
import wtf.reversed.toolbox.util.*;

abstract sealed class LZDecompressor implements Decompressor
    permits FastLZDecompressor, LZ4Decompressor {
    LZDecompressor() {
    }

    void copyLiteral(Bytes src, int srcOff, MutableBytes dst, int dstOff, int len) {
        Check.fromIndexSize(srcOff, len, src.length());
        Check.fromIndexSize(dstOff, len, dst.length());

        src.slice(srcOff, len).copyTo(dst, dstOff);
    }

    void copyReference(MutableBytes dst, int dstOff, int offset, int length) {
        Check.fromIndexSize(dstOff, length, dst.length());
        Check.argument(offset > 0 && dstOff - offset >= 0, "Invalid match");

        int dstPos = dstOff - offset;
        if (offset == 1) {
            dst.slice(dstOff, length).fill(dst.get(dstOff - 1));
        } else if (offset >= length) {
            dst.slice(dstPos, length).copyTo(dst, dstOff);
        } else {
            for (int i = 0; i < length; i++) {
                dst.set(dstOff + i, dst.get(dstPos + i));
            }
        }
    }
}
