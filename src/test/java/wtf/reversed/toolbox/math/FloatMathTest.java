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


    @Test
    void testClamp01() {
        assertThat(FloatMath.clamp01(-1.0f)).isEqualTo(0.0f);
        assertThat(FloatMath.clamp01(0.0f)).isEqualTo(0.0f);
        assertThat(FloatMath.clamp01(0.5f)).isEqualTo(0.5f);
        assertThat(FloatMath.clamp01(1.0f)).isEqualTo(1.0f);
        assertThat(FloatMath.clamp01(2.0f)).isEqualTo(1.0f);
    }

    @Test
    void testClamp11() {
        assertThat(FloatMath.clamp11(-2.0f)).isEqualTo(-1.0f);
        assertThat(FloatMath.clamp11(-1.0f)).isEqualTo(-1.0f);
        assertThat(FloatMath.clamp11(0.0f)).isEqualTo(0.0f);
        assertThat(FloatMath.clamp11(1.0f)).isEqualTo(1.0f);
        assertThat(FloatMath.clamp11(2.0f)).isEqualTo(1.0f);
    }

    @Test
    void testUnpackUNorm8() {
        assertThat(FloatMath.unpackUNorm8((byte) 0)).isEqualTo(0.0f);
        assertThat(FloatMath.unpackUNorm8((byte) 127)).isCloseTo(0.498f, within(0.001f));
        assertThat(FloatMath.unpackUNorm8((byte) 255)).isCloseTo(1.0f, within(0.001f));
    }

    @Test
    void testUnpackSNorm8() {
        assertThat(FloatMath.unpackSNorm8((byte) -127)).isEqualTo(-1.0f);
        assertThat(FloatMath.unpackSNorm8((byte) 0)).isEqualTo(0.0f);
        assertThat(FloatMath.unpackSNorm8((byte) 127)).isEqualTo(1.0f);
    }

    @Test
    void testUnpackUNorm16() {
        assertThat(FloatMath.unpackUNorm16((short) 0)).isEqualTo(0.0f);
        assertThat(FloatMath.unpackUNorm16((short) 32767)).isCloseTo(0.5f, within(0.001f));
        assertThat(FloatMath.unpackUNorm16((short) 65535)).isEqualTo(1.0f, within(0.001f));
    }

    @Test
    void testUnpackSNorm16() {
        assertThat(FloatMath.unpackSNorm16((short) -32767)).isEqualTo(-1.0f);
        assertThat(FloatMath.unpackSNorm16((short) 0)).isEqualTo(0.0f);
        assertThat(FloatMath.unpackSNorm16((short) 32767)).isEqualTo(1.0f);
    }

    @Test
    void testPackUNorm8() {
        assertThat(FloatMath.packUNorm8(0.0f)).isEqualTo((byte) 0);
        assertThat(FloatMath.packUNorm8(0.5f)).isEqualTo((byte) -128);
        assertThat(FloatMath.packUNorm8(1.0f)).isEqualTo((byte) 255);
        assertThat(FloatMath.packUNorm8(-0.5f)).isEqualTo((byte) 0);
        assertThat(FloatMath.packUNorm8(1.5f)).isEqualTo((byte) 255);
    }

    @Test
    void testPackSNorm8() {
        assertThat(FloatMath.packSNorm8(-1.0f)).isEqualTo((byte) -127);
        assertThat(FloatMath.packSNorm8(0.0f)).isEqualTo((byte) 0);
        assertThat(FloatMath.packSNorm8(1.0f)).isEqualTo((byte) 127);
    }

    @Test
    void testPackUNorm16() {
        assertThat(FloatMath.packUNorm16(0.0f)).isEqualTo((short) 0);
        assertThat(FloatMath.packUNorm16(0.5f)).isEqualTo((short) -32768);
        assertThat(FloatMath.packUNorm16(1.0f)).isEqualTo((short) 65535);
    }

    @Test
    void testPackSNorm16() {
        assertThat(FloatMath.packSNorm16(-1.0f)).isEqualTo((short) -32767);
        assertThat(FloatMath.packSNorm16(0.0f)).isEqualTo((short) 0);
        assertThat(FloatMath.packSNorm16(1.0f)).isEqualTo((short) 32767);
    }

    @Test
    void testPackAndUnpack() {
        assertThat(FloatMath.unpackUNorm8(FloatMath.packUNorm8(0.5f))).isCloseTo(0.5f, within(0.01f));
        assertThat(FloatMath.unpackSNorm8(FloatMath.packSNorm8(-0.5f))).isCloseTo(-0.5f, within(0.01f));
        assertThat(FloatMath.unpackUNorm16(FloatMath.packUNorm16(0.75f))).isCloseTo(0.75f, within(0.001f));
        assertThat(FloatMath.unpackSNorm16(FloatMath.packSNorm16(0.25f))).isCloseTo(0.25f, within(0.001f));
    }
}
