package wtf.reversed.toolbox.math;

import wtf.reversed.toolbox.collect.*;
import wtf.reversed.toolbox.io.*;
import wtf.reversed.toolbox.util.*;

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
) implements InnerProductSpace<Vector4>, Primitive {
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
    public float dot(Vector4 other) {
        return x * other.x + y * other.y + z * other.z + w * other.w;
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
    public void copyTo(Floats.Mutable floats, int offset) {
        Check.fromIndexSize(offset, 4, floats.length());
        floats.set(offset/**/, x);
        floats.set(offset + 1, y);
        floats.set(offset + 2, z);
        floats.set(offset + 3, w);
    }

    @Override
    public void copyTo(FloatBuffer floats) {
        floats.put(x);
        floats.put(y);
        floats.put(z);
        floats.put(w);
    }


    /**
     * Converts this vector to a {@link Vector2} by dropping the z and w components.
     *
     * @return The new vector.
     */
    public Vector2 toVector2() {
        return new Vector2(x, y);
    }

    /**
     * Converts this vector to a {@link Vector3} by dropping the w component.
     *
     * @return The new vector.
     */
    public Vector3 toVector3() {
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
        result = 31 * result + Float.hashCode(x);
        result = 31 * result + Float.hashCode(y);
        result = 31 * result + Float.hashCode(z);
        result = 31 * result + Float.hashCode(w);
        return result;
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + ", " + z + ", " + w + "]";
    }
}
