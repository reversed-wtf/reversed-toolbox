package wtf.reversed.toolbox.collect;

import wtf.reversed.toolbox.util.*;

import java.nio.*;
import java.util.Arrays;

public final class MutableDoubles extends Doubles {
    private MutableDoubles(double[] array, int fromIndex, int toIndex) {
        super(array, fromIndex, toIndex);
    }

    public static MutableDoubles wrap(double[] array) {
        return new MutableDoubles(array, 0, array.length);
    }

    public static MutableDoubles wrap(double[] array, int fromIndex, int toIndex) {
        return new MutableDoubles(array, fromIndex, toIndex);
    }

    public static MutableDoubles allocate(int size) {
        return new MutableDoubles(new double[size], 0, size);
    }

    public void setDouble(int index, double value) {
        Check.index(index, size());
        array[fromIndex + index] = value;
    }

    public DoubleBuffer asMutableBuffer() {
        return DoubleBuffer.wrap(array, fromIndex, size());
    }

    public void fill(double value) {
        Arrays.fill(array, fromIndex, toIndex, value);
    }

    public MutableDoubles slice(int fromIndex) {
        return slice(fromIndex, size());
    }

    public MutableDoubles slice(int fromIndex, int toIndex) {
        Check.fromToIndex(fromIndex, toIndex, size());
        return new MutableDoubles(array, this.fromIndex + fromIndex, this.fromIndex + toIndex);
    }

    @Override
    public Double set(int index, Double element) {
        double oldValue = getDouble(index);
        setDouble(index, element);
        return oldValue;
    }
}
