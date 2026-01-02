package wtf.reversed.toolbox.math;

public enum Angle {
    DEGREES,
    RADIANS;

    private static final float DEGREES_TO_RADIANS = (float) (Math.PI / 180.0);
    private static final float RADIANS_TO_DEGREES = (float) (180.0 / Math.PI);

    public float toDegrees(float angle) {
        return switch (this) {
            case DEGREES -> angle;
            case RADIANS -> angle * RADIANS_TO_DEGREES;
        };
    }

    public float toRadians(float angle) {
        return switch (this) {
            case DEGREES -> angle * DEGREES_TO_RADIANS;
            case RADIANS -> angle;
        };
    }
}
