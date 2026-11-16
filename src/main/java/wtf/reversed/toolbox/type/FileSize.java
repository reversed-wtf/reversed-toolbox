package wtf.reversed.toolbox.type;

import wtf.reversed.toolbox.util.*;

public final class FileSize extends Amount.OfLong<FileSize> {
    public static final FileSize ZERO = new FileSize(0);

    private FileSize(long bytes) {
        super(Check.nonNegative(bytes, "bytes"));
    }

    public static FileSize ofBytes(long bytes) {
        return bytes == 0 ? ZERO : new FileSize(bytes);
    }

    public long toBytes() {
        return value;
    }

    @Override
    protected FileSize newInstance(long value) {
        return ofBytes(value);
    }

    @Override
    public String toString() {
        if (value < 1024) {
            return value + " B";
        } else if (value < 1024 * 1024) {
            return String.format("%.2f KiB", value / (1024.0));
        } else if (value < 1024 * 1024 * 1024) {
            return String.format("%.2f MiB", value / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GiB", value / (1024.0 * 1024.0 * 1024.0));
        }
    }
}
