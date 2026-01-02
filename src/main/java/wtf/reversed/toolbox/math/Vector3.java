package wtf.reversed.toolbox.math;

import wtf.reversed.toolbox.collect.*;
import wtf.reversed.toolbox.io.*;
import wtf.reversed.toolbox.util.*;

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
     * Creates a new vector from an existing {@link Vector2} and a z and w component.
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
    public float get(int index) {
        return switch (index) {
            case 0 -> x;
            case 1 -> y;
            case 2 -> z;
            default -> throw new IndexOutOfBoundsException();
        };
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
    public float dot(Vector3 other) {
        return x * other.x + y * other.y + z * other.z;
    }


    @Override
    public void copyTo(Floats.Mutable floats, int offset) {
        Check.fromIndexSize(offset, 4, floats.length());
        floats.set(offset/**/, x);
        floats.set(offset + 1, y);
        floats.set(offset + 2, z);
    }

    @Override
    public void copyTo(FloatBuffer floats) {
        floats.put(x);
        floats.put(y);
        floats.put(z);
    }


    /**
     * Converts this vector to a {@link Vector2} by dropping the z and w components.
     *
     * @return The new vector.
     */
    public Vector2 toVector2() {
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
        result = 31 * result + Float.hashCode(x);
        result = 31 * result + Float.hashCode(y);
        result = 31 * result + Float.hashCode(z);
        return result;
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + ", " + z + "]";
    }
}
