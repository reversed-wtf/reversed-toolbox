package wtf.reversed.toolbox.math;

import wtf.reversed.toolbox.collect.*;
import wtf.reversed.toolbox.io.*;

import java.io.*;
import java.nio.*;

/**
 * A three-dimensional vector.
 *
 * @param x The x component.
 * @param y The y component.
 * @param z The z component.
 */
public record Vector3(
    float x,
    float y,
    float z
) implements Vector<Vector3>, Primitive {
    /**
     * The number of bytes required to store a three-dimensional vector.
     */
    public static final int BYTES = Float.BYTES * 3;

    /**
     * A vector with all components set to zero.
     */
    public static final Vector3 ZERO = new Vector3(0.0f, 0.0f, 0.0f);

    /**
     * A vector with all components set to one.
     */
    public static final Vector3 ONE = new Vector3(1.0f, 1.0f, 1.0f);

    /**
     * A vector with the x component set to one and the other components set to zero.
     */
    public static final Vector3 X = new Vector3(1.0f, 0.0f, 0.0f);

    /**
     * A vector with the y component set to one and the other components set to zero.
     */
    public static final Vector3 Y = new Vector3(0.0f, 1.0f, 0.0f);

    /**
     * A vector with the z component set to one and the other components set to zero.
     */
    public static final Vector3 Z = new Vector3(0.0f, 0.0f, 1.0f);


    /**
     * Creates a new vector with all components set to the same value.
     *
     * @param v The value for all components.
     */
    public Vector3(float v) {
        this(v, v, v);
    }

    /**
     * Creates a new vector from an existing {@link Vector2} and a z component.
     *
     * @param v The vector.
     * @param z The z component.
     */
    public Vector3(Vector2 v, float z) {
        this(v.x(), v.y(), z);
    }

    /**
     * Creates a new vector from a binary source.
     *
     * @param source The binary source.
     * @return The vector.
     * @throws IOException If an I/O error occurs.
     */
    public static Vector3 read(BinarySource source) throws IOException {
        float x = source.readFloat();
        float y = source.readFloat();
        float z = source.readFloat();
        return new Vector3(x, y, z);
    }


    @Override
    public Vector3 add(Vector3 other) {
        return new Vector3(x + other.x, y + other.y, z + other.z);
    }

    @Override
    public Vector3 multiply(float scalar) {
        return new Vector3(x * scalar, y * scalar, z * scalar);
    }


    @Override
    public float get(int index) {
        return switch (index) {
            case 0 -> x;
            case 1 -> y;
            case 2 -> z;
            default -> throw new IndexOutOfBoundsException();
        };
    }

    @Override
    public float dot(Vector3 other) {
        return x * other.x + y * other.y + z * other.z;
    }


    @Override
    public int componentCount() {
        return 3;
    }

    @Override
    public void toSliceUnsafe(Floats.Mutable floats, int offset) {
        floats.set(offset/**/, x);
        floats.set(offset + 1, y);
        floats.set(offset + 2, z);
    }

    @Override
    public void toBufferUnsafe(FloatBuffer floats) {
        floats.put(x);
        floats.put(y);
        floats.put(z);
    }


    /**
     * Calculates the cross product of this vector and another.
     *
     * @param other The other vector.
     * @return The cross product.
     */
    public Vector3 cross(Vector3 other) {
        return new Vector3(
            Math.fma(y, other.z(), -z * other.y()),
            Math.fma(z, other.x(), -x * other.z()),
            Math.fma(x, other.y(), -y * other.x())
        );
    }

    /**
     * Calculates the distance between this vector and another.
     *
     * @param other The other vector.
     * @return The distance.
     */
    public float distance(Vector3 other) {
        float dx = x - other.x;
        float dy = y - other.y;
        float dz = z - other.z;
        return FloatMath.sqrt(Math.fma(dx, dx, Math.fma(dy, dy, dz * dz)));
    }

    /**
     * Performs a fused multiply-add operation on this vector.
     *
     * @param mul The multiplier.
     * @param add The addend vector.
     * @return The result of the fused multiply-add operation.
     */
    public Vector3 fma(float mul, Vector3 add) {
        return new Vector3(
            Math.fma(x, mul, add.x),
            Math.fma(y, mul, add.y),
            Math.fma(z, mul, add.z)
        );
    }

    /**
     * Transforms this vector by the given matrix.
     *
     * @param matrix The matrix to transform by.
     * @return The transformed vector.
     */
    public Vector3 transform(Matrix3 matrix) {
        return new Vector3(
            Math.fma(x, matrix.m11(), Math.fma(y, matrix.m12(), z * matrix.m13())),
            Math.fma(x, matrix.m21(), Math.fma(y, matrix.m22(), z * matrix.m23())),
            Math.fma(x, matrix.m31(), Math.fma(y, matrix.m32(), z * matrix.m33()))
        );
    }

    /**
     * Transforms this vector by the given matrix.
     *
     * @param matrix The matrix to transform by.
     * @return The transformed vector.
     */
    public Vector3 transform(Matrix4 matrix) {
        return new Vector3(
            Math.fma(x, matrix.m11(), Math.fma(y, matrix.m12(), Math.fma(z, matrix.m13(), matrix.m14()))),
            Math.fma(x, matrix.m21(), Math.fma(y, matrix.m22(), Math.fma(z, matrix.m23(), matrix.m24()))),
            Math.fma(x, matrix.m31(), Math.fma(y, matrix.m32(), Math.fma(z, matrix.m33(), matrix.m34())))
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


    @Override
    public boolean equals(Object obj) {
        return obj instanceof Vector3 other
            && FloatMath.equals(x, other.x)
            && FloatMath.equals(y, other.y)
            && FloatMath.equals(z, other.z);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + FloatMath.hashCode(x);
        result = 31 * result + FloatMath.hashCode(y);
        result = 31 * result + FloatMath.hashCode(z);
        return result;
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + ", " + z + "]";
    }
}
