package wtf.reversed.toolbox.collect;

import java.lang.invoke.*;
import java.nio.*;

public sealed abstract class Slice permits Bytes, Shorts, Ints, Longs, Floats, Doubles {
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

    public abstract int length();

    public abstract Buffer asBuffer();
}
