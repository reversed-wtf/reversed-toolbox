package wtf.reversed.toolbox.collect;

import wtf.reversed.toolbox.util.*;

import java.nio.*;
import java.util.Arrays;

public final class MutableFloats extends Floats {
    private MutableFloats(float[] array, int fromIndex, int toIndex) {
        super(array, fromIndex, toIndex);
    }

    public static MutableFloats wrap(float[] array) {
        return new MutableFloats(array, 0, array.length);
    }

    public static MutableFloats wrap(float[] array, int fromIndex, int toIndex) {
        return new MutableFloats(array, fromIndex, toIndex);
    }

    public static MutableFloats allocate(int size) {
        return new MutableFloats(new float[size], 0, size);
    }

    public void setFloat(int index, float value) {
        Check.index(index, size());
        array[fromIndex + index] = value;
    }

    public FloatBuffer asMutableBuffer() {
        return FloatBuffer.wrap(array, fromIndex, size());
    }

    public void fill(int fromIndex, int toIndex, float value) {
        Check.fromToIndex(fromIndex, toIndex, size());
        Arrays.fill(array, this.fromIndex + fromIndex, this.fromIndex + toIndex, value);
    }

    @Override
    public Float set(int index, Float element) {
        float oldValue = getFloat(index);
        setFloat(index, element);
        return oldValue;
    }
}
