package wtf.reversed.toolbox.math;

import nl.jqno.equalsverifier.*;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

class Vector3Test {
    @Test
    void testEqualsAndHashCode() {
        EqualsVerifier
            .forClass(Vector3.class)
            .verify();
    }

    @Test
    void testConstants() {
        assertThat(Vector3.ZERO).isEqualTo(new Vector3(0, 0, 0));
        assertThat(Vector3.ONE).isEqualTo(new Vector3(1, 1, 1));
        assertThat(Vector3.X).isEqualTo(new Vector3(1, 0, 0));
        assertThat(Vector3.Y).isEqualTo(new Vector3(0, 1, 0));
        assertThat(Vector3.Z).isEqualTo(new Vector3(0, 0, 1));
    }

    @Test
    void testConstructors() {
        assertThat(new Vector3(5)).isEqualTo(new Vector3(5, 5, 5));
        assertThat(new Vector3(new Vector2(1, 2), 3)).isEqualTo(new Vector3(1, 2, 3));
    }

    @Test
    void testComponentAccess() {
        var v = new Vector3(1, 2, 3);
        assertThat(v.get(0)).isEqualTo(1);
        assertThat(v.get(1)).isEqualTo(2);
        assertThat(v.get(2)).isEqualTo(3);
        assertThatThrownBy(() -> v.get(3)).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    void testLinearOperations() {
        var v1 = new Vector3(1, 2, 3);
        var v2 = new Vector3(5, 6, 7);

        assertThat(v1.add(v2)).isEqualTo(new Vector3(6, 8, 10));
        assertThat(v1.subtract(v2)).isEqualTo(new Vector3(-4, -4, -4));
        assertThat(v1.multiply(2)).isEqualTo(new Vector3(2, 4, 6));
        assertThat(v1.divide(2)).isEqualTo(new Vector3(0.5f, 1.0f, 1.5f));
        assertThat(v1.negate()).isEqualTo(new Vector3(-1, -2, -3));
    }

    @Test
    void testVectorOperations() {
        var v1 = new Vector3(1, 2, 3);
        var v2 = new Vector3(5, 6, 7);
        assertThat(v1.dot(v2)).isEqualTo(38);
        assertThat(v1.lengthSquared()).isEqualTo(14);
        assertThat(v1.length()).isEqualTo(FloatMath.sqrt(14));
        assertThat(v1.normalize()).isEqualTo(new Vector3(1, 2, 3).divide(FloatMath.sqrt(14)));
    }

    @Test
    void testConversions() {
        var v = new Vector3(1, 2, 3);
        assertThat(v.xy()).isEqualTo(new Vector2(1, 2));
    }

    @Test
    void testTransform3() {
        var v = new Vector3(1, 2, 3);
        var scale = Matrix3.fromScale(4, 5, 6);
        var result = v.transform(scale);
        assertThat(result).isEqualTo(new Vector3(4, 10, 18));
    }

    @Test
    void testTransform4() {
        var v = new Vector3(1, 2, 3);
        var translation = Matrix4.fromTranslation(4, 5, 6);
        var result = v.transform(translation);
        assertThat(result).isEqualTo(new Vector3(5, 7, 9));
    }
}
