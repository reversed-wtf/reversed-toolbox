package wtf.reversed.toolbox.collect;

import wtf.reversed.toolbox.hash.*;
import wtf.reversed.toolbox.util.*;

import java.lang.invoke.*;
import java.nio.*;
import java.util.*;


/**
 * A sealed abstract class representing a view over a segment of a byte array. Subclasses of this class provide
 * specialized access to the data for various data types such as bytes, shorts, ints, longs, floats, and doubles.
 * <p>
 * All data is stored in little-endian format. This is important when using {@link #asBytes()} or any of the other
 * conversion methods from bytes to other types.
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

    /**
     * Returns the length of this slice, calculated as the total number of bytes divided by the size of each element.
     *
     * @return The number of elements in the slice.
     */
    public abstract int length();

    /**
     * Converts the slice into a {@code Buffer} representation, allowing access to its underlying data using Java's
     * buffer API. This method can return a view of the slice or a read-only buffer based on implementation.
     *
     * @return A buffer representing the data of this slice.
     */
    public abstract Buffer asBuffer();

    /**
     * Converts the slice into a {@code Bytes} representation, allowing access to the underlying data defined by the
     * slice's array, offset, and length.
     * <p>
     * Bytes are stored in little-endian format.
     *
     * @return A new {@code Bytes} instance representing the data within this slice.
     */
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

    /**
     * Compares this instance with the specified object for equality. The equality check involves verifying if both
     * objects belong to the same "family" of classes and if their internal arrays, specified by their respective
     * offsets and lengths, are equal.
     * <p>
     * For floating-point values, the comparison is performed using direct byte-wise comparison, so values are
     * considered equal if their bit representations are identical.
     *
     * @param obj The object to compare with this instance for equality. It can be any object or null.
     * @return {@code true} if the specified object is equal to this instance; {@code false} otherwise.
     */
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

    /**
     * Computes and returns the hash code for this slice. The hash code is generated by hashing the underlying data
     * represented by the slice and converting the resulting hash to an integer.
     *
     * @return The integer hash code of this slice.
     */
    @Override
    public final int hashCode() {
        return HASH.hash(asBytes()).asInt();
    }

    /**
     * Returns a string representation of this slice. The string representation includes the length of the slice and the
     * type of the slice.
     *
     * @return A string representation of this slice.
     */
    @Override
    public final String toString() {
        String simpleName = getClass().getSimpleName().toLowerCase(Locale.ROOT);
        return "[" + length() + " " + simpleName + "]";
    }
}
