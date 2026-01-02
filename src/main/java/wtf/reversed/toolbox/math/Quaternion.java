package wtf.reversed.toolbox.math;

import wtf.reversed.toolbox.collect.*;
import wtf.reversed.toolbox.io.*;
import wtf.reversed.toolbox.util.*;

import java.io.*;
import java.nio.*;

/**
 * A quaternion representing a rotation in 3D space.
 *
 * @param x The x component.
 * @param y The y component.
 * @param z The z component.
 * @param w The w component.
 */
public record Quaternion(
    float x,
    float y,
    float z,
    float w
) implements Vector<Quaternion>, Primitive {
    /**
     * The identity quaternion, representing no rotation.
     */
    public static final Quaternion IDENTITY = new Quaternion(0.0f, 0.0f, 0.0f, 1.0f);


    /**
     * Creates a new quaternion from a binary source.
     *
     * @param source The binary source.
     * @return The quaternion.
     * @throws IOException If an I/O error occurs.
     */
    public static Quaternion read(BinarySource source) throws IOException {
        float x = source.readFloat();
        float y = source.readFloat();
        float z = source.readFloat();
        float w = source.readFloat();
        return new Quaternion(x, y, z, w);
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
    public Quaternion add(Quaternion other) {
        return new Quaternion(x + other.x, y + other.y, z + other.z, w + other.w);
    }

    @Override
    public Quaternion multiply(float scalar) {
        return new Quaternion(x * scalar, y * scalar, z * scalar, w * scalar);
    }

    @Override
    public float dot(Quaternion other) {
        return x * other.x + y * other.y + z * other.z + w * other.w;
    }


    /**
     * Multiplies this quaternion with another quaternion.
     *
     * @param other The other quaternion.
     * @return The result quaternion.
     */
    public Quaternion multiply(Quaternion other) {
        return new Quaternion(
            w * other.x + x * other.w + y * other.z - z * other.y,
            w * other.y - x * other.z + y * other.w + z * other.x,
            w * other.z + x * other.y - y * other.x + z * other.w,
            w * other.w - x * other.x - y * other.y - z * other.z
        );
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


    @Override
    public boolean equals(Object obj) {
        return obj instanceof Quaternion other
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
