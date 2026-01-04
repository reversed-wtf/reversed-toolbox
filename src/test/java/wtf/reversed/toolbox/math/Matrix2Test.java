package wtf.reversed.toolbox.math;

import nl.jqno.equalsverifier.*;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

public class Matrix2Test {
    @Test
    void testEqualsAndHashCode() {
        EqualsVerifier.forClass(Matrix2.class).verify();
    }

    @Test
    void testInverse() {
        Matrix2 m1 = new Matrix2(
            1, 2,
            3, 4
        );

        Matrix2 m2 = m1
            .multiply(m1.inverse())
            .subtract(Matrix2.IDENTITY);

        float sum = 0;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                sum += m2.get(i, j);
            }
        }
        assertThat(sum).isEqualTo(0f, offset(Linear.EPSILON));
    }
}
