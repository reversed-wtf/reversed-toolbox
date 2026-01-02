package wtf.reversed.toolbox.math;

/**
 * Represents a 2x2 matrix in row-major order.
 *
 * @param m00 The value in the first row and first column.
 * @param m01 The value in the first row and second column.
 * @param m10 The value in the second row and first column.
 * @param m11 The value in the second row and second column.
 */
public record Matrix2(
    float m00, float m01,
    float m10, float m11
) implements Matrix<Matrix2> {
    /**
     * The identity matrix for 2x2 transformations.
     */
    public static final Matrix2 IDENTITY = new Matrix2(
        1.0f, 0.0f,
        0.0f, 1.0f
    );


    @Override
    public Matrix2 add(Matrix2 other) {
        return new Matrix2(
            m00 + other.m00, m01 + other.m01,
            m10 + other.m10, m11 + other.m11
        );
    }

    @Override
    public Matrix2 multiply(float scalar) {
        return new Matrix2(
            m00 * scalar, m01 * scalar,
            m10 * scalar, m11 * scalar
        );
    }


    @Override
    public Matrix2 multiply(Matrix2 other) {
        return new Matrix2(
            m00 * other.m00 + m01 * other.m10,
            m00 * other.m01 + m01 * other.m11,
            m10 * other.m00 + m11 * other.m10,
            m10 * other.m01 + m11 * other.m11
        );
    }

    @Override
    public Matrix2 transpose() {
        return new Matrix2(
            m00, m10,
            m01, m11
        );
    }

    @Override
    public float determinant() {
        return m00 * m11 - m01 * m10;
    }

    @Override
    public Matrix2 inverse() {
        float det = determinant();

        if (Math.abs(determinant()) < 1e-6) {
            throw new ArithmeticException("Matrix is singular, cannot invert.");
        }

        return new Matrix2(
            +m11, -m01,
            -m10, +m00
        ).divide(det);
    }

    @Override
    public float get(int row, int column) {
        return switch (row) {
            case 0 -> switch (column) {
                case 0 -> m00;
                case 1 -> m01;
                default -> throw new IndexOutOfBoundsException();
            };
            case 1 -> switch (column) {
                case 0 -> m10;
                case 1 -> m11;
                default -> throw new IndexOutOfBoundsException();
            };
            default -> throw new IndexOutOfBoundsException();
        };
    }


    @Override
    public boolean equals(Object obj) {
        return obj instanceof Matrix2 other
            && FloatMath.equals(m00, other.m00)
            && FloatMath.equals(m01, other.m01)
            && FloatMath.equals(m10, other.m10)
            && FloatMath.equals(m11, other.m11);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + FloatMath.hashCode(m00);
        result = 31 * result + FloatMath.hashCode(m01);
        result = 31 * result + FloatMath.hashCode(m10);
        result = 31 * result + FloatMath.hashCode(m11);
        return result;
    }

    @Override
    public String toString() {
        return "" +
            "[[" + m00 + ", " + m01 + "]\n" +
            " [" + m10 + ", " + m11 + "]]";
    }
}
