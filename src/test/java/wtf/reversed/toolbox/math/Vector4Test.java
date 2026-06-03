package wtf.reversed.toolbox.math;

import nl.jqno.equalsverifier.*;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

class Vector4Test {
    @Test
    void testEqualsAndHashCode() {
        EqualsVerifier
            .forClass(Vector4.class)
            .verify();
    }

    @Test
    void testConstants() {
        assertThat(Vector4.BYTES).isEqualTo(16);
        assertThat(Vector4.ZERO).isEqualTo(new Vector4(0, 0, 0, 0));
        assertThat(Vector4.ONE).isEqualTo(new Vector4(1, 1, 1, 1));
        assertThat(Vector4.X).isEqualTo(new Vector4(1, 0, 0, 0));
        assertThat(Vector4.Y).isEqualTo(new Vector4(0, 1, 0, 0));
        assertThat(Vector4.Z).isEqualTo(new Vector4(0, 0, 1, 0));
        assertThat(Vector4.W).isEqualTo(new Vector4(0, 0, 0, 1));
    }

    @Test
    void testConstructors() {
        assertThat(new Vector4(5)).isEqualTo(new Vector4(5, 5, 5, 5));
        assertThat(new Vector4(new Vector2(1, 2), 3, 4)).isEqualTo(new Vector4(1, 2, 3, 4));
        assertThat(new Vector4(new Vector3(1, 2, 3), 4)).isEqualTo(new Vector4(1, 2, 3, 4));
    }

    @Test
    void testComponentAccess() {
        var v = new Vector4(1, 2, 3, 4);
        assertThat(v.get(0)).isEqualTo(1);
        assertThat(v.get(1)).isEqualTo(2);
        assertThat(v.get(2)).isEqualTo(3);
        assertThat(v.get(3)).isEqualTo(4);
        assertThatThrownBy(() -> v.get(4)).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    void testLinearOperations() {
        var v1 = new Vector4(1, 2, 3, 4);
        var v2 = new Vector4(5, 6, 7, 8);

        assertThat(v1.add(v2)).isEqualTo(new Vector4(6, 8, 10, 12));
        assertThat(v1.subtract(v2)).isEqualTo(new Vector4(-4, -4, -4, -4));
        assertThat(v1.multiply(2)).isEqualTo(new Vector4(2, 4, 6, 8));
        assertThat(v1.divide(2)).isEqualTo(new Vector4(0.5f, 1.0f, 1.5f, 2.0f));
        assertThat(v1.negate()).isEqualTo(new Vector4(-1, -2, -3, -4));
    }

    @Test
    void testVectorOperations() {
        var v1 = new Vector4(1, 2, 3, 4);
        var v2 = new Vector4(5, 6, 7, 8);
        assertThat(v1.dot(v2)).isEqualTo(70);
        assertThat(v1.lengthSquared()).isEqualTo(30);
        assertThat(v1.length()).isEqualTo(FloatMath.sqrt(30));
        assertThat(v1.normalize()).isEqualTo(new Vector4(1, 2, 3, 4).divide(FloatMath.sqrt(30)));
    }

    @Test
    void testConversions() {
        var v = new Vector4(1, 2, 3, 4);
        assertThat(v.xy()).isEqualTo(new Vector2(1, 2));
        assertThat(v.xyz()).isEqualTo(new Vector3(1, 2, 3));
    }

    @Test
    void testTransform() {
        var v = new Vector4(1, 2, 3, 1);
        var translation = Matrix4.fromTranslation(4, 5, 6);
        var result = v.transform(translation);
        assertThat(result).isEqualTo(new Vector4(5, 7, 9, 1));
    }

    @Test
    void testDistance() {
        var v1 = new Vector4(1, 2, 3, 4);
        var v2 = new Vector4(5, 6, 7, 8);
        assertThat(v1.distance(v2)).isEqualTo(FloatMath.sqrt(64));
    }

    @Test
    void testFma() {
        var v1 = new Vector4(1, 2, 3, 4);
        var v2 = new Vector4(5, 6, 7, 8);
        assertThat(v1.fma(7, v2)).isEqualTo(new Vector4(12, 20, 28, 36));
    }

    @Test
    void testFmaVector() {
        var v1 = new Vector4(1, +2, +3, +4);
        var v2 = new Vector4(5, +6, +7, +8);
        var v3 = new Vector4(9, 10, 11, 12);
        assertThat(v1.fma(v2, v3)).isEqualTo(new Vector4(14, 22, 32, 44));
    }

    @Test
    void testMultiply() {
        var v1 = new Vector4(1, 2, 3, 4);
        var v2 = new Vector4(5, 6, 7, 8);
        assertThat(v1.multiply(v2)).isEqualTo(new Vector4(5, 12, 21, 32));
    }
}
