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
    public Matrix3 multiply(Matrix3 other) {
        return new Matrix3(
            m00 * other.m00 + m01 * other.m10 + m02 * other.m20,
            m00 * other.m01 + m01 * other.m11 + m02 * other.m21,
            m00 * other.m02 + m01 * other.m12 + m02 * other.m22,
            m10 * other.m00 + m11 * other.m10 + m12 * other.m20,
            m10 * other.m01 + m11 * other.m11 + m12 * other.m21,
            m10 * other.m02 + m11 * other.m12 + m12 * other.m22,
            m20 * other.m00 + m21 * other.m10 + m22 * other.m20,
            m20 * other.m01 + m21 * other.m11 + m22 * other.m21,
            m20 * other.m02 + m21 * other.m12 + m22 * other.m22
        );
    }

    @Override
    public Matrix3 transpose() {
        return new Matrix3(
            m00, m10, m20,
            m01, m11, m21,
            m02, m12, m22
        );
    }

    @Override
    public float determinant() {
        float r00 = +(m11 * m22 - m12 * m21);
        float r10 = -(m10 * m22 - m12 * m20);
        float r20 = +(m10 * m21 - m11 * m20);
        return m00 * r00 + m01 * r10 + m02 * r20;
    }

    @Override
    public Matrix3 inverse() {
        float r00 = +(m11 * m22 - m12 * m21);
        float r10 = -(m10 * m22 - m12 * m20);
        float r20 = +(m10 * m21 - m11 * m20);
        float det = m00 * r00 + m01 * r10 + m02 * r20;

        if (Math.abs(det) < 1e-6) {
            throw new ArithmeticException("Matrix is singular, cannot invert.");
        }

        float r01 = -(m01 * m22 - m02 * m21);
        float r11 = +(m00 * m22 - m02 * m20);
        float r21 = -(m00 * m21 - m01 * m20);

        float r02 = +(m01 * m12 - m02 * m11);
        float r12 = -(m00 * m12 - m02 * m10);
        float r22 = +(m00 * m11 - m01 * m10);

        return new Matrix3(
            r00, r01, r02,
            r10, r11, r12,
            r20, r21, r22
        ).divide(det);
    }

    @Override
    public float get(int row, int column) {
        return switch (row) {
            case 0 -> switch (column) {
                case 0 -> m00;
                case 1 -> m01;
                case 2 -> m02;
                default -> throw new IndexOutOfBoundsException();
            };
            case 1 -> switch (column) {
                case 0 -> m10;
                case 1 -> m11;
                case 2 -> m12;
                default -> throw new IndexOutOfBoundsException();
            };
            case 2 -> switch (column) {
                case 0 -> m20;
                case 1 -> m21;
                case 2 -> m22;
                default -> throw new IndexOutOfBoundsException();
            };
            default -> throw new IndexOutOfBoundsException();
        };
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
