package wtf.reversed.toolbox.collect;

import wtf.reversed.toolbox.util.*;

import java.lang.invoke.*;
import java.nio.*;

public sealed abstract class Slice
    permits Bytes, Shorts, Ints, Longs, Floats, Doubles {

    static final VarHandle VH_SHORT = MethodHandles
        .byteArrayViewVarHandle(short[].class, ByteOrder.LITTLE_ENDIAN)
        .withInvokeExactBehavior();

    static final VarHandle VH_INT = MethodHandles
        .byteArrayViewVarHandle(int[].class, ByteOrder.LITTLE_ENDIAN)
        .withInvokeExactBehavior();

    static final VarHandle VH_LONG = MethodHandles
        .byteArrayViewVarHandle(long[].class, ByteOrder.LITTLE_ENDIAN)
        .withInvokeExactBehavior();

    static final VarHandle VH_FLOAT = MethodHandles
        .byteArrayViewVarHandle(float[].class, ByteOrder.LITTLE_ENDIAN)
        .withInvokeExactBehavior();

    static final VarHandle VH_DOUBLE = MethodHandles
        .byteArrayViewVarHandle(double[].class, ByteOrder.LITTLE_ENDIAN)
        .withInvokeExactBehavior();

    final byte[] array;
    final int offset;
    final int length;

    Slice() {
        this(new byte[0], 0, 0);
    }

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
