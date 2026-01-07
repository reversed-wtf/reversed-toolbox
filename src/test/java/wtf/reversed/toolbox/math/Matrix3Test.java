package wtf.reversed.toolbox.math;

import nl.jqno.equalsverifier.*;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

public class Matrix3Test {
    @Test
    void testEqualsAndHashCode() {
        EqualsVerifier.forClass(Matrix3.class).verify();
    }

    @Test
    void testInverse() {
        Matrix3 m1 = new Matrix3(
            1, 1, 4,
            3, 3, 3,
            5, 1, 9
        );

        Matrix3 m2 = m1
            .multiply(m1.inverse())
            .subtract(Matrix3.IDENTITY);

        float sum = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                sum += m2.get(i, j);
            }
        }
        assertThat(sum).isEqualTo(0f, offset(Linear.EPSILON));
    }
}
