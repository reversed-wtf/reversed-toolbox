package wtf.reversed.toolbox.math;

/**
 * Utility class for floating-point math operations.
 */
public final class FloatMath {
    /**
     * The value of {@code PI}.
     */
    public static final float PI = (float) Math.PI;
    /**
     * The value of {@code 2 * PI}.
     */
    public static final float TAU = (float) (2.0 * Math.PI);
    /**
     * The value of {@code PI / 2}.
     */
    public static final float FRAC_PI_2 = (float) (Math.PI / 2.0);

    private FloatMath() {
    }


    /**
     * Computes the sine of a float value.
     *
     * @param a the float value
     * @return the sine of the value
     */
    public static float sin(float a) {
        return (float) Math.sin(a);
    }

    /**
     * Computes the cosine of a float value.
     *
     * @param a the float value
     * @return the cosine of the value
     */
    public static float cos(float a) {
        return (float) Math.cos(a);
    }

    /**
     * Computes the tangent of a float value.
     *
     * @param a the float value
     * @return the tangent of the value
     */
    public static float tan(float a) {
        return (float) Math.tan(a);
    }

    /**
     * Computes the square root of a float value.
     *
     * @param a the float value
     * @return the square root of the value
     */
    public static float sqrt(float a) {
        return (float) Math.sqrt(a);
    }


    /**
     * Compares two float values for equality using "folding" semantics.
     * <p>
     * Unlike standard {@code ==}, this method:
     * <ul>
     *     <li>Considers all NaN values to be equal to each other.</li>
     *     <li>Considers positive zero and negative zero to be equal.</li>
     * </ul>
     *
     * @param a the first float value
     * @param b the second float value
     * @return {@code true} if the values are considered equal, {@code false} otherwise
     */
    public static boolean equals(float a, float b) {
        return Float.isNaN(a) ? Float.isNaN(b) : a == b;
    }

    /**
     * Computes a hash code for a float value consistent with {@link #equals(float, float)}.
     * <p>
     * All NaN values are normalized to a single hash constant. Positive and negative
     * zero are also normalized to produce the same hash code.
     *
     * @param a the float value to hash
     * @return a hash code value for the float
     */
    public static int hashCode(float a) {
        return Float.isNaN(a)
            ? 0x7fc00000
            : Float.floatToRawIntBits(a + 0.0f);
    }
}
