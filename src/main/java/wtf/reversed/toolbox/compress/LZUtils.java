package wtf.reversed.toolbox.compress;

import wtf.reversed.toolbox.collect.*;

final class LZUtils {
    private LZUtils() {
    }

    static void copyLiteral(Bytes src, int srcOff, Bytes.Mutable dst, int dstOff, int len) {
        try {
            src.slice(srcOff, len).copyTo(dst, dstOff);
        } catch (IndexOutOfBoundsException e) {
            throw new DecompressorException("Literal out of bounds: " +
                "source=" + srcOff + ", target=" + dstOff + ", length=" + len, e);
        }
    }

    static void copyReference(Bytes.Mutable dst, int dstOff, int offset, int length) {
        if (offset <= 0) {
            throw new DecompressorException("Backreference out of bounds: " +
                "position=" + dstOff + ", offset=" + offset + ", length=" + length);
        }

        try {
            int srcPos = dstOff - offset;
            if (offset == 1) {
                byte b = dst.get(dstOff - 1);
                dst.slice(dstOff, length).fill(b);
            } else if (offset >= length) {
                dst.slice(srcPos, length).copyTo(dst, dstOff);
            } else {
                dst.slice(srcPos, offset).copyTo(dst, dstOff);
                int copied = offset;
                do {
                    int chunk = Math.min(copied, length - copied);
                    dst.slice(dstOff, chunk).copyTo(dst, dstOff + copied);
                    copied += chunk;
                } while (copied < length);
            }
        } catch (IndexOutOfBoundsException e) {
            throw new DecompressorException("Backreference out of bounds: " +
                "position=" + dstOff + ", offset=" + offset + ", length=" + length);
        }
    }
}
