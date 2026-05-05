package wtf.reversed.toolbox.util;

import java.nio.*;

enum SliceType {
    Bytes(byte.class, Byte.class, ByteBuffer.class, 1),
    Shorts(short.class, Short.class, ShortBuffer.class, 2),
    Ints(int.class, Integer.class, IntBuffer.class, 4),
    Longs(long.class, Long.class, LongBuffer.class, 8),
    Floats(float.class, Float.class, FloatBuffer.class, 4),
    Doubles(double.class, Double.class, DoubleBuffer.class, 8),
    ;

    private final Class<?> primitiveType;
    private final Class<?> boxedType;
    private final Class<?> bufferType;
    private final int primitiveSize;

    SliceType(Class<?> primitiveType, Class<?> boxedType, Class<?> bufferType, int primitiveSize) {
        this.primitiveType = primitiveType;
        this.boxedType = boxedType;
        this.bufferType = bufferType;
        this.primitiveSize = primitiveSize;
    }

    String typeName() {
        return name();
    }

    Class<?> primitiveType() {
        return primitiveType;
    }

    Class<?> boxedType() {
        return boxedType;
    }

    Class<?> bufferType() {
        return bufferType;
    }

    int primitiveSize() {
        return primitiveSize;
    }

    boolean isByte() {
        return this == Bytes;
    }

    String capitalizedPrimitiveName() {
        var s = primitiveType.getSimpleName();
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    String varHandleName(ByteOrder order) {
        return "VH_" + primitiveType.getSimpleName().toUpperCase()
            + (order == ByteOrder.LITTLE_ENDIAN ? "" : "_BE");
    }

    public boolean isIntegral() {
        return this != Floats && this != Doubles;
    }
}
