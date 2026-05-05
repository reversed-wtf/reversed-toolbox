package wtf.reversed.toolbox.collect;

import wtf.reversed.toolbox.util.*;

import java.lang.invoke.*;
import java.nio.*;

/**
 * Sealed root of the slice hierarchy. Each subtype views a contiguous range of
 * a backing {@code byte[]} as a typed primitive sequence (bytes, shorts, ints,
 * longs, floats, doubles).
 *
 * <p>Slices are little-endian. The backing store is shared between a slice and
 * any further {@link Bytes#slice(int, int) slice()} of it; only typed-array
 * factories such as {@code Ints.wrap(int[])} make a defensive copy, since the
 * backing must be {@code byte[]}. {@code Bytes.wrap(byte[])} is the only
 * zero-copy view factory.
 *
 * <p>Mutability is a type-level distinction, not a memory-level one: a
 * {@code Mutable} subtype aliases the same {@code byte[]} as its non-mutable
 * parent, so handing out a non-mutable view does not prevent another reference
 * from changing the underlying memory.
 */
public sealed abstract class Slice
    permits Bytes, Shorts, Ints, Longs, Floats, Doubles {

    static final byte[] EMPTY_ARRAY = new byte[0];

    static final VarHandle VH_SHORT = byteArrayVarHandle(short[].class, ByteOrder.LITTLE_ENDIAN);
    static final VarHandle VH_INT = byteArrayVarHandle(int[].class, ByteOrder.LITTLE_ENDIAN);
    static final VarHandle VH_LONG = byteArrayVarHandle(long[].class, ByteOrder.LITTLE_ENDIAN);
    static final VarHandle VH_FLOAT = byteArrayVarHandle(float[].class, ByteOrder.LITTLE_ENDIAN);
    static final VarHandle VH_DOUBLE = byteArrayVarHandle(double[].class, ByteOrder.LITTLE_ENDIAN);

    static final VarHandle VH_SHORT_BE = byteArrayVarHandle(short[].class, ByteOrder.BIG_ENDIAN);
    static final VarHandle VH_INT_BE = byteArrayVarHandle(int[].class, ByteOrder.BIG_ENDIAN);
    static final VarHandle VH_LONG_BE = byteArrayVarHandle(long[].class, ByteOrder.BIG_ENDIAN);
    static final VarHandle VH_FLOAT_BE = byteArrayVarHandle(float[].class, ByteOrder.BIG_ENDIAN);
    static final VarHandle VH_DOUBLE_BE = byteArrayVarHandle(double[].class, ByteOrder.BIG_ENDIAN);

    private static VarHandle byteArrayVarHandle(Class<?> viewType, ByteOrder order) {
        return MethodHandles
            .byteArrayViewVarHandle(viewType, order)
            .withInvokeExactBehavior();
    }

    final byte[] array;
    final int offset;
    final int length;

    Slice(byte[] array, int offset, int length) {
        Check.fromIndexSize(offset, length, array.length);
        this.array = array;
        this.offset = offset;
        this.length = length;
    }

    public abstract int length();

    public abstract Buffer asBuffer();

    public Bytes asBytes() {
        return new Bytes(array, offset, length);
    }

    final ByteBuffer asByteBuffer() {
        return ByteBuffer
            .wrap(array, offset, length)
            .order(ByteOrder.LITTLE_ENDIAN);
    }

    final void copyWithinBytes(int srcIndex, int dstIndex, int length) {
        Check.fromIndexSize(srcIndex, length, this.length);
        Check.fromIndexSize(dstIndex, length, this.length);
        System.arraycopy(array, offset + srcIndex, array, offset + dstIndex, length);
    }

}
