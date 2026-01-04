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
     * Computes the arcsine of a float value.
     *
     * @param a the float value
     * @return the arcsine of the value
     */
    public static float asin(float a) {
        return (float) Math.asin(a);
    }

    /**
     * Computes the arccosine of a float value.
     *
     * @param a the float value
     * @return the arccosine of the value
     */
    public static float acos(float a) {
        return (float) Math.acos(a);
    }

    /**
     * Computes the arctangent of a float value.
     *
     * @param a the float value
     * @return the arctangent of the value
     */
    public static float atan(float a) {
        return (float) Math.atan(a);
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
     * Computes the reciprocal square root of a float value.
     *
     * @param a the float value
     * @return the reciprocal square root of the value
     */
    public static float rsqrt(float a) {
        return 1.0f / sqrt(a);
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


    /**
     * Clamps a float value to the range [0.0, 1.0].
     *
     * @param f the value to clamp
     * @return the clamped value between 0.0 and 1.0
     */
    public static float clamp01(float f) {
        return Math.clamp(f, 0.0f, 1.0f);
    }

    /**
     * Clamps a float value to the range [-1.0, 1.0].
     *
     * @param f the value to clamp
     * @return the clamped value between -1.0 and 1.0
     */
    public static float clamp11(float f) {
        return Math.clamp(f, -1.0f, 1.0f);
    }

    /**
     * Unpacks an unsigned normalized 8-bit value to a float [0.0, 1.0].
     *
     * @param value the unsigned byte value
     * @return a float between 0.0 and 1.0
     */
    public static float unpackUNorm8(byte value) {
        return Byte.toUnsignedInt(value) * (1.0f / 255.0f);
    }

    /**
     * Unpacks a signed normalized 8-bit value to a float [-1.0, 1.0].
     *
     * @param value the signed byte value
     * @return a float between -1.0 and 1.0
     */
    public static float unpackSNorm8(byte value) {
        return Math.max(-127, value) * (1.0f / 127.0f);
    }

    /**
     * Unpacks an unsigned normalized 16-bit value to a float [0.0, 1.0].
     *
     * @param value the unsigned short value
     * @return a float between 0.0 and 1.0
     */
    public static float unpackUNorm16(short value) {
        return Short.toUnsignedInt(value) * (1.0f / 65535.0f);
    }

    /**
     * Unpacks a signed normalized 16-bit value to a float [-1.0, 1.0].
     *
     * @param value the signed short value
     * @return a float between -1.0 and 1.0
     */
    public static float unpackSNorm16(short value) {
        return Math.max(-32767, value) * (1.0f / 32767.0f);
    }

    /**
     * Packs a float [0.0, 1.0] to an unsigned normalized 8-bit value.
     *
     * @param value the float value to pack
     * @return the packed byte value
     */
    public static byte packUNorm8(float value) {
        return (byte) Math.fma(clamp01(value), 255.0f, 0.5f);
    }

    /**
     * Packs a float [-1.0, 1.0] to a signed normalized 8-bit value.
     *
     * @param value the float value to pack
     * @return the packed byte value
     */
    public static byte packSNorm8(float value) {
        return (byte) Math.round(clamp11(value) * 127.0f);
    }

    /**
     * Packs a float [0.0, 1.0] to an unsigned normalized 16-bit value.
     *
     * @param value the float value to pack
     * @return the packed short value
     */
    public static short packUNorm16(float value) {
        return (short) Math.fma(clamp01(value), 65535.0f, 0.5f);
    }

    /**
     * Packs a float [-1.0, 1.0] to a signed normalized 16-bit value.
     *
     * @param value the float value to pack
     * @return the packed short value
     */
    public static short packSNorm16(float value) {
        return (short) Math.round(clamp11(value) * 32767.0f);
    }
}
