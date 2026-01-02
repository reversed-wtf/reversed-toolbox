package wtf.reversed.toolbox.math;

/**
 * Represents a 4x4 matrix in row-major order.
 *
 * @param m00 The value in the first row and first column.
 * @param m01 The value in the first row and second column.
 * @param m02 The value in the first row and third column.
 * @param m03 The value in the first row and fourth column.
 * @param m10 The value in the second row and first column.
 * @param m11 The value in the second row and second column.
 * @param m12 The value in the second row and third column.
 * @param m13 The value in the second row and fourth column.
 * @param m20 The value in the third row and first column.
 * @param m21 The value in the third row and second column.
 * @param m22 The value in the third row and third column.
 * @param m23 The value in the third row and fourth column.
 * @param m30 The value in the fourth row and first column.
 * @param m31 The value in the fourth row and second column.
 * @param m32 The value in the fourth row and third column.
 * @param m33 The value in the fourth row and fourth column.
 */
public record Matrix4(
    float m00, float m01, float m02, float m03,
    float m10, float m11, float m12, float m13,
    float m20, float m21, float m22, float m23,
    float m30, float m31, float m32, float m33
) implements Matrix<Matrix4> {
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
        return Matrix3.fromRotation(rotation).toMatrix4();
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


    /**
     * Converts this matrix to a {@link Matrix3}.
     *
     * @return A {@link Matrix3} representation of this matrix.
     */
    public Matrix3 toMatrix3() {
        return new Matrix3(
            m00, m01, m02,
            m10, m11, m12,
            m20, m21, m22
        );
    }


    @Override
    public float get(int row, int column) {
        return switch (row) {
            case 0 -> switch (column) {
                case 0 -> m00;
                case 1 -> m01;
                case 2 -> m02;
                case 3 -> m03;
                default -> throw new IndexOutOfBoundsException();
            };
            case 1 -> switch (column) {
                case 0 -> m10;
                case 1 -> m11;
                case 2 -> m12;
                case 3 -> m13;
                default -> throw new IndexOutOfBoundsException();
            };
            case 2 -> switch (column) {
                case 0 -> m20;
                case 1 -> m21;
                case 2 -> m22;
                case 3 -> m23;
                default -> throw new IndexOutOfBoundsException();
            };
            case 3 -> switch (column) {
                case 0 -> m30;
                case 1 -> m31;
                case 2 -> m32;
                case 3 -> m33;
                default -> throw new IndexOutOfBoundsException();
            };
            default -> throw new IndexOutOfBoundsException();
        };
    }

    @Override
    public Matrix4 add(Matrix4 other) {
        return new Matrix4(
            m00 + other.m00, m01 + other.m01, m02 + other.m02, m03 + other.m03,
            m10 + other.m10, m11 + other.m11, m12 + other.m12, m13 + other.m13,
            m20 + other.m20, m21 + other.m21, m22 + other.m22, m23 + other.m23,
            m30 + other.m30, m31 + other.m31, m32 + other.m32, m33 + other.m33
        );
    }

    @Override
    public Matrix4 multiply(float scalar) {
        return new Matrix4(
            m00 * scalar, m01 * scalar, m02 * scalar, m03 * scalar,
            m10 * scalar, m11 * scalar, m12 * scalar, m13 * scalar,
            m20 * scalar, m21 * scalar, m22 * scalar, m23 * scalar,
            m30 * scalar, m31 * scalar, m32 * scalar, m33 * scalar
        );
    }


    @Override
    public Matrix4 multiply(Matrix4 other) {
        return new Matrix4(
            m00 * other.m00 + m01 * other.m10 + m02 * other.m20 + m03 * other.m30,
            m00 * other.m01 + m01 * other.m11 + m02 * other.m21 + m03 * other.m31,
            m00 * other.m02 + m01 * other.m12 + m02 * other.m22 + m03 * other.m32,
            m00 * other.m03 + m01 * other.m13 + m02 * other.m23 + m03 * other.m33,
            m10 * other.m00 + m11 * other.m10 + m12 * other.m20 + m13 * other.m30,
            m10 * other.m01 + m11 * other.m11 + m12 * other.m21 + m13 * other.m31,
            m10 * other.m02 + m11 * other.m12 + m12 * other.m22 + m13 * other.m32,
            m10 * other.m03 + m11 * other.m13 + m12 * other.m23 + m13 * other.m33,
            m20 * other.m00 + m21 * other.m10 + m22 * other.m20 + m23 * other.m30,
            m20 * other.m01 + m21 * other.m11 + m22 * other.m21 + m23 * other.m31,
            m20 * other.m02 + m21 * other.m12 + m22 * other.m22 + m23 * other.m32,
            m20 * other.m03 + m21 * other.m13 + m22 * other.m23 + m23 * other.m33,
            m30 * other.m00 + m31 * other.m10 + m32 * other.m20 + m33 * other.m30,
            m30 * other.m01 + m31 * other.m11 + m32 * other.m21 + m33 * other.m31,
            m30 * other.m02 + m31 * other.m12 + m32 * other.m22 + m33 * other.m32,
            m30 * other.m03 + m31 * other.m13 + m32 * other.m23 + m33 * other.m33
        );
    }

    @Override
    public Matrix4 transpose() {
        return new Matrix4(
            m00, m10, m20, m30,
            m01, m11, m21, m31,
            m02, m12, m22, m32,
            m03, m13, m23, m33
        );
    }

    @Override
    public float determinant() {
        float m2233 = m22 * m33 - m23 * m32;
        float m2133 = m21 * m33 - m23 * m31;
        float m2132 = m21 * m32 - m22 * m31;
        float m2033 = m20 * m33 - m23 * m30;
        float m2032 = m20 * m32 - m22 * m30;
        float m2031 = m20 * m31 - m21 * m30;

        float r00 = +(m11 * m2233 - m12 * m2133 + m13 * m2132);
        float r10 = -(m10 * m2233 - m12 * m2033 + m13 * m2032);
        float r20 = +(m10 * m2133 - m11 * m2033 + m13 * m2031);
        float r30 = -(m10 * m2132 - m11 * m2032 + m12 * m2031);
        return m00 * r00 + m01 * r10 + m02 * r20 + m03 * r30;
    }

    @Override
    public Matrix4 inverse() {
        float m2233 = m22 * m33 - m23 * m32;
        float m2133 = m21 * m33 - m23 * m31;
        float m2132 = m21 * m32 - m22 * m31;
        float m2033 = m20 * m33 - m23 * m30;
        float m2032 = m20 * m32 - m22 * m30;
        float m2031 = m20 * m31 - m21 * m30;

        float r00 = +(m11 * m2233 - m12 * m2133 + m13 * m2132);
        float r10 = -(m10 * m2233 - m12 * m2033 + m13 * m2032);
        float r20 = +(m10 * m2133 - m11 * m2033 + m13 * m2031);
        float r30 = -(m10 * m2132 - m11 * m2032 + m12 * m2031);
        float det = m00 * r00 + m01 * r10 + m02 * r20 + m03 * r30;

        if (Math.abs(det) < 1e-6) {
            throw new ArithmeticException("Matrix is singular, cannot invert.");
        }

        float r01 = -(m01 * m2233 - m02 * m2133 + m03 * m2132);
        float r11 = +(m00 * m2233 - m02 * m2033 + m03 * m2032);
        float r21 = -(m00 * m2133 - m01 * m2033 + m03 * m2031);
        float r31 = +(m00 * m2132 - m01 * m2032 + m02 * m2031);

        float m1233 = m12 * m33 - m13 * m32;
        float m1133 = m11 * m33 - m13 * m31;
        float m1132 = m11 * m32 - m12 * m31;
        float m1033 = m10 * m33 - m13 * m30;
        float m1032 = m10 * m32 - m12 * m30;
        float m1031 = m10 * m31 - m11 * m30;

        float r02 = +(m01 * m1233 - m02 * m1133 + m03 * m1132);
        float r12 = -(m00 * m1233 - m02 * m1033 + m03 * m1032);
        float r22 = +(m00 * m1133 - m01 * m1033 + m03 * m1031);
        float r32 = -(m00 * m1132 - m01 * m1032 + m02 * m1031);

        float m1223 = m12 * m23 - m13 * m22;
        float m1123 = m11 * m23 - m13 * m21;
        float m1122 = m11 * m22 - m12 * m21;
        float m1023 = m10 * m23 - m13 * m20;
        float m1022 = m10 * m22 - m12 * m20;
        float m1021 = m10 * m21 - m11 * m20;

        float r03 = -(m01 * m1223 - m02 * m1123 + m03 * m1122);
        float r13 = +(m00 * m1223 - m02 * m1023 + m03 * m1022);
        float r23 = -(m00 * m1123 - m01 * m1023 + m03 * m1021);
        float r33 = +(m00 * m1122 - m01 * m1022 + m02 * m1021);

        return new Matrix4(
            r00, r01, r02, r03,
            r10, r11, r12, r13,
            r20, r21, r22, r23,
            r30, r31, r32, r33
        ).divide(det);
    }


    @Override
    public boolean equals(Object obj) {
        return obj instanceof Matrix4 other
            && FloatMath.equals(m00, other.m00)
            && FloatMath.equals(m01, other.m01)
            && FloatMath.equals(m02, other.m02)
            && FloatMath.equals(m03, other.m03)
            && FloatMath.equals(m10, other.m10)
            && FloatMath.equals(m11, other.m11)
            && FloatMath.equals(m12, other.m12)
            && FloatMath.equals(m13, other.m13)
            && FloatMath.equals(m20, other.m20)
            && FloatMath.equals(m21, other.m21)
            && FloatMath.equals(m22, other.m22)
            && FloatMath.equals(m23, other.m23)
            && FloatMath.equals(m30, other.m30)
            && FloatMath.equals(m31, other.m31)
            && FloatMath.equals(m32, other.m32)
            && FloatMath.equals(m33, other.m33);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + FloatMath.hashCode(m00);
        result = 31 * result + FloatMath.hashCode(m01);
        result = 31 * result + FloatMath.hashCode(m02);
        result = 31 * result + FloatMath.hashCode(m03);
        result = 31 * result + FloatMath.hashCode(m10);
        result = 31 * result + FloatMath.hashCode(m11);
        result = 31 * result + FloatMath.hashCode(m12);
        result = 31 * result + FloatMath.hashCode(m13);
        result = 31 * result + FloatMath.hashCode(m20);
        result = 31 * result + FloatMath.hashCode(m21);
        result = 31 * result + FloatMath.hashCode(m22);
        result = 31 * result + FloatMath.hashCode(m23);
        result = 31 * result + FloatMath.hashCode(m30);
        result = 31 * result + FloatMath.hashCode(m31);
        result = 31 * result + FloatMath.hashCode(m32);
        result = 31 * result + FloatMath.hashCode(m33);
        return result;
    }

    @Override
    public String toString() {
        return "" +
            "[[" + m00 + ", " + m01 + ", " + m02 + ", " + m03 + "]\n" +
            " [" + m10 + ", " + m11 + ", " + m12 + ", " + m13 + "]\n" +
            " [" + m20 + ", " + m21 + ", " + m22 + ", " + m23 + "]\n" +
            " [" + m30 + ", " + m31 + ", " + m32 + ", " + m33 + "]]";
    }
}
