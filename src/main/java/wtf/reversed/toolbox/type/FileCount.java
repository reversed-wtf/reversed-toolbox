package wtf.reversed.toolbox.type;

import wtf.reversed.toolbox.util.*;

import java.text.*;

public final class FileCount extends Amount.OfLong<FileCount> {
    public static final FileCount ZERO = new FileCount(0);

    private FileCount(long count) {
        super(Check.nonNegative(count, "count"));
    }

    public static FileCount of(long count) {
        return count == 0 ? ZERO : new FileCount(count);
    }

    @Override
    protected FileCount newInstance(long value) {
        return of(value);
    }

    @Override
    public String toString() {
        if (value == 0) {
            return "no files";
        } else if (value == 1) {
            return "one file";
        } else {
            return NumberFormat.getIntegerInstance().format(value) + " files";
        }
    }
}
