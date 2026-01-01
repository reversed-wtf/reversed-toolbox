package wtf.reversed.toolbox.math;

import wtf.reversed.toolbox.collect.*;
import wtf.reversed.toolbox.io.*;
import wtf.reversed.toolbox.util.*;

import java.io.*;
import java.nio.*;

/**
 * A two-dimensional vector.
 *
 * @param x The x component.
 * @param y The y component.
 */
public record Vector2(
    float x,
    float y
) implements InnerProductSpace<Vector2>, Primitive {
    /**
     * A vector with all components set to zero.
     */
    public static final Vector2 ZERO = new Vector2(0.0f, 0.0f);

    /**
     * A vector with all components set to one.
     */
    public static final Vector2 ONE = new Vector2(1.0f, 1.0f);

    /**
     * A vector with the x component set to one and the other components set to zero.
     */
    public static final Vector2 X = new Vector2(1.0f, 0.0f);

    /**
     * A vector with the y component set to one and the other components set to zero.
     */
    public static final Vector2 Y = new Vector2(0.0f, 1.0f);


    /**
     * Creates a new vector with all components set to the same value.
     *
     * @param v The value for all components.
     */
    public Vector2(float v) {
        this(v, v);
    }

    /**
     * Creates a new vector from a binary source.
     *
     * @param source The binary source.
     * @return The vector.
     * @throws IOException If an I/O error occurs.
     */
    public static Vector2 read(BinarySource source) throws IOException {
        float x = source.readFloat();
        float y = source.readFloat();
        return new Vector2(x, y);
    }


    @Override
    public Vector2 add(Vector2 other) {
        return new Vector2(x + other.x, y + other.y);
    }

    @Override
    public Vector2 multiply(float scalar) {
        return new Vector2(x * scalar, y * scalar);
    }

    @Override
    public float dot(Vector2 other) {
        return x * other.x + y * other.y;
    }


    @Override
    public float get(int index) {
        return switch (index) {
            case 0 -> x;
            case 1 -> y;
            default -> throw new IndexOutOfBoundsException();
        };
    }

    @Override
    public void copyTo(Floats.Mutable floats, int offset) {
        Check.fromIndexSize(offset, 4, floats.length());
        floats.set(offset/**/, x);
        floats.set(offset + 1, y);
    }

    @Override
    public void copyTo(FloatBuffer floats) {
        floats.put(x);
        floats.put(y);
    }


    @Override
    public boolean equals(Object obj) {
        return obj instanceof Vector2 other
            && FloatMath.equals(x, other.x)
            && FloatMath.equals(y, other.y);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + Float.hashCode(x);
        result = 31 * result + Float.hashCode(y);
        return result;
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + "]";
    }
}
