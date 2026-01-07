package wtf.reversed.toolbox.math;

/**
 * Represents different angle measurement units.
 */
public enum Angle {
    /**
     * Represents angles in degrees.
     */
    DEGREES,
    /**
     * Represents angles in radians.
     */
    RADIANS;

    private static final float DEGREES_TO_RADIANS = (float) (Math.PI / 180.0);
    private static final float RADIANS_TO_DEGREES = (float) (180.0 / Math.PI);

    /**
     * Converts an angle to degrees.
     *
     * @param angle the angle to convert
     * @return the converted angle
     */
    public float toDegrees(float angle) {
        return switch (this) {
            case DEGREES -> angle;
            case RADIANS -> angle * RADIANS_TO_DEGREES;
        };
    }

    /**
     * Converts an angle to radians.
     *
     * @param angle the angle to convert
     * @return the converted angle
     */
    public float toRadians(float angle) {
        return switch (this) {
            case DEGREES -> angle * DEGREES_TO_RADIANS;
            case RADIANS -> angle;
        };
    }
}
