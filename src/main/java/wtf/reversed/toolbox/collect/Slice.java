package wtf.reversed.toolbox.collect;

import wtf.reversed.toolbox.hash.*;
import wtf.reversed.toolbox.util.*;

import java.lang.invoke.*;
import java.nio.*;
import java.util.*;


/**
 * Represents an immutable view of a slice of an array of bytes.
 * This is an abstract base class for working with typed slices such as bytes, shorts, integers, longs, floats,
 * and doubles. Instances of specific typed slices inherit from this base class.
 */
public sealed abstract class Slice
    permits Bytes, Shorts, Ints, Longs, Floats, Doubles {

    static final byte[] EMPTY_ARRAY = new byte[0];

    static final HashFunction HASH = HashFunction.xxHash64(0L);

    static final VarHandle VH_SHORT_LE = MethodHandles.byteArrayViewVarHandle(short[].class, ByteOrder.LITTLE_ENDIAN).withInvokeExactBehavior();
    static final VarHandle VH_INT_LE = MethodHandles.byteArrayViewVarHandle(int[].class, ByteOrder.LITTLE_ENDIAN).withInvokeExactBehavior();
    static final VarHandle VH_LONG_LE = MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.LITTLE_ENDIAN).withInvokeExactBehavior();
    static final VarHandle VH_FLOAT_LE = MethodHandles.byteArrayViewVarHandle(float[].class, ByteOrder.LITTLE_ENDIAN).withInvokeExactBehavior();
    static final VarHandle VH_DOUBLE_LE = MethodHandles.byteArrayViewVarHandle(double[].class, ByteOrder.LITTLE_ENDIAN).withInvokeExactBehavior();

    static final VarHandle VH_SHORT_BE = MethodHandles.byteArrayViewVarHandle(short[].class, ByteOrder.BIG_ENDIAN).withInvokeExactBehavior();
    static final VarHandle VH_INT_BE = MethodHandles.byteArrayViewVarHandle(int[].class, ByteOrder.BIG_ENDIAN).withInvokeExactBehavior();
    static final VarHandle VH_LONG_BE = MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.BIG_ENDIAN).withInvokeExactBehavior();
    static final VarHandle VH_FLOAT_BE = MethodHandles.byteArrayViewVarHandle(float[].class, ByteOrder.BIG_ENDIAN).withInvokeExactBehavior();
    static final VarHandle VH_DOUBLE_BE = MethodHandles.byteArrayViewVarHandle(double[].class, ByteOrder.BIG_ENDIAN).withInvokeExactBehavior();

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

    private Class<?> family(Class<?> clazz) {
        Class<?> enclosing = clazz.getEnclosingClass();
        return enclosing != null ? enclosing : clazz;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Slice that) || family(getClass()) != family(obj.getClass())) {
            return false;
        }
        return Arrays.equals(
            this.array, this.offset, this.offset + this.length,
            that.array, that.offset, that.offset + that.length
        );
    }

    @Override
    public final int hashCode() {
        return HASH.hash(asBytes()).asInt();
    }

    @Override
    public final String toString() {
        String simpleName = getClass().getSimpleName().toLowerCase(Locale.ROOT);
        return "[" + length() + " " + simpleName + "]";
    }
}
