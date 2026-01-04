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
) implements Vector<Quaternion>, Divisible<Quaternion>, Primitive {
    /**
     * The identity quaternion, representing no rotation.
     */
    public static final Quaternion IDENTITY = new Quaternion(0.0f, 0.0f, 0.0f, 1.0f);

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
     * <p>
     * The specified order specifies <b>intrinsic</b> rotations.
     * If you need extrinsic rotations, use the `swap()` method on the order.
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
     * <p>
     * The specified order specifies <b>intrinsic</b> rotations.
     * If you need extrinsic rotations, use the `swap()` method on the order.
     *
     * @param angleX The angle around the X axis.
     * @param angleY The angle around the Y axis.
     * @param angleZ The angle around the Z axis.
     * @param unit   The unit of the angles.
     * @param order  The order of rotation.
     * @return The quaternion.
     */
    public static Quaternion fromEuler(float angleX, float angleY, float angleZ, Angle unit, Order order) {
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
            case XZY -> new Quaternion(scc - css, csc - scs, ccs + ssc, ccc + sss);
            case YXZ -> new Quaternion(scc + css, csc - scs, ccs - ssc, ccc + sss);
            case YZX -> new Quaternion(scc + css, csc + scs, ccs - ssc, ccc - sss);
            case ZXY -> new Quaternion(scc - css, csc + scs, ccs + ssc, ccc - sss);
            case ZYX -> new Quaternion(scc - css, csc + scs, ccs - ssc, ccc + sss);
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
            matrix.m11(), matrix.m21(), matrix.m31(),
            matrix.m12(), matrix.m22(), matrix.m32(),
            matrix.m13(), matrix.m23(), matrix.m33()
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
            matrix.m11(), matrix.m21(), matrix.m31(),
            matrix.m12(), matrix.m22(), matrix.m32(),
            matrix.m13(), matrix.m23(), matrix.m33()
        );
    }

    /**
     * Creates a new quaternion from a rotation matrix. The matrix must be orthogonal.
     *
     * @param m11 The element at row 1, column 1 of the matrix.
     * @param m21 The element at row 1, column 2 of the matrix.
     * @param m31 The element at row 1, column 3 of the matrix.
     * @param m12 The element at row 2, column 1 of the matrix.
     * @param m22 The element at row 2, column 2 of the matrix.
     * @param m32 The element at row 2, column 3 of the matrix.
     * @param m13 The element at row 3, column 1 of the matrix.
     * @param m23 The element at row 3, column 2 of the matrix.
     * @param m33 The element at row 3, column 3 of the matrix.
     * @return The quaternion.
     */
    public static Quaternion fromMatrix(
        float m11, float m21, float m31,
        float m12, float m22, float m32,
        float m13, float m23, float m33
    ) {
        float invLenX = FloatMath.rsqrt(m11 * m11 + m21 * m21 + m31 * m31);
        float invLenY = FloatMath.rsqrt(m12 * m12 + m22 * m22 + m32 * m32);
        float invLenZ = FloatMath.rsqrt(m13 * m13 + m23 * m23 + m33 * m33);

        return fromMatrixNormalized(
            m11 * invLenX, m21 * invLenX, m31 * invLenX,
            m12 * invLenY, m22 * invLenY, m32 * invLenY,
            m13 * invLenZ, m23 * invLenZ, m33 * invLenZ
        );
    }

    /**
     * Creates a new quaternion from a normalized rotation matrix.
     *
     * @param m11 The element at row 1, column 1 of the matrix.
     * @param m21 The element at row 1, column 2 of the matrix.
     * @param m31 The element at row 1, column 3 of the matrix.
     * @param m12 The element at row 2, column 1 of the matrix.
     * @param m22 The element at row 2, column 2 of the matrix.
     * @param m32 The element at row 2, column 3 of the matrix.
     * @param m13 The element at row 3, column 1 of the matrix.
     * @param m23 The element at row 3, column 2 of the matrix.
     * @param m33 The element at row 3, column 3 of the matrix.
     * @return The quaternion.
     */
    public static Quaternion fromMatrixNormalized(
        float m11, float m21, float m31,
        float m12, float m22, float m32,
        float m13, float m23, float m33
    ) {
        float trace = m11 + m22 + m33;
        if (trace > 0.0f) {
            float s = FloatMath.sqrt(1.0f + trace);
            float invS = 0.5f / s;
            return new Quaternion(
                (m32 - m23) * invS,
                (m13 - m31) * invS,
                (m21 - m12) * invS,
                0.5f * s
            );
        } else if (m11 > m22 && m11 > m33) {
            float s = FloatMath.sqrt(1.0f + m11 - m22 - m33);
            float invS = 0.5f / s;
            return new Quaternion(
                0.5f * s,
                (m21 + m12) * invS,
                (m13 + m31) * invS,
                (m32 - m23) * invS
            );
        } else if (m22 > m33) {
            float s = FloatMath.sqrt(1.0f + m22 - m11 - m33);
            float invS = 0.5f / s;
            return new Quaternion(
                (m21 + m12) * invS,
                0.5f * s,
                (m32 + m23) * invS,
                (m13 - m31) * invS
            );
        } else {
            float s = FloatMath.sqrt(1.0f + m33 - m11 - m22);
            float invS = 0.5f / s;
            return new Quaternion(
                (m13 + m31) * invS,
                (m32 + m23) * invS,
                0.5f * s,
                (m21 - m12) * invS
            );
        }
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
    public float dot(Quaternion other) {
        return x * other.x + y * other.y + z * other.z + w * other.w;
    }


    @Override
    public Quaternion one() {
        return IDENTITY;
    }

    @Override
    public Quaternion multiply(Quaternion other) {
        return new Quaternion(
            w * other.x + x * other.w + y * other.z - z * other.y,
            w * other.y - x * other.z + y * other.w + z * other.x,
            w * other.z + x * other.y - y * other.x + z * other.w,
            w * other.w - x * other.x - y * other.y - z * other.z
        );
    }

    @Override
    public Quaternion inverse() {
        return conjugate().divide(lengthSquared());
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


    /**
     * Calculates the conjugate of this quaternion.
     *
     * @return The conjugate.
     */
    public Quaternion conjugate() {
        return new Quaternion(-x, -y, -z, w);
    }

    /**
     * Calculates the spherical linear interpolation between this quaternion and another one.
     *
     * @param other The other quaternion.
     * @param t     The interpolation factor.
     * @return The interpolated quaternion.
     */
    public Quaternion slerp(Quaternion other, float t) {
        float cosOmega = dot(other);
        float sign = 1.0f;
        if (cosOmega < 0.0f) {
            cosOmega = -cosOmega;
            sign = -1.0f;
        }

        float s1;
        float s2;
        if (cosOmega > 1.0f - EPSILON) {
            s1 = 1.0f - t;
            s2 = t * sign;
        } else {
            float omega = FloatMath.acos(cosOmega);
            float invSinOmega = 1.0f / FloatMath.sin(omega);
            s1 = FloatMath.sin((1.0f - t) * omega) * invSinOmega;
            s2 = FloatMath.sin(t * omega) * invSinOmega * sign;
        }
        return multiply(s1).add(other.multiply(s2));
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
        result = 31 * result + FloatMath.hashCode(x);
        result = 31 * result + FloatMath.hashCode(y);
        result = 31 * result + FloatMath.hashCode(z);
        result = 31 * result + FloatMath.hashCode(w);
        return result;
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + ", " + z + ", " + w + "]";
    }
}
