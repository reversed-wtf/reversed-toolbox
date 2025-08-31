package wtf.reversed.toolbox.util;

import java.lang.invoke.*;
import java.nio.*;

public final class Arrays {
    private static final VarHandle asShortLE = MethodHandles.byteArrayViewVarHandle(short[].class, ByteOrder.LITTLE_ENDIAN);
    private static final VarHandle asShortBE = MethodHandles.byteArrayViewVarHandle(short[].class, ByteOrder.BIG_ENDIAN);
    private static final VarHandle asIntLE = MethodHandles.byteArrayViewVarHandle(int[].class, ByteOrder.LITTLE_ENDIAN);
    private static final VarHandle asIntBE = MethodHandles.byteArrayViewVarHandle(int[].class, ByteOrder.BIG_ENDIAN);
    private static final VarHandle asLongLE = MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.LITTLE_ENDIAN);
    private static final VarHandle asLongBE = MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.BIG_ENDIAN);
    private static final VarHandle asFloatLE = MethodHandles.byteArrayViewVarHandle(float[].class, ByteOrder.LITTLE_ENDIAN);
    private static final VarHandle asFloatBE = MethodHandles.byteArrayViewVarHandle(float[].class, ByteOrder.BIG_ENDIAN);
    private static final VarHandle asDoubleLE = MethodHandles.byteArrayViewVarHandle(double[].class, ByteOrder.LITTLE_ENDIAN);
    private static final VarHandle asDoubleBE = MethodHandles.byteArrayViewVarHandle(double[].class, ByteOrder.BIG_ENDIAN);

    private Arrays() {
    }

    public static short getShort(byte[] array, int index, ByteOrder order) {
        var handle = order == ByteOrder.LITTLE_ENDIAN ? asShortLE : asShortBE;
        return (short) handle.get(array, index);
    }

    public static int getInt(byte[] array, int index, ByteOrder order) {
        var handle = order == ByteOrder.LITTLE_ENDIAN ? asIntLE : asIntBE;
        return (int) handle.get(array, index);
    }

    public static long getLong(byte[] array, int index, ByteOrder order) {
        var handle = order == ByteOrder.LITTLE_ENDIAN ? asLongLE : asLongBE;
        return (long) handle.get(array, index);
    }

    public static float getFloat(byte[] array, int index, ByteOrder order) {
        var handle = order == ByteOrder.LITTLE_ENDIAN ? asFloatLE : asFloatBE;
        return (float) handle.get(array, index);
    }

    public static double getDouble(byte[] array, int index, ByteOrder order) {
        var handle = order == ByteOrder.LITTLE_ENDIAN ? asDoubleLE : asDoubleBE;
        return (double) handle.get(array, index);
    }
}
