package wtf.reversed.toolbox.math;

import wtf.reversed.toolbox.collect.*;

import java.nio.*;

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
    public static Matrix2 fromRotation(float rotation, Angle unit) {
        float sin = FloatMath.sin(unit.toRadians(rotation));
        float cos = FloatMath.cos(unit.toRadians(rotation));

        return new Matrix2(
            +cos, +sin,
            -sin, +cos
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


    /**
     * Converts this matrix to a {@link Matrix3}.
     *
     * @return A {@link Matrix3} representation of this matrix.
     */
    public Matrix3 toMatrix3() {
        return new Matrix3(
            m00, m01, 0.f,
            m10, m11, 0.f,
            0.f, 0.f, 1.f
        );
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
    public int componentCount() {
        return 4;
    }

    @Override
    public void toSliceUnsafe(Floats.Mutable floats, int offset) {
        floats.set(offset/**/, m00);
        floats.set(offset + 1, m01);
        floats.set(offset + 2, m10);
        floats.set(offset + 3, m11);
    }

    @Override
    public void toBufferUnsafe(FloatBuffer floats) {
        floats.put(m00);
        floats.put(m01);
        floats.put(m10);
        floats.put(m11);
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
