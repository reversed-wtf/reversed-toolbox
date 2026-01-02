package wtf.reversed.toolbox.math;

import wtf.reversed.toolbox.collect.*;
import wtf.reversed.toolbox.io.*;

import java.io.*;
import java.nio.*;

/**
 * An axis aligned bounding box.
 *
 * @param minX The minimum x coordinate.
 * @param minY The minimum y coordinate.
 * @param minZ The minimum z coordinate.
 * @param maxX The maximum x coordinate.
 * @param maxY The maximum y coordinate.
 * @param maxZ The maximum z coordinate.
 */
public record Bounds(
    float minX, float minY, float minZ,
    float maxX, float maxY, float maxZ
) implements Primitive {
    public static final Bounds EMPTY = new Bounds(
        0.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 0.0f
    );

    /**
     * Reads bounds from a binary source.
     *
     * @param source The source to read from
     * @return The read bounds
     * @throws IOException If an I/O error occurs
     */
    public static Bounds read(BinarySource source) throws IOException {
        float minX = source.readFloat();
        float minY = source.readFloat();
        float minZ = source.readFloat();
        float maxX = source.readFloat();
        float maxY = source.readFloat();
        float maxZ = source.readFloat();

        return new Bounds(
            minX, minY, minZ,
            maxX, maxY, maxZ
        );
    }

    /**
     * Compute bounds from a list of vertices, returning an empty bounds if the list is empty
     *
     * @param vertices The vertices to calculate bounds from
     * @return The calculated bounds
     */
    public static Bounds calculate(Floats vertices) {
        if (vertices.length() % 3 != 0) {
            throw new IllegalArgumentException("Vertices must be a multiple of 3");
        }
        if (vertices.length() == 0) {
            return EMPTY;
        }

        float minX = vertices.get(0);
        float maxX = vertices.get(0);
        float minY = vertices.get(1);
        float maxY = vertices.get(1);
        float minZ = vertices.get(2);
        float maxZ = vertices.get(2);
        for (int i = 3; i < vertices.length(); i += 3) {
            float x = vertices.get(i/**/);
            float y = vertices.get(i + 1);
            float z = vertices.get(i + 2);
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            minZ = Math.min(minZ, z);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
            maxZ = Math.max(maxZ, z);
        }
        return new Bounds(
            minX, minY, minZ,
            maxX, maxY, maxZ
        );
    }


    /**
     * Returns the minimum corner of the bounds.
     *
     * @return The minimum corner.
     */
    public Vector3 min() {
        return new Vector3(minX, minY, minZ);
    }

    /**
     * Returns the maximum corner of the bounds.
     *
     * @return The maximum corner.
     */
    public Vector3 max() {
        return new Vector3(maxX, maxY, maxZ);
    }

    /**
     * Returns the center of the bounds.
     *
     * @return The center.
     */
    public Vector3 center() {
        return min().add(max()).multiply(0.5f);
    }


    @Override
    public int componentCount() {
        return 6;
    }

    @Override
    public void toSliceUnsafe(Floats.Mutable floats, int offset) {
        floats.set(offset/**/, minX);
        floats.set(offset + 1, minY);
        floats.set(offset + 2, minZ);
        floats.set(offset + 3, maxX);
        floats.set(offset + 4, maxY);
        floats.set(offset + 5, maxZ);
    }

    @Override
    public void toBufferUnsafe(FloatBuffer floats) {
        floats.put(minX);
        floats.put(minY);
        floats.put(minZ);
        floats.put(maxX);
        floats.put(maxY);
        floats.put(maxZ);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Bounds other
            && FloatMath.equals(minX, other.minX)
            && FloatMath.equals(minY, other.minY)
            && FloatMath.equals(minZ, other.minZ)
            && FloatMath.equals(maxX, other.maxX)
            && FloatMath.equals(maxY, other.maxY)
            && FloatMath.equals(maxZ, other.maxZ);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + Float.hashCode(minX);
        result = 31 * result + Float.hashCode(minY);
        result = 31 * result + Float.hashCode(minZ);
        result = 31 * result + Float.hashCode(maxX);
        result = 31 * result + Float.hashCode(maxY);
        result = 31 * result + Float.hashCode(maxZ);
        return result;
    }

    @Override
    public String toString() {
        return "" +
            "[[" + minX + ", " + minY + ", " + minZ + "]\n" +
            " [" + maxX + ", " + maxY + ", " + maxZ + "]]";
    }
}
