package wtf.reversed.toolbox.math;

/**
 * Represents a 3x3 matrix in row-major order.
 *
 * @param m00 The value in the first row and first column.
 * @param m01 The value in the first row and second column.
 * @param m02 The value in the first row and third column.
 * @param m10 The value in the second row and first column.
 * @param m11 The value in the second row and second column.
 * @param m12 The value in the second row and third column.
 * @param m20 The value in the third row and first column.
 * @param m21 The value in the third row and second column.
 * @param m22 The value in the third row and third column.
 */
public record Matrix3(
    float m00, float m01, float m02,
    float m10, float m11, float m12,
    float m20, float m21, float m22
) implements Matrix<Matrix3> {
    /**
     * The identity matrix for 3x3 transformations.
     */
    public static final Matrix3 IDENTITY = new Matrix3(
        1.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 1.0f
    );

    @Override
    public Matrix3 add(Matrix3 other) {
        return new Matrix3(
            m00 + other.m00, m01 + other.m01, m02 + other.m02,
            m10 + other.m10, m11 + other.m11, m12 + other.m12,
            m20 + other.m20, m21 + other.m21, m22 + other.m22
        );
    }

    @Override
    public Matrix3 multiply(float scalar) {
        return new Matrix3(
            m00 * scalar, m01 * scalar, m02 * scalar,
            m10 * scalar, m11 * scalar, m12 * scalar,
            m20 * scalar, m21 * scalar, m22 * scalar
        );
    }


    @Override
    public boolean equals(Object obj) {
        return obj instanceof Matrix3 other
            && FloatMath.equals(m00, other.m00)
            && FloatMath.equals(m01, other.m01)
            && FloatMath.equals(m02, other.m02)
            && FloatMath.equals(m10, other.m10)
            && FloatMath.equals(m11, other.m11)
            && FloatMath.equals(m12, other.m12)
            && FloatMath.equals(m20, other.m20)
            && FloatMath.equals(m21, other.m21)
            && FloatMath.equals(m22, other.m22);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + FloatMath.hashCode(m00);
        result = 31 * result + FloatMath.hashCode(m01);
        result = 31 * result + FloatMath.hashCode(m02);
        result = 31 * result + FloatMath.hashCode(m10);
        result = 31 * result + FloatMath.hashCode(m11);
        result = 31 * result + FloatMath.hashCode(m12);
        result = 31 * result + FloatMath.hashCode(m20);
        result = 31 * result + FloatMath.hashCode(m21);
        result = 31 * result + FloatMath.hashCode(m22);
        return result;
    }

    @Override
    public String toString() {
        return "" +
            "[[" + m00 + ", " + m01 + ", " + m02 + "]\n" +
            " [" + m10 + ", " + m11 + ", " + m12 + "]\n" +
            " [" + m20 + ", " + m21 + ", " + m22 + "]]";
    }
}
