package wtf.reversed.toolbox.math;

import wtf.reversed.toolbox.collect.*;
import wtf.reversed.toolbox.io.*;

import java.io.*;
import java.nio.*;

/**
 * A quaternion representing a rotation in 3D space.
 *
 * @param x The x component.
 * @param y The y component.
 * @param z The z component.
 * @param w The w component.
 */
public record Quaternion(
    float x,
    float y,
    float z,
    float w
) implements Vector<Quaternion>, Primitive {
    /**
     * The identity quaternion, representing no rotation.
     */
    public static final Quaternion IDENTITY = new Quaternion(0.0f, 0.0f, 0.0f, 1.0f);

    /**
     * Creates a new quaternion from an axis and angle.
     *
     * @param axis  The axis of rotation.
     * @param angle The angle of rotation.
     * @param unit  The unit of the angle.
     * @return The quaternion.
     */
    public static Quaternion fromAxisAngle(Vector3 axis, float angle, Angle unit) {
        return fromAxisAngle(axis.x(), axis.y(), axis.z(), angle, unit);
    }

    /**
     * Creates a new quaternion from an axis and angle.
     *
     * @param axisX The x component of the axis of rotation.
     * @param axisY The y component of the axis of rotation.
     * @param axisZ The z component of the axis of rotation.
     * @param angle The angle of rotation.
     * @param unit  The unit of the angle.
     * @return The quaternion.
     */
    private static Quaternion fromAxisAngle(float axisX, float axisY, float axisZ, float angle, Angle unit) {
        float halfAngle = unit.toRadians(angle) * 0.5f;
        float sin = FloatMath.sin(halfAngle);
        float cos = FloatMath.cos(halfAngle);

        return new Quaternion(
            axisX * sin,
            axisY * sin,
            axisZ * sin,
            cos
        );
    }

    /**
     * Creates a new quaternion from Euler angles.
     *
     * @param angle The Euler angles.
     * @param unit  The unit of the angles.
     * @param order The order of rotation.
     * @return The quaternion.
     */
    public static Quaternion fromEuler(Vector3 angle, Angle unit, Order order) {
        return fromEuler(angle.x(), angle.y(), angle.z(), unit, order);
    }

    /**
     * Creates a new quaternion from Euler angles.
     *
     * @param angleX The angle around the X axis.
     * @param angleY The angle around the Y axis.
     * @param angleZ The angle around the Z axis.
     * @param unit   The unit of the angles.
     * @param order  The order of rotation.
     * @return The quaternion.
     */
    private static Quaternion fromEuler(float angleX, float angleY, float angleZ, Angle unit, Order order) {
        float halfAngleX = unit.toRadians(angleX) * 0.5f;
        float sx = FloatMath.sin(halfAngleX);
        float cx = FloatMath.cos(halfAngleX);

        float halfAngleY = unit.toRadians(angleY) * 0.5f;
        float sy = FloatMath.sin(halfAngleY);
        float cy = FloatMath.cos(halfAngleY);

        float halfAngleZ = unit.toRadians(angleZ) * 0.5f;
        float sz = FloatMath.sin(halfAngleZ);
        float cz = FloatMath.cos(halfAngleZ);

        float scc = sx * cy * cz;
        float css = cx * sy * sz;
        float csc = cx * sy * cz;
        float scs = sx * cy * sz;
        float ccs = cx * cy * sz;
        float ssc = sx * sy * cz;
        float ccc = cx * cy * cz;
        float sss = sx * sy * sz;

        return switch (order) {
            case XYZ -> new Quaternion(scc + css, csc - scs, ccs + ssc, ccc - sss);
            case YXZ -> new Quaternion(scc + css, csc - scs, ccs - ssc, ccc + sss);
            case ZXY -> new Quaternion(scc - css, csc + scs, ccs + ssc, ccc - sss);
            case ZYX -> new Quaternion(scc - css, csc + scs, ccs - ssc, ccc + sss);
            case YZX -> new Quaternion(scc + css, csc + scs, ccs - ssc, ccc - sss);
            case XZY -> new Quaternion(scc - css, csc - scs, ccs + ssc, ccc + sss);
        };
    }

    /**
     * Creates a new quaternion from a rotation matrix. The matrix must be orthogonal.
     *
     * @param matrix The rotation matrix.
     * @return The quaternion.
     */
    public static Quaternion fromMatrix(Matrix3 matrix) {
        return fromMatrix(
            matrix.m00(), matrix.m01(), matrix.m02(),
            matrix.m10(), matrix.m11(), matrix.m12(),
            matrix.m20(), matrix.m21(), matrix.m22()
        );
    }

    /**
     * Creates a new quaternion from a rotation matrix. The matrix must be orthogonal.
     *
     * @param matrix The rotation matrix.
     * @return The quaternion.
     */
    public static Quaternion fromMatrix(Matrix4 matrix) {
        return fromMatrix(
            matrix.m00(), matrix.m01(), matrix.m02(),
            matrix.m10(), matrix.m11(), matrix.m12(),
            matrix.m20(), matrix.m21(), matrix.m22()
        );
    }

    /**
     * Creates a new quaternion from a rotation matrix. The matrix must be orthogonal.
     *
     * @param m00 The element at row 0, column 0 of the matrix.
     * @param m01 The element at row 0, column 1 of the matrix.
     * @param m02 The element at row 0, column 2 of the matrix.
     * @param m10 The element at row 1, column 0 of the matrix.
     * @param m11 The element at row 1, column 1 of the matrix.
     * @param m12 The element at row 1, column 2 of the matrix.
     * @param m20 The element at row 2, column 0 of the matrix.
     * @param m21 The element at row 2, column 1 of the matrix.
     * @param m22 The element at row 2, column 2 of the matrix.
     * @return The quaternion.
     */
    public static Quaternion fromMatrix(
        float m00, float m01, float m02,
        float m10, float m11, float m12,
        float m20, float m21, float m22
    ) {
        float inverseLengthX = 1.0f / FloatMath.sqrt(m00 * m00 + m10 * m10 + m20 * m20);
        float inverseLengthY = 1.0f / FloatMath.sqrt(m01 * m01 + m11 * m11 + m21 * m21);
        float inverseLengthZ = 1.0f / FloatMath.sqrt(m02 * m02 + m12 * m12 + m22 * m22);

        return fromMatrixNormalized(
            m00 * inverseLengthX, m01 * inverseLengthX, m02 * inverseLengthX,
            m10 * inverseLengthY, m11 * inverseLengthY, m12 * inverseLengthY,
            m20 * inverseLengthZ, m21 * inverseLengthZ, m22 * inverseLengthZ
        );
    }

    /**
     * Creates a new quaternion from a normalized rotation matrix.
     *
     * @param m00 The element at row 0, column 0 of the matrix.
     * @param m01 The element at row 0, column 1 of the matrix.
     * @param m02 The element at row 0, column 2 of the matrix.
     * @param m10 The element at row 1, column 0 of the matrix.
     * @param m11 The element at row 1, column 1 of the matrix.
     * @param m12 The element at row 1, column 2 of the matrix.
     * @param m20 The element at row 2, column 0 of the matrix.
     * @param m21 The element at row 2, column 1 of the matrix.
     * @param m22 The element at row 2, column 2 of the matrix.
     * @return The quaternion.
     */
    public static Quaternion fromMatrixNormalized(
        float m00, float m01, float m02,
        float m10, float m11, float m12,
        float m20, float m21, float m22
    ) {
        float trace = m00 + m11 + m22;
        if (trace > 0.0f) {
            float s = FloatMath.sqrt(1.0f + trace);
            float invS = 0.5f / s;
            return new Quaternion(
                (m12 - m21) * invS,
                (m20 - m02) * invS,
                (m01 - m10) * invS,
                0.5f * s
            );
        } else if (m00 > m11 && m00 > m22) {
            float s = FloatMath.sqrt(1.0f + m00 - m11 - m22);
            float invS = 0.5f / s;
            return new Quaternion(
                0.5f * s,
                (m01 + m10) * invS,
                (m20 + m02) * invS,
                (m12 - m21) * invS
            );
        } else if (m11 > m22) {
            float s = FloatMath.sqrt(1.0f + m11 - m00 - m22);
            float invS = 0.5f / s;
            return new Quaternion(
                (m01 + m10) * invS,
                0.5f * s,
                (m12 + m21) * invS,
                (m20 - m02) * invS
            );
        } else {
            float s = FloatMath.sqrt(1.0f + m22 - m00 - m11);
            float invS = 0.5f / s;
            return new Quaternion(
                (m20 + m02) * invS,
                (m12 + m21) * invS,
                0.5f * s,
                (m01 - m10) * invS
            );
        }
    }

    /**
     * Creates a new quaternion from a binary source.
     *
     * @param source The binary source.
     * @return The quaternion.
     * @throws IOException If an I/O error occurs.
     */
    public static Quaternion read(BinarySource source) throws IOException {
        float x = source.readFloat();
        float y = source.readFloat();
        float z = source.readFloat();
        float w = source.readFloat();
        return new Quaternion(x, y, z, w);
    }


    @Override
    public float get(int index) {
        return switch (index) {
            case 0 -> x;
            case 1 -> y;
            case 2 -> z;
            case 3 -> w;
            default -> throw new IndexOutOfBoundsException();
        };
    }

    @Override
    public Quaternion add(Quaternion other) {
        return new Quaternion(x + other.x, y + other.y, z + other.z, w + other.w);
    }

    @Override
    public Quaternion multiply(float scalar) {
        return new Quaternion(x * scalar, y * scalar, z * scalar, w * scalar);
    }

    @Override
    public float dot(Quaternion other) {
        return x * other.x + y * other.y + z * other.z + w * other.w;
    }


    /**
     * Multiplies this quaternion with another quaternion.
     *
     * @param other The other quaternion.
     * @return The product.
     */
    public Quaternion multiply(Quaternion other) {
        return new Quaternion(
            w * other.x + x * other.w + y * other.z - z * other.y,
            w * other.y - x * other.z + y * other.w + z * other.x,
            w * other.z + x * other.y - y * other.x + z * other.w,
            w * other.w - x * other.x - y * other.y - z * other.z
        );
    }

    /**
     * Returns the conjugate of this quaternion.
     *
     * @return The conjugate.
     */
    public Quaternion conjugate() {
        return new Quaternion(-x, -y, -z, w);
    }

    /**
     * Returns the inverse of this quaternion.
     *
     * @return The inverse.
     */
    public Quaternion inverse() {
        return conjugate().multiply(1.0f / dot(this));
    }


    @Override
    public int componentCount() {
        return 4;
    }

    @Override
    public void toSliceUnsafe(Floats.Mutable floats, int offset) {
        floats.set(offset/**/, x);
        floats.set(offset + 1, y);
        floats.set(offset + 2, z);
        floats.set(offset + 3, w);
    }

    @Override
    public void toBufferUnsafe(FloatBuffer floats) {
        floats.put(x);
        floats.put(y);
        floats.put(z);
        floats.put(w);
    }


    @Override
    public boolean equals(Object obj) {
        return obj instanceof Quaternion other
            && FloatMath.equals(x, other.x)
            && FloatMath.equals(y, other.y)
            && FloatMath.equals(z, other.z)
            && FloatMath.equals(w, other.w);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + Float.hashCode(x);
        result = 31 * result + Float.hashCode(y);
        result = 31 * result + Float.hashCode(z);
        result = 31 * result + Float.hashCode(w);
        return result;
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + ", " + z + ", " + w + "]";
    }
}
