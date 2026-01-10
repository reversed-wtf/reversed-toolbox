package wtf.reversed.toolbox.math;

import wtf.reversed.toolbox.collect.*;
import wtf.reversed.toolbox.io.*;

import java.io.*;
import java.nio.*;

/**
 * A four-dimensional vector.
 *
 * @param x The x component.
 * @param y The y component.
 * @param z The z component.
 * @param w The w component.
 */
public record Vector4(
    float x,
    float y,
    float z,
    float w
) implements Vector<Vector4>, Primitive {
    /**
     * The number of bytes required to store a four-dimensional vector.
     */
    public static final int BYTES = Float.BYTES * 4;

    /**
     * A vector with all components set to zero.
     */
    public static final Vector4 ZERO = new Vector4(0.0f, 0.0f, 0.0f, 0.0f);

    /**
     * A vector with all components set to one.
     */
    public static final Vector4 ONE = new Vector4(1.0f, 1.0f, 1.0f, 1.0f);

    /**
     * A vector with the x component set to one and the other components set to zero.
     */
    public static final Vector4 X = new Vector4(1.0f, 0.0f, 0.0f, 0.0f);

    /**
     * A vector with the y component set to one and the other components set to zero.
     */
    public static final Vector4 Y = new Vector4(0.0f, 1.0f, 0.0f, 0.0f);

    /**
     * A vector with the z component set to one and the other components set to zero.
     */
    public static final Vector4 Z = new Vector4(0.0f, 0.0f, 1.0f, 0.0f);

    /**
     * A vector with the w component set to one and the other components set to zero.
     */
    public static final Vector4 W = new Vector4(0.0f, 0.0f, 0.0f, 1.0f);


    /**
     * Creates a new vector with all components set to the same value.
     *
     * @param v The value for all components.
     */
    public Vector4(float v) {
        this(v, v, v, v);
    }

    /**
     * Creates a new vector from an existing {@link Vector2} and a z and w component.
     *
     * @param v The vector.
     * @param z The z component.
     * @param w The w component.
     */
    public Vector4(Vector2 v, float z, float w) {
        this(v.x(), v.y(), z, w);
    }

    /**
     * Creates a new vector from an existing {@link Vector3} and a w component.
     *
     * @param v The vector.
     * @param w The w component.
     */
    public Vector4(Vector3 v, float w) {
        this(v.x(), v.y(), v.z(), w);
    }

    /**
     * Creates a new vector from a binary source.
     *
     * @param source The binary source.
     * @return The vector.
     * @throws IOException If an I/O error occurs.
     */
    public static Vector4 read(BinarySource source) throws IOException {
        float x = source.readFloat();
        float y = source.readFloat();
        float z = source.readFloat();
        float w = source.readFloat();
        return new Vector4(x, y, z, w);
    }


    @Override
    public Vector4 add(Vector4 other) {
        return new Vector4(x + other.x, y + other.y, z + other.z, w + other.w);
    }

    @Override
    public Vector4 multiply(float scalar) {
        return new Vector4(x * scalar, y * scalar, z * scalar, w * scalar);
    }


    @Override
    public float get(int index) {
        return switch (index) {
            case 0 -> x;
            case 1 -> y;
            case 2 -> z;
            case 3 -> w;
            default -> throw new IndexOutOfBoundsException();
        };
    }

    @Override
    public float dot(Vector4 other) {
        return x * other.x + y * other.y + z * other.z + w * other.w;
    }


    @Override
    public int componentCount() {
        return 4;
    }

    @Override
    public void toSliceUnsafe(Floats.Mutable floats, int offset) {
        floats.set(offset/**/, x);
        floats.set(offset + 1, y);
        floats.set(offset + 2, z);
        floats.set(offset + 3, w);
    }

    @Override
    public void toBufferUnsafe(FloatBuffer floats) {
        floats.put(x);
        floats.put(y);
        floats.put(z);
        floats.put(w);
    }


    /**
     * Transforms this vector by the given matrix.
     *
     * @param matrix The matrix to transform by.
     * @return The transformed vector.
     */
    public Vector4 transform(Matrix4 matrix) {
        return new Vector4(
            Math.fma(x, matrix.m11(), Math.fma(y, matrix.m12(), Math.fma(z, matrix.m13(), w * matrix.m14()))),
            Math.fma(x, matrix.m21(), Math.fma(y, matrix.m22(), Math.fma(z, matrix.m23(), w * matrix.m24()))),
            Math.fma(x, matrix.m31(), Math.fma(y, matrix.m32(), Math.fma(z, matrix.m33(), w * matrix.m34()))),
            Math.fma(x, matrix.m41(), Math.fma(y, matrix.m42(), Math.fma(z, matrix.m43(), w * matrix.m44())))
        );
    }

    /**
     * Returns the xy components of this vector.
     *
     * @return xy components of this vector
     */
    public Vector2 xy() {
        return new Vector2(x, y);
    }

    /**
     * Returns the xyz components of this vector.
     *
     * @return xyz components of this vector
     */
    public Vector3 xyz() {
        return new Vector3(x, y, z);
    }


    @Override
    public boolean equals(Object obj) {
        return obj instanceof Vector4 other
            && FloatMath.equals(x, other.x)
            && FloatMath.equals(y, other.y)
            && FloatMath.equals(z, other.z)
            && FloatMath.equals(w, other.w);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + FloatMath.hashCode(x);
        result = 31 * result + FloatMath.hashCode(y);
        result = 31 * result + FloatMath.hashCode(z);
        result = 31 * result + FloatMath.hashCode(w);
        return result;
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + ", " + z + ", " + w + "]";
    }

}
