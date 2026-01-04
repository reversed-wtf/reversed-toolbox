package wtf.reversed.toolbox.math;

import wtf.reversed.toolbox.collect.*;

import java.nio.*;

/**
 * Represents a 2x2 matrix.
 * <p>
 * This matrix is stored in column-major order. Meaning the elements are laid out in memory
 * such that the first four elements represent the first column, the next four elements
 * represent the second column, and so on.
 * <p>
 * This is the same order as OpenGL uses.
 *
 * @param m11 The element in the first row and the first column.
 * @param m21 The element in the second row and the first column.
 * @param m12 The element in the first row and the second column.
 * @param m22 The element in the second row and the second column.
 */
public record Matrix2(
    float m11, float m21,
    float m12, float m22
) implements Matrix<Matrix2>, Primitive {
    /**
     * The identity matrix for 2x2 transformations.
     */
    public static final Matrix2 IDENTITY = new Matrix2(
        1.0f, 0.0f,
        0.0f, 1.0f
    );


    /**
     * Creates a new matrix representing a rotation transformation.
     *
     * @param rotation The quaternion representing the rotation.
     * @return A new matrix representing a rotation transformation.
     */
    public static Matrix2 fromRotation(Quaternion rotation) {
        float x = rotation.x();
        float y = rotation.y();
        float z = rotation.z();
        float w = rotation.w();

        float x2 = x + x;
        float y2 = y + y;
        float z2 = z + z;

        float wz = w * z2;
        float xx = x * x2;
        float xy = x * y2;
        float yy = y * y2;
        float zz = z * z2;

        return new Matrix2(
            1.0f - yy - zz, xy + wz,
            xy - wz, 1.0f - xx - zz
        );
    }

    /**
     * Creates a new matrix representing a rotation transformation.
     *
     * @param scale The scale vector.
     * @return A new matrix representing a rotation transformation.
     */
    public static Matrix2 fromScale(Vector2 scale) {
        return fromScale(scale.x(), scale.y());
    }

    /**
     * Creates a matrix representing a scale transformation.
     *
     * @param sx The scale factor along the x-axis.
     * @param sy The scale factor along the y-axis.
     * @return A new matrix representing a scale transformation.
     */
    public static Matrix2 fromScale(float sx, float sy) {
        return new Matrix2(
            sx, 0f,
            0f, sy
        );
    }


    @Override
    public Matrix2 add(Matrix2 other) {
        return new Matrix2(
            m11 + other.m11, m21 + other.m21,
            m12 + other.m12, m22 + other.m22
        );
    }

    @Override
    public Matrix2 multiply(float scalar) {
        return new Matrix2(
            m11 * scalar, m21 * scalar,
            m12 * scalar, m22 * scalar
        );
    }


    @Override
    public Matrix2 one() {
        return IDENTITY;
    }

    @Override
    public Matrix2 multiply(Matrix2 other) {
        return new Matrix2(
            Math.fma(m11, other.m11, m12 * other.m21),
            Math.fma(m21, other.m11, m22 * other.m21),
            Math.fma(m11, other.m12, m12 * other.m22),
            Math.fma(m21, other.m12, m22 * other.m22)
        );
    }

    @Override
    public Matrix2 inverse() {
        float det = determinant();

        // TODO: Fix epsilons
        if (Math.abs(det) < 1e-6f) {
            throw new ArithmeticException("Cannot invert matrix with near-zero determinant");
        }

        return new Matrix2(
            +m22, -m21,
            -m12, +m11
        ).divide(det);
    }


    @Override
    public Matrix2 transpose() {
        return new Matrix2(
            m11, m12,
            m21, m22
        );
    }

    @Override
    public float determinant() {
        return m11 * m22 - m21 * m12;
    }


    @Override
    public float get(int row, int column) {
        return switch (row) {
            case 0 -> switch (column) {
                case 0 -> m11;
                case 1 -> m12;
                default -> throw new IndexOutOfBoundsException();
            };
            case 1 -> switch (column) {
                case 0 -> m21;
                case 1 -> m22;
                default -> throw new IndexOutOfBoundsException();
            };
            default -> throw new IndexOutOfBoundsException();
        };
    }

    @Override
    public int componentCount() {
        return 4;
    }

    @Override
    public void toSliceUnsafe(Floats.Mutable floats, int offset) {
        floats.set(offset/**/, m11);
        floats.set(offset + 1, m21);
        floats.set(offset + 2, m12);
        floats.set(offset + 3, m22);
    }

    @Override
    public void toBufferUnsafe(FloatBuffer floats) {
        floats.put(m11);
        floats.put(m21);
        floats.put(m12);
        floats.put(m22);
    }


    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof Matrix2 other
            && FloatMath.equals(m11, other.m11)
            && FloatMath.equals(m21, other.m21)
            && FloatMath.equals(m12, other.m12)
            && FloatMath.equals(m22, other.m22);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + FloatMath.hashCode(m11);
        result = 31 * result + FloatMath.hashCode(m21);
        result = 31 * result + FloatMath.hashCode(m12);
        result = 31 * result + FloatMath.hashCode(m22);
        return result;
    }

    @Override
    public String toString() {
        return "" +
            "[[" + m11 + ", " + m12 + "]\n" +
            " [" + m21 + ", " + m22 + "]]";
    }
}
