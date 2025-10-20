package wtf.reversed.toolbox.math;

import java.lang.Math;

public record Vector3f(float x, float y, float z) {
    public static final Vector3f ZERO = new Vector3f(0.0f, 0.0f, 0.0f);

    public static final Vector3f ONE = new Vector3f(1.0f, 1.0f, 1.0f);

    public static final Vector3f X = new Vector3f(1.0f, 0.0f, 0.0f);

    public static final Vector3f Y = new Vector3f(0.0f, 1.0f, 0.0f);

    public static final Vector3f Z = new Vector3f(0.0f, 0.0f, 1.0f);

    public static Vector3f splat(float value) {
        return new Vector3f(value, value, value);
    }

    public Vector3f add(Vector3f other) {
        return new Vector3f(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public Vector3f add(float x, float y, float z) {
        return new Vector3f(this.x + x, this.y + y, this.z + z);
    }

    public Vector3f add(float scalar) {
        return new Vector3f(this.x + scalar, this.y + scalar, this.z + scalar);
    }

    public Vector3f subtract(Vector3f other) {
        return new Vector3f(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    public Vector3f subtract(float x, float y, float z) {
        return new Vector3f(this.x - x, this.y - y, this.z - z);
    }

    public Vector3f subtract(float scalar) {
        return new Vector3f(this.x - scalar, this.y - scalar, this.z - scalar);
    }

    public Vector3f multiply(Vector3f other) {
        return new Vector3f(this.x * other.x, this.y * other.y, this.z * other.z);
    }

    public Vector3f multiply(float x, float y, float z) {
        return new Vector3f(this.x * x, this.y * y, this.z * z);
    }

    public Vector3f multiply(float scalar) {
        return new Vector3f(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    public Vector3f divide(Vector3f other) {
        return new Vector3f(this.x / other.x, this.y / other.y, this.z / other.z);
    }

    public Vector3f divide(float x, float y, float z) {
        return new Vector3f(this.x / x, this.y / y, this.z / z);
    }

    public Vector3f divide(float scalar) {
        return new Vector3f(this.x / scalar, this.y / scalar, this.z / scalar);
    }

    public Vector3f negate() {
        return new Vector3f(-this.x, -this.y, -this.z);
    }

    public float dot(Vector3f other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    public float length() {
        return (float) Math.sqrt(lengthSquared());
    }

    public float lengthSquared() {
        return dot(this);
    }

    public Vector3f normalize() {
        return divide(length());
    }

    public Vector3f fma(Vector3f scale, Vector3f offset) {
        return new Vector3f(
            (float) Math.fma(this.x, scale.x, offset.x),
            (float) Math.fma(this.y, scale.y, offset.y),
            (float) Math.fma(this.z, scale.z, offset.z)
        );
    }
}
