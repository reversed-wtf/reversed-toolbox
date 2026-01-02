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
) implements Linear<Matrix4> {
    /**
     * The identity matrix for 4x4 transformations.
     */
    public static final Matrix4 IDENTITY = new Matrix4(
        1.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 0.0f, 1.0f
    );

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
