package wtf.reversed.toolbox.compress;

import wtf.reversed.toolbox.collect.*;
import wtf.reversed.toolbox.util.*;

final class LZUtils {
    private LZUtils() {
    }

    static void copyLiteral(Bytes src, int srcOff, Bytes.Mutable dst, int dstOff, int len) {
        src.slice(srcOff, len).copyTo(dst, dstOff);
    }

    static void copyReference(Bytes.Mutable dst, int dstOff, int offset, int length) {
        Check.argument(offset > 0 && dstOff - offset >= 0, "Match before start");

        int srcPos = dstOff - offset;
        if (offset == 1) {
            byte b = dst.get(dstOff - 1);
            dst.slice(dstOff, length).fill(b);
        } else if (offset >= length) {
            dst.slice(srcPos, length).copyTo(dst, dstOff);
        } else {
            dst.slice(srcPos, offset).copyTo(dst, dstOff);
            int copied = offset;
            while (copied < length) {
                int chunk = Math.min(copied, length - copied);
                dst.slice(dstOff, chunk).copyTo(dst, dstOff + copied);
                copied += chunk;
            }
        }
    }
}
