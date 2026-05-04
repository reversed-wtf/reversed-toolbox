package wtf.reversed.toolbox.util;

import java.nio.*;

enum SliceType {
    Bytes("Bytes", byte.class, Byte.class, ByteBuffer.class, Byte.BYTES),
    Shorts("Shorts", short.class, Short.class, ShortBuffer.class, Short.BYTES),
    Ints("Ints", int.class, Integer.class, IntBuffer.class, Integer.BYTES),
    Longs("Longs", long.class, Long.class, LongBuffer.class, Long.BYTES),
    Floats("Floats", float.class, Float.class, FloatBuffer.class, Float.BYTES),
    Doubles("Doubles", double.class, Double.class, DoubleBuffer.class, Double.BYTES),
    ;

    private final String typeName;
    private final Class<?> primitiveType;
    private final Class<?> boxedType;
    private final Class<?> bufferType;
    private final int primitiveSize;

    SliceType(
        String typeName,
        Class<?> primitiveType,
        Class<?> boxedType,
        Class<?> bufferType,
        int primitiveSize
    ) {
        this.typeName = typeName;
        this.primitiveType = primitiveType;
        this.boxedType = boxedType;
        this.bufferType = bufferType;
        this.primitiveSize = primitiveSize;
    }

    public String typeName() {
        return typeName;
    }

    public Class<?> primitiveType() {
        return primitiveType;
    }

    public Class<?> boxedType() {
        return boxedType;
    }

    public Class<?> bufferType() {
        return bufferType;
    }

    public int primitiveSize() {
        return primitiveSize;
    }

    public boolean isByte() {
        return this == Bytes;
    }

    public boolean isIntegral() {
        return this == SliceType.Bytes
            || this == SliceType.Shorts
            || this == SliceType.Ints
            || this == SliceType.Longs;
    }

    public boolean isFloating() {
        return this == SliceType.Floats
            || this == SliceType.Doubles;
    }
}
