package wtf.reversed.toolbox.math;

import java.lang.Math;

public record Vector2f(float x, float y) {
    public static final Vector2f ZERO = new Vector2f(0.0f, 0.0f);

    public static final Vector2f ONE = new Vector2f(1.0f, 1.0f);

    public static final Vector2f X = new Vector2f(1.0f, 0.0f);

    public static final Vector2f Y = new Vector2f(0.0f, 1.0f);

    public static Vector2f splat(float value) {
        return new Vector2f(value, value);
    }

    public Vector2f add(Vector2f other) {
        return new Vector2f(this.x + other.x, this.y + other.y);
    }

    public Vector2f add(float x, float y) {
        return new Vector2f(this.x + x, this.y + y);
    }

    public Vector2f add(float scalar) {
        return new Vector2f(this.x + scalar, this.y + scalar);
    }

    public Vector2f subtract(Vector2f other) {
        return new Vector2f(this.x - other.x, this.y - other.y);
    }

    public Vector2f subtract(float x, float y) {
        return new Vector2f(this.x - x, this.y - y);
    }

    public Vector2f subtract(float scalar) {
        return new Vector2f(this.x - scalar, this.y - scalar);
    }

    public Vector2f multiply(Vector2f other) {
        return new Vector2f(this.x * other.x, this.y * other.y);
    }

    public Vector2f multiply(float x, float y) {
        return new Vector2f(this.x * x, this.y * y);
    }

    public Vector2f multiply(float scalar) {
        return new Vector2f(this.x * scalar, this.y * scalar);
    }

    public Vector2f divide(Vector2f other) {
        return new Vector2f(this.x / other.x, this.y / other.y);
    }

    public Vector2f divide(float x, float y) {
        return new Vector2f(this.x / x, this.y / y);
    }

    public Vector2f divide(float scalar) {
        return new Vector2f(this.x / scalar, this.y / scalar);
    }

    public Vector2f negate() {
        return new Vector2f(-this.x, -this.y);
    }

    public float dot(Vector2f other) {
        return this.x * other.x + this.y * other.y;
    }

    public float length() {
        return (float) Math.sqrt(lengthSquared());
    }

    public float lengthSquared() {
        return dot(this);
    }

    public Vector2f normalize() {
        return divide(length());
    }

    public Vector2f fma(Vector2f scale, Vector2f offset) {
        return new Vector2f(
            (float) Math.fma(this.x, scale.x, offset.x),
            (float) Math.fma(this.y, scale.y, offset.y)
        );
    }
}
