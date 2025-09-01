package wtf.reversed.toolbox.collect;

import wtf.reversed.toolbox.util.*;

import java.nio.*;
import java.util.Arrays;

public final class MutableLongs extends Longs {
    private MutableLongs(long[] array, int fromIndex, int toIndex) {
        super(array, fromIndex, toIndex);
    }

    public static MutableLongs wrap(long[] array) {
        return new MutableLongs(array, 0, array.length);
    }

    public static MutableLongs wrap(long[] array, int fromIndex, int toIndex) {
        return new MutableLongs(array, fromIndex, toIndex);
    }

    public static MutableLongs allocate(int size) {
        return new MutableLongs(new long[size], 0, size);
    }

    public void setLong(int index, long value) {
        Check.index(index, size());
        array[fromIndex + index] = value;
    }

    public LongBuffer asMutableBuffer() {
        return LongBuffer.wrap(array, fromIndex, size());
    }

    public void fill(int fromIndex, int toIndex, long value) {
        Check.fromToIndex(fromIndex, toIndex, size());
        Arrays.fill(array, this.fromIndex + fromIndex, this.fromIndex + toIndex, value);
    }

    @Override
    public Long set(int index, Long element) {
        long oldValue = getLong(index);
        setLong(index, element);
        return oldValue;
    }
}
