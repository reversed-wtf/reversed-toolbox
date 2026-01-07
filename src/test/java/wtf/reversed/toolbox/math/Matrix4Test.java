package wtf.reversed.toolbox.math;

import nl.jqno.equalsverifier.*;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

public class Matrix4Test {
    @Test
    void testEqualsAndHashCode() {
        EqualsVerifier.forClass(Matrix4.class).verify();
    }

    @Test
    void testInverse() {
        Matrix4 m1 = new Matrix4(
            1, 1, 4, 5,
            3, 3, 3, 2,
            5, 1, 9, 0,
            9, 7, 7, 9
        );

        Matrix4 m2 = m1
            .multiply(m1.inverse())
            .subtract(Matrix4.IDENTITY);

        float sum = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                sum += m2.get(i, j);
            }
        }
        assertThat(sum).isEqualTo(0f, offset(Linear.EPSILON));
    }

    @Test
    void testDecompose() {
        var translation = new Vector3(10, 20, 30);
        var rotation = Quaternion.fromAxisAngle(Vector3.X, 90, Angle.DEGREES);
        var scale = new Vector3(4, 5, 6);

        Matrix4 m = Matrix4.fromTranslation(translation)
            .multiply(Matrix4.fromRotation(rotation))
            .multiply(Matrix4.fromScale(scale));

        var decomposed = m.decompose();

        assertThat(decomposed.translation().subtract(translation).lengthSquared()).isLessThan(Linear.EPSILON);
        assertThat(decomposed.rotation().subtract(rotation).lengthSquared()).isLessThan(Linear.EPSILON);
        assertThat(decomposed.scale().subtract(scale).lengthSquared()).isLessThan(Linear.EPSILON);

        assertThat(m.toTranslation().subtract(translation).lengthSquared()).isLessThan(Linear.EPSILON);
        assertThat(m.toRotation().subtract(rotation).lengthSquared()).isLessThan(Linear.EPSILON);
        assertThat(m.toScale().subtract(scale).lengthSquared()).isLessThan(Linear.EPSILON);
    }
}
