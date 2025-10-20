package wtf.reversed.toolbox.math;

import java.lang.Math;

public record Vector4f(float x, float y, float z, float w) {
    public static final Vector4f ZERO = new Vector4f(0.0f, 0.0f, 0.0f, 0.0f);

    public static final Vector4f ONE = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);

    public static final Vector4f X = new Vector4f(1.0f, 0.0f, 0.0f, 0.0f);

    public static final Vector4f Y = new Vector4f(0.0f, 1.0f, 0.0f, 0.0f);

    public static final Vector4f Z = new Vector4f(0.0f, 0.0f, 1.0f, 0.0f);

    public static final Vector4f W = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);

    public static Vector4f splat(float value) {
        return new Vector4f(value, value, value, value);
    }

    public Vector4f add(Vector4f other) {
        return new Vector4f(this.x + other.x, this.y + other.y, this.z + other.z, this.w + other.w);
    }

    public Vector4f add(float x, float y, float z, float w) {
        return new Vector4f(this.x + x, this.y + y, this.z + z, this.w + w);
    }

    public Vector4f add(float scalar) {
        return new Vector4f(this.x + scalar, this.y + scalar, this.z + scalar, this.w + scalar);
    }

    public Vector4f subtract(Vector4f other) {
        return new Vector4f(this.x - other.x, this.y - other.y, this.z - other.z, this.w - other.w);
    }

    public Vector4f subtract(float x, float y, float z, float w) {
        return new Vector4f(this.x - x, this.y - y, this.z - z, this.w - w);
    }

    public Vector4f subtract(float scalar) {
        return new Vector4f(this.x - scalar, this.y - scalar, this.z - scalar, this.w - scalar);
    }

    public Vector4f multiply(Vector4f other) {
        return new Vector4f(this.x * other.x, this.y * other.y, this.z * other.z, this.w * other.w);
    }

    public Vector4f multiply(float x, float y, float z, float w) {
        return new Vector4f(this.x * x, this.y * y, this.z * z, this.w * w);
    }

    public Vector4f multiply(float scalar) {
        return new Vector4f(this.x * scalar, this.y * scalar, this.z * scalar, this.w * scalar);
    }

    public Vector4f divide(Vector4f other) {
        return new Vector4f(this.x / other.x, this.y / other.y, this.z / other.z, this.w / other.w);
    }

    public Vector4f divide(float x, float y, float z, float w) {
        return new Vector4f(this.x / x, this.y / y, this.z / z, this.w / w);
    }

    public Vector4f divide(float scalar) {
        return new Vector4f(this.x / scalar, this.y / scalar, this.z / scalar, this.w / scalar);
    }

    public Vector4f negate() {
        return new Vector4f(-this.x, -this.y, -this.z, -this.w);
    }

    public float dot(Vector4f other) {
        return this.x * other.x + this.y * other.y + this.z * other.z + this.w * other.w;
    }

    public float length() {
        return (float) Math.sqrt(lengthSquared());
    }

    public float lengthSquared() {
        return dot(this);
    }

    public Vector4f normalize() {
        return divide(length());
    }

    public Vector4f fma(Vector4f scale, Vector4f offset) {
        return new Vector4f(
            (float) Math.fma(this.x, scale.x, offset.x),
            (float) Math.fma(this.y, scale.y, offset.y),
            (float) Math.fma(this.z, scale.z, offset.z),
            (float) Math.fma(this.w, scale.w, offset.w)
        );
    }
}
