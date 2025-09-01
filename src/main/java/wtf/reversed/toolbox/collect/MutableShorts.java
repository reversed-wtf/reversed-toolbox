package wtf.reversed.toolbox.collect;

import wtf.reversed.toolbox.util.*;

import java.nio.*;
import java.util.Arrays;

public final class MutableShorts extends Shorts {
    private MutableShorts(short[] array, int fromIndex, int toIndex) {
        super(array, fromIndex, toIndex);
    }

    public static MutableShorts wrap(short[] array) {
        return new MutableShorts(array, 0, array.length);
    }

    public static MutableShorts wrap(short[] array, int fromIndex, int toIndex) {
        return new MutableShorts(array, fromIndex, toIndex);
    }

    public static MutableShorts allocate(int size) {
        return new MutableShorts(new short[size], 0, size);
    }

    public void setShort(int index, short value) {
        Check.index(index, size());
        array[fromIndex + index] = value;
    }

    public ShortBuffer asMutableBuffer() {
        return ShortBuffer.wrap(array, fromIndex, size());
    }

    public void fill(int fromIndex, int toIndex, short value) {
        Check.fromToIndex(fromIndex, toIndex, size());
        Arrays.fill(array, this.fromIndex + fromIndex, this.fromIndex + toIndex, value);
    }

    @Override
    public Short set(int index, Short element) {
        short oldValue = getShort(index);
        setShort(index, element);
        return oldValue;
    }
}
