package wtf.reversed.toolbox.math;

import wtf.reversed.toolbox.collect.*;

import java.nio.*;

/**
 * Represents a 3x3 matrix.
 * <p>
 * This matrix is stored in column-major order. Meaning the elements are laid out in memory
 * such that the first four elements represent the first column, the next four elements
 * represent the second column, and so on.
 * <p>
 * This is the same order as OpenGL uses.
 *
 * @param m11 The element in the first row and the first column.
 * @param m21 The element in the second row and the first column.
 * @param m31 The element in the third row and the first column.
 * @param m12 The element in the first row and the second column.
 * @param m22 The element in the second row and the second column.
 * @param m32 The element in the third row and the second column.
 * @param m13 The element in the first row and the third column.
 * @param m23 The element in the second row and the third column.
 * @param m33 The element in the third row and the third column.
 */
public record Matrix3(
    float m11, float m21, float m31,
    float m12, float m22, float m32,
    float m13, float m23, float m33
) implements Matrix<Matrix3>, Primitive {
    /**
     * The identity matrix for 3x3 transformations.
     */
    public static final Matrix3 IDENTITY = new Matrix3(
        1.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 1.0f
    );


    /**
     * Creates a new matrix representing a rotation transformation.
     *
     * @param rotation The quaternion representing the rotation.
     * @return A new matrix representing a rotation transformation.
     */
    public static Matrix3 fromRotation(Quaternion rotation) {
        float x = rotation.x();
        float y = rotation.y();
        float z = rotation.z();
        float w = rotation.w();

        float x2 = x + x;
        float y2 = y + y;
        float z2 = z + z;

        float wx = w * x2;
        float wy = w * y2;
        float wz = w * z2;
        float xx = x * x2;
        float xy = x * y2;
        float xz = x * z2;
        float yy = y * y2;
        float yz = y * z2;
        float zz = z * z2;

        return new Matrix3(
            1.0f - yy - zz, xy + wz, xz - wy,
            xy - wz, 1.0f - xx - zz, yz + wx,
            xz + wy, yz - wx, 1.0f - xx - yy
        );
    }

    /**
     * Creates a new matrix representing a rotation transformation.
     *
     * @param scale The scale vector.
     * @return A new matrix representing a rotation transformation.
     */
    public static Matrix3 fromScale(Vector3 scale) {
        return fromScale(scale.x(), scale.y(), scale.z());
    }

    /**
     * Creates a matrix representing a scale transformation.
     *
     * @param sx The scale factor along the x-axis.
     * @param sy The scale factor along the y-axis.
     * @param sz The scale factor along the z-axis.
     * @return A new matrix representing a scale transformation.
     */
    public static Matrix3 fromScale(float sx, float sy, float sz) {
        return new Matrix3(
            sx, 0f, 0f,
            0f, sy, 0f,
            0f, 0f, sz
        );
    }


    @Override
    public Matrix3 add(Matrix3 other) {
        return new Matrix3(
            m11 + other.m11, m21 + other.m21, m31 + other.m31,
            m12 + other.m12, m22 + other.m22, m32 + other.m32,
            m13 + other.m13, m23 + other.m23, m33 + other.m33
        );
    }

    @Override
    public Matrix3 multiply(float scalar) {
        return new Matrix3(
            m11 * scalar, m21 * scalar, m31 * scalar,
            m12 * scalar, m22 * scalar, m32 * scalar,
            m13 * scalar, m23 * scalar, m33 * scalar
        );
    }


    @Override
    public Matrix3 multiply(Matrix3 other) {
        return new Matrix3(
            Math.fma(m11, other.m11, Math.fma(m12, other.m21, m13 * other.m31)),
            Math.fma(m21, other.m11, Math.fma(m22, other.m21, m23 * other.m31)),
            Math.fma(m31, other.m11, Math.fma(m32, other.m21, m33 * other.m31)),
            Math.fma(m11, other.m12, Math.fma(m12, other.m22, m13 * other.m32)),
            Math.fma(m21, other.m12, Math.fma(m22, other.m22, m23 * other.m32)),
            Math.fma(m31, other.m12, Math.fma(m32, other.m22, m33 * other.m32)),
            Math.fma(m11, other.m13, Math.fma(m12, other.m23, m13 * other.m33)),
            Math.fma(m21, other.m13, Math.fma(m22, other.m23, m23 * other.m33)),
            Math.fma(m31, other.m13, Math.fma(m32, other.m23, m33 * other.m33))
        );
    }


    @Override
    public Matrix3 one() {
        return IDENTITY;
    }

    @Override
    public Matrix3 inverse() {
        float a11 = +(m22 * m33 - m32 * m23);
        float a12 = -(m12 * m33 - m32 * m13);
        float a13 = +(m12 * m23 - m22 * m13);

        float det = m11 * a11 + m21 * a12 + m31 * a13;

        // TODO: Fix epsilons
        if (Math.abs(det) < 1e-6f) {
            throw new ArithmeticException("Cannot invert matrix with near-zero determinant");
        }

        float a21 = -(m21 * m33 - m31 * m23);
        float a22 = +(m11 * m33 - m31 * m13);
        float a23 = -(m11 * m23 - m21 * m13);

        float a31 = +(m21 * m32 - m31 * m22);
        float a32 = -(m11 * m32 - m31 * m12);
        float a33 = +(m11 * m22 - m21 * m12);

        return new Matrix3(
            a11, a21, a31,
            a12, a22, a32,
            a13, a23, a33
        ).divide(det);
    }

    @Override
    public Matrix3 transpose() {
        return new Matrix3(
            m11, m12, m13,
            m21, m22, m23,
            m31, m32, m33
        );
    }


    @Override
    public float determinant() {
        float a11 = +(m22 * m33 - m32 * m23);
        float a12 = -(m12 * m33 - m32 * m13);
        float a13 = +(m12 * m23 - m22 * m13);

        return m11 * a11 + m21 * a12 + m31 * a13;
    }


    @Override
    public float get(int row, int column) {
        return switch (row) {
            case 0 -> switch (column) {
                case 0 -> m11;
                case 1 -> m12;
                case 2 -> m13;
                default -> throw new IndexOutOfBoundsException();
            };
            case 1 -> switch (column) {
                case 0 -> m21;
                case 1 -> m22;
                case 2 -> m23;
                default -> throw new IndexOutOfBoundsException();
            };
            case 2 -> switch (column) {
                case 0 -> m31;
                case 1 -> m32;
                case 2 -> m33;
                default -> throw new IndexOutOfBoundsException();
            };
            default -> throw new IndexOutOfBoundsException();
        };
    }

    @Override
    public int componentCount() {
        return 9;
    }

    @Override
    public void toSliceUnsafe(Floats.Mutable floats, int offset) {
        floats.set(offset/**/, m11);
        floats.set(offset + 1, m21);
        floats.set(offset + 2, m31);
        floats.set(offset + 3, m12);
        floats.set(offset + 4, m22);
        floats.set(offset + 5, m32);
        floats.set(offset + 6, m13);
        floats.set(offset + 7, m23);
        floats.set(offset + 8, m33);
    }

    @Override
    public void toBufferUnsafe(FloatBuffer floats) {
        floats.put(m11);
        floats.put(m21);
        floats.put(m31);
        floats.put(m12);
        floats.put(m22);
        floats.put(m32);
        floats.put(m13);
        floats.put(m23);
        floats.put(m33);
    }


    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof Matrix3 other
            && FloatMath.equals(m11, other.m11)
            && FloatMath.equals(m21, other.m21)
            && FloatMath.equals(m31, other.m31)
            && FloatMath.equals(m12, other.m12)
            && FloatMath.equals(m22, other.m22)
            && FloatMath.equals(m32, other.m32)
            && FloatMath.equals(m13, other.m13)
            && FloatMath.equals(m23, other.m23)
            && FloatMath.equals(m33, other.m33);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + FloatMath.hashCode(m11);
        result = 31 * result + FloatMath.hashCode(m21);
        result = 31 * result + FloatMath.hashCode(m31);
        result = 31 * result + FloatMath.hashCode(m12);
        result = 31 * result + FloatMath.hashCode(m22);
        result = 31 * result + FloatMath.hashCode(m32);
        result = 31 * result + FloatMath.hashCode(m13);
        result = 31 * result + FloatMath.hashCode(m23);
        result = 31 * result + FloatMath.hashCode(m33);
        return result;
    }

    @Override
    public String toString() {
        return "" +
            "[[" + m11 + ", " + m12 + ", " + m13 + "]\n" +
            " [" + m21 + ", " + m22 + ", " + m23 + "]\n" +
            " [" + m31 + ", " + m32 + ", " + m33 + "]]";
    }
}
