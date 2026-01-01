package wtf.reversed.toolbox.math;

import org.assertj.core.data.*;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

class FloatMathTest {
    private static final float OTHER_NAN = Float.intBitsToFloat(0x7f800001);
    private static final Offset<Float> EPSILON = offset(1e-6f);

    @Test
    void testSin() {
        assertThat(FloatMath.sin(0.0f)).isCloseTo(0.0f, EPSILON);
        assertThat(FloatMath.sin(FloatMath.FRAC_PI_2)).isCloseTo(1.0f, EPSILON);
        assertThat(FloatMath.sin(FloatMath.PI)).isCloseTo(0.0f, EPSILON);
    }

    @Test
    void testCos() {
        assertThat(FloatMath.cos(0.0f)).isCloseTo(1.0f, EPSILON);
        assertThat(FloatMath.cos(FloatMath.FRAC_PI_2)).isCloseTo(0.0f, EPSILON);
        assertThat(FloatMath.cos(FloatMath.PI)).isCloseTo(-1.0f, EPSILON);
    }

    @Test
    void testTan() {
        assertThat(FloatMath.tan(0.0f)).isCloseTo(0.0f, EPSILON);
        assertThat(FloatMath.tan((float) (Math.PI / 4.0))).isCloseTo(1.0f, EPSILON);
    }

    @Test
    void testSqrt() {
        assertThat(FloatMath.sqrt(4.0f)).isEqualTo(2.0f);
        assertThat(FloatMath.sqrt(9.0f)).isEqualTo(3.0f);
        assertThat(FloatMath.sqrt(2.0f)).isCloseTo(1.4142135f, EPSILON);
    }

    @Test
    void testEqualsRegularNumbers() {
        assertThat(FloatMath.equals(1.0f, 1.0f)).isTrue();
        assertThat(FloatMath.equals(1.0f, 2.0f)).isFalse();
    }

    @Test
    void testEqualsZeros() {
        assertThat(FloatMath.equals(0.0f, -0.0f)).isTrue();
        assertThat(FloatMath.equals(-0.0f, 0.0f)).isTrue();
    }

    @Test
    void testEqualsNaNs() {
        assertThat(FloatMath.equals(Float.NaN, Float.NaN)).isTrue();
        assertThat(FloatMath.equals(Float.NaN, OTHER_NAN)).isTrue();
        assertThat(FloatMath.equals(OTHER_NAN, Float.NaN)).isTrue();

        assertThat(FloatMath.equals(Float.NaN, 1.0f)).isFalse();
        assertThat(FloatMath.equals(1.0f, Float.NaN)).isFalse();

        assertThat(FloatMath.equals(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)).isTrue();
        assertThat(FloatMath.equals(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY)).isFalse();
    }

    @Test
    void testHashCodeZeros() {
        assertThat(FloatMath.hashCode(0.0f))
            .isEqualTo(FloatMath.hashCode(-0.0f));
    }

    @Test
    void testHashCodeNaNs() {
        assertThat(FloatMath.hashCode(Float.NaN)).isEqualTo(0x7fc00000);
        assertThat(FloatMath.hashCode(OTHER_NAN)).isEqualTo(0x7fc00000);
    }
}
