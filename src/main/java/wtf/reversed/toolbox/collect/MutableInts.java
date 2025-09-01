package wtf.reversed.toolbox.collect;

import wtf.reversed.toolbox.util.*;

import java.nio.*;
import java.util.Arrays;

public final class MutableInts extends Ints {
    private MutableInts(int[] array, int fromIndex, int toIndex) {
        super(array, fromIndex, toIndex);
    }

    public static MutableInts wrap(int[] array) {
        return new MutableInts(array, 0, array.length);
    }

    public static MutableInts wrap(int[] array, int fromIndex, int toIndex) {
        return new MutableInts(array, fromIndex, toIndex);
    }

    public static MutableInts allocate(int size) {
        return new MutableInts(new int[size], 0, size);
    }

    public void setInt(int index, int value) {
        Check.index(index, size());
        array[fromIndex + index] = value;
    }

    public IntBuffer asMutableBuffer() {
        return IntBuffer.wrap(array, fromIndex, size());
    }

    public void fill(int value) {
        Arrays.fill(array, fromIndex, toIndex, value);
    }

    public MutableInts slice(int fromIndex) {
        return slice(fromIndex, size());
    }

    public MutableInts slice(int fromIndex, int toIndex) {
        Check.fromToIndex(fromIndex, toIndex, size());
        return new MutableInts(array, this.fromIndex + fromIndex, this.fromIndex + toIndex);
    }

    @Override
    public Integer set(int index, Integer element) {
        int oldValue = getInt(index);
        setInt(index, element);
        return oldValue;
    }
}
