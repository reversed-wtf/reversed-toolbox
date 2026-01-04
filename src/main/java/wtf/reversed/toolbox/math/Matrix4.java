package wtf.reversed.toolbox.math;

import wtf.reversed.toolbox.collect.*;

import java.nio.*;

/**
 * Represents a 4x4 matrix.
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
 * @param m41 The element in the fourth row and the first column.
 * @param m12 The element in the first row and the second column.
 * @param m22 The element in the second row and the second column.
 * @param m32 The element in the third row and the second column.
 * @param m42 The element in the fourth row and the second column.
 * @param m13 The element in the first row and the third column.
 * @param m23 The element in the second row and the third column.
 * @param m33 The element in the third row and the third column.
 * @param m43 The element in the fourth row and the third column.
 * @param m14 The element in the first row and the fourth column.
 * @param m24 The element in the second row and the fourth column.
 * @param m34 The element in the third row and the fourth column.
 * @param m44 The element in the fourth row and the fourth column.
 */
public record Matrix4(
    float m11, float m21, float m31, float m41,
    float m12, float m22, float m32, float m42,
    float m13, float m23, float m33, float m43,
    float m14, float m24, float m34, float m44
) implements Matrix<Matrix4>, Primitive {
    /**
     * The identity matrix for 4x4 transformations.
     */
    public static final Matrix4 IDENTITY = new Matrix4(
        1.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 0.0f, 1.0f
    );


    /**
     * Creates a new matrix representing a rotation transformation.
     *
     * @param rotation The quaternion representing the rotation.
     * @return A new matrix representing a rotation transformation.
     */
    public static Matrix4 fromRotation(Quaternion rotation) {
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

        return new Matrix4(
            1.0f - yy - zz, xy + wz, xz - wy, 0.0f,
            xy - wz, 1.0f - xx - zz, yz + wx, 0.0f,
            xz + wy, yz - wx, 1.0f - xx - yy, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f
        );
    }

    /**
     * Creates a new matrix representing a rotation transformation.
     *
     * @param scale The scale vector.
     * @return A new matrix representing a rotation transformation.
     */
    public static Matrix4 fromScale(Vector3 scale) {
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
    public static Matrix4 fromScale(float sx, float sy, float sz) {
        return new Matrix4(
            sx, 0f, 0f, 0f,
            0f, sy, 0f, 0f,
            0f, 0f, sz, 0f,
            0f, 0f, 0f, 1f
        );
    }

    /**
     * Creates a new matrix representing a translation transformation.
     *
     * @param translation The translation vector.
     * @return A new matrix representing a translation transformation.
     */
    public static Matrix4 fromTranslation(Vector3 translation) {
        return fromTranslation(translation.x(), translation.y(), translation.z());
    }

    /**
     * Creates a matrix representing a translation transformation.
     *
     * @param tx The translation along the x-axis.
     * @param ty The translation along the y-axis.
     * @param tz The translation along the z-axis.
     * @return A new matrix representing a translation transformation.
     */
    public static Matrix4 fromTranslation(float tx, float ty, float tz) {
        return new Matrix4(
            1f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, 1f, 0f,
            tx, ty, tz, 1f
        );
    }


    @Override
    public Matrix4 add(Matrix4 other) {
        return new Matrix4(
            m11 + other.m11, m21 + other.m21, m31 + other.m31, m41 + other.m41,
            m12 + other.m12, m22 + other.m22, m32 + other.m32, m42 + other.m42,
            m13 + other.m13, m23 + other.m23, m33 + other.m33, m43 + other.m43,
            m14 + other.m14, m24 + other.m24, m34 + other.m34, m44 + other.m44
        );
    }

    @Override
    public Matrix4 multiply(float scalar) {
        return new Matrix4(
            m11 * scalar, m21 * scalar, m31 * scalar, m41 * scalar,
            m12 * scalar, m22 * scalar, m32 * scalar, m42 * scalar,
            m13 * scalar, m23 * scalar, m33 * scalar, m43 * scalar,
            m14 * scalar, m24 * scalar, m34 * scalar, m44 * scalar
        );
    }


    @Override
    public Matrix4 one() {
        return IDENTITY;
    }

    @Override
    public Matrix4 multiply(Matrix4 other) {
        return new Matrix4(
            Math.fma(m11, other.m11, Math.fma(m12, other.m21, Math.fma(m13, other.m31, m14 * other.m41))),
            Math.fma(m21, other.m11, Math.fma(m22, other.m21, Math.fma(m23, other.m31, m24 * other.m41))),
            Math.fma(m31, other.m11, Math.fma(m32, other.m21, Math.fma(m33, other.m31, m34 * other.m41))),
            Math.fma(m41, other.m11, Math.fma(m42, other.m21, Math.fma(m43, other.m31, m44 * other.m41))),
            Math.fma(m11, other.m12, Math.fma(m12, other.m22, Math.fma(m13, other.m32, m14 * other.m42))),
            Math.fma(m21, other.m12, Math.fma(m22, other.m22, Math.fma(m23, other.m32, m24 * other.m42))),
            Math.fma(m31, other.m12, Math.fma(m32, other.m22, Math.fma(m33, other.m32, m34 * other.m42))),
            Math.fma(m41, other.m12, Math.fma(m42, other.m22, Math.fma(m43, other.m32, m44 * other.m42))),
            Math.fma(m11, other.m13, Math.fma(m12, other.m23, Math.fma(m13, other.m33, m14 * other.m43))),
            Math.fma(m21, other.m13, Math.fma(m22, other.m23, Math.fma(m23, other.m33, m24 * other.m43))),
            Math.fma(m31, other.m13, Math.fma(m32, other.m23, Math.fma(m33, other.m33, m34 * other.m43))),
            Math.fma(m41, other.m13, Math.fma(m42, other.m23, Math.fma(m43, other.m33, m44 * other.m43))),
            Math.fma(m11, other.m14, Math.fma(m12, other.m24, Math.fma(m13, other.m34, m14 * other.m44))),
            Math.fma(m21, other.m14, Math.fma(m22, other.m24, Math.fma(m23, other.m34, m24 * other.m44))),
            Math.fma(m31, other.m14, Math.fma(m32, other.m24, Math.fma(m33, other.m34, m34 * other.m44))),
            Math.fma(m41, other.m14, Math.fma(m42, other.m24, Math.fma(m43, other.m34, m44 * other.m44)))
        );
    }

    @Override
    public Matrix4 inverse() {
        float m3344 = m33 * m44 - m43 * m34;
        float m2344 = m23 * m44 - m43 * m24;
        float m2334 = m23 * m34 - m33 * m24;
        float m1344 = m13 * m44 - m43 * m14;
        float m1334 = m13 * m34 - m33 * m14;
        float m1324 = m13 * m24 - m23 * m14;

        float a11 = +(m22 * m3344 - m32 * m2344 + m42 * m2334);
        float a12 = -(m12 * m3344 - m32 * m1344 + m42 * m1334);
        float a13 = +(m12 * m2344 - m22 * m1344 + m42 * m1324);
        float a14 = -(m12 * m2334 - m22 * m1334 + m32 * m1324);

        float det = m11 * a11 + m21 * a12 + m31 * a13 + m41 * a14;

        // TODO: Fix epsilons
        if (Math.abs(det) < 1e-6f) {
            throw new ArithmeticException("Cannot invert matrix with near-zero determinant");
        }

        float a21 = -(m21 * m3344 - m31 * m2344 + m41 * m2334);
        float a22 = +(m11 * m3344 - m31 * m1344 + m41 * m1334);
        float a23 = -(m11 * m2344 - m21 * m1344 + m41 * m1324);
        float a24 = +(m11 * m2334 - m21 * m1334 + m31 * m1324);

        float m3244 = m32 * m44 - m42 * m34;
        float m2244 = m22 * m44 - m42 * m24;
        float m2234 = m22 * m34 - m32 * m24;
        float m1244 = m12 * m44 - m42 * m14;
        float m1234 = m12 * m34 - m32 * m14;
        float m1224 = m12 * m24 - m22 * m14;

        float a31 = +(m21 * m3244 - m31 * m2244 + m41 * m2234);
        float a32 = -(m11 * m3244 - m31 * m1244 + m41 * m1234);
        float a33 = +(m11 * m2244 - m21 * m1244 + m41 * m1224);
        float a34 = -(m11 * m2234 - m21 * m1234 + m31 * m1224);

        float m3243 = m32 * m43 - m42 * m33;
        float m2243 = m22 * m43 - m42 * m23;
        float m2233 = m22 * m33 - m32 * m23;
        float m1243 = m12 * m43 - m42 * m13;
        float m1233 = m12 * m33 - m32 * m13;
        float m1223 = m12 * m23 - m22 * m13;

        float a41 = -(m21 * m3243 - m31 * m2243 + m41 * m2233);
        float a42 = +(m11 * m3243 - m31 * m1243 + m41 * m1233);
        float a43 = -(m11 * m2243 - m21 * m1243 + m41 * m1223);
        float a44 = +(m11 * m2233 - m21 * m1233 + m31 * m1223);

        return new Matrix4(
            a11, a21, a31, a41,
            a12, a22, a32, a42,
            a13, a23, a33, a43,
            a14, a24, a34, a44
        ).divide(det);
    }


    @Override
    public Matrix4 transpose() {
        return new Matrix4(
            m11, m12, m13, m14,
            m21, m22, m23, m24,
            m31, m32, m33, m34,
            m41, m42, m43, m44
        );
    }

    @Override
    public float determinant() {
        float m3344 = m33 * m44 - m43 * m34;
        float m2344 = m23 * m44 - m43 * m24;
        float m2334 = m23 * m34 - m33 * m24;
        float m1344 = m13 * m44 - m43 * m14;
        float m1334 = m13 * m34 - m33 * m14;
        float m1324 = m13 * m24 - m23 * m14;

        float a11 = +(m22 * m3344 - m32 * m2344 + m42 * m2334);
        float a12 = -(m12 * m3344 - m32 * m1344 + m42 * m1334);
        float a13 = +(m12 * m2344 - m22 * m1344 + m42 * m1324);
        float a14 = -(m12 * m2334 - m22 * m1334 + m32 * m1324);

        return m11 * a11 + m21 * a12 + m31 * a13 + m41 * a14;
    }

    @Override
    public float get(int row, int column) {
        return switch (row) {
            case 0 -> switch (column) {
                case 0 -> m11;
                case 1 -> m12;
                case 2 -> m13;
                case 3 -> m14;
                default -> throw new IndexOutOfBoundsException();
            };
            case 1 -> switch (column) {
                case 0 -> m21;
                case 1 -> m22;
                case 2 -> m23;
                case 3 -> m24;
                default -> throw new IndexOutOfBoundsException();
            };
            case 2 -> switch (column) {
                case 0 -> m31;
                case 1 -> m32;
                case 2 -> m33;
                case 3 -> m34;
                default -> throw new IndexOutOfBoundsException();
            };
            case 3 -> switch (column) {
                case 0 -> m41;
                case 1 -> m42;
                case 2 -> m43;
                case 3 -> m44;
                default -> throw new IndexOutOfBoundsException();
            };
            default -> throw new IndexOutOfBoundsException();
        };
    }


    @Override
    public int componentCount() {
        return 16;
    }

    @Override
    public void toSliceUnsafe(Floats.Mutable floats, int offset) {
        floats.set(offset/* */, m11);
        floats.set(offset + +1, m21);
        floats.set(offset + +2, m31);
        floats.set(offset + +3, m41);
        floats.set(offset + +4, m12);
        floats.set(offset + +5, m22);
        floats.set(offset + +6, m32);
        floats.set(offset + +7, m42);
        floats.set(offset + +8, m13);
        floats.set(offset + +9, m23);
        floats.set(offset + 10, m33);
        floats.set(offset + 11, m43);
        floats.set(offset + 12, m14);
        floats.set(offset + 13, m24);
        floats.set(offset + 14, m34);
        floats.set(offset + 15, m44);
    }

    @Override
    public void toBufferUnsafe(FloatBuffer floats) {
        floats.put(m11);
        floats.put(m21);
        floats.put(m31);
        floats.put(m41);
        floats.put(m12);
        floats.put(m22);
        floats.put(m32);
        floats.put(m42);
        floats.put(m13);
        floats.put(m23);
        floats.put(m33);
        floats.put(m43);
        floats.put(m14);
        floats.put(m24);
        floats.put(m34);
        floats.put(m44);
    }


    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof Matrix4 other
            && FloatMath.equals(m11, other.m11)
            && FloatMath.equals(m21, other.m21)
            && FloatMath.equals(m31, other.m31)
            && FloatMath.equals(m41, other.m41)
            && FloatMath.equals(m12, other.m12)
            && FloatMath.equals(m22, other.m22)
            && FloatMath.equals(m32, other.m32)
            && FloatMath.equals(m42, other.m42)
            && FloatMath.equals(m13, other.m13)
            && FloatMath.equals(m23, other.m23)
            && FloatMath.equals(m33, other.m33)
            && FloatMath.equals(m43, other.m43)
            && FloatMath.equals(m14, other.m14)
            && FloatMath.equals(m24, other.m24)
            && FloatMath.equals(m34, other.m34)
            && FloatMath.equals(m44, other.m44);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + FloatMath.hashCode(m11);
        result = 31 * result + FloatMath.hashCode(m21);
        result = 31 * result + FloatMath.hashCode(m31);
        result = 31 * result + FloatMath.hashCode(m41);
        result = 31 * result + FloatMath.hashCode(m12);
        result = 31 * result + FloatMath.hashCode(m22);
        result = 31 * result + FloatMath.hashCode(m32);
        result = 31 * result + FloatMath.hashCode(m42);
        result = 31 * result + FloatMath.hashCode(m13);
        result = 31 * result + FloatMath.hashCode(m23);
        result = 31 * result + FloatMath.hashCode(m33);
        result = 31 * result + FloatMath.hashCode(m43);
        result = 31 * result + FloatMath.hashCode(m14);
        result = 31 * result + FloatMath.hashCode(m24);
        result = 31 * result + FloatMath.hashCode(m34);
        result = 31 * result + FloatMath.hashCode(m44);
        return result;
    }

    @Override
    public String toString() {
        return "" +
            "[[" + m11 + ", " + m12 + ", " + m13 + ", " + m14 + "]\n" +
            " [" + m21 + ", " + m22 + ", " + m23 + ", " + m24 + "]\n" +
            " [" + m31 + ", " + m32 + ", " + m33 + ", " + m34 + "]\n" +
            " [" + m41 + ", " + m42 + ", " + m43 + ", " + m44 + "]]";
    }
}
