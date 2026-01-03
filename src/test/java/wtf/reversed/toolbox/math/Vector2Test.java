package wtf.reversed.toolbox.math;

import nl.jqno.equalsverifier.*;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

class Vector2Test {
    @Test
    void testEqualsAndHashCode() {
        EqualsVerifier
            .forClass(Vector2.class)
            .verify();
    }

    @Test
    void testConstants() {
        assertThat(Vector2.ZERO).isEqualTo(new Vector2(0, 0));
        assertThat(Vector2.ONE).isEqualTo(new Vector2(1, 1));
        assertThat(Vector2.X).isEqualTo(new Vector2(1, 0));
        assertThat(Vector2.Y).isEqualTo(new Vector2(0, 1));
    }

    @Test
    void testConstructors() {
        assertThat(new Vector2(5)).isEqualTo(new Vector2(5, 5));
    }

    @Test
    void testComponentAccess() {
        var v = new Vector2(1, 2);
        assertThat(v.get(0)).isEqualTo(1);
        assertThat(v.get(1)).isEqualTo(2);
        assertThatThrownBy(() -> v.get(2)).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    void testLinearOperations() {
        var v1 = new Vector2(1, 2);
        var v2 = new Vector2(5, 6);

        assertThat(v1.add(v2)).isEqualTo(new Vector2(6, 8));
        assertThat(v1.subtract(v2)).isEqualTo(new Vector2(-4, -4));
        assertThat(v1.multiply(2)).isEqualTo(new Vector2(2, 4));
        assertThat(v1.divide(2)).isEqualTo(new Vector2(0.5f, 1.0f));
        assertThat(v1.negate()).isEqualTo(new Vector2(-1, -2));
    }

    @Test
    void testVectorOperations() {
        var v1 = new Vector2(1, 2);
        var v2 = new Vector2(5, 6);
        assertThat(v1.dot(v2)).isEqualTo(17);
        assertThat(v1.lengthSquared()).isEqualTo(5);
        assertThat(v1.length()).isEqualTo(FloatMath.sqrt(5));
        assertThat(v1.normalize()).isEqualTo(new Vector2(1, 2).divide(FloatMath.sqrt(5)));
    }

    @Test
    void testTransform3() {
        var v = new Vector2(1, 2);
        var scale = Matrix2.fromScale(4, 5);
        var result = v.transform(scale);
        assertThat(result).isEqualTo(new Vector2(4, 10));
    }

    @Test
    void testTransform4() {
        var v = new Vector2(1, 2);
        var translation = Matrix3.fromScale(4, 5, 6);
        var result = v.transform(translation);
        assertThat(result).isEqualTo(new Vector2(4, 10));
    }
}
