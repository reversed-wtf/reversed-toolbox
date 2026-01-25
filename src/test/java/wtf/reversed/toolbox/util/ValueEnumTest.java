package wtf.reversed.toolbox.util;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

class ValueEnumTest {
    @Test
    void testFromValue() {
        assertThat(ValueEnum.fromValue(IntegerValueEnum.class, 1)).isEqualTo(IntegerValueEnum.ONE);
        assertThat(ValueEnum.fromValue(IntegerValueEnum.class, 2)).isEqualTo(IntegerValueEnum.TWO);
        assertThat(ValueEnum.fromValue(IntegerValueEnum.class, 10)).isEqualTo(IntegerValueEnum.TEN);
        assertThat(ValueEnum.fromValue(StringValueEnum.class, "alpha")).isEqualTo(StringValueEnum.A);
    }

    @Test
    void testFromValueThrowsOnUnknown() {
        assertThatExceptionOfType(NoSuchElementException.class)
            .isThrownBy(() -> ValueEnum.fromValue(IntegerValueEnum.class, 3));
        assertThatExceptionOfType(NoSuchElementException.class)
            .isThrownBy(() -> ValueEnum.fromValue(StringValueEnum.class, "gamma"));
    }

    @Test
    void testFromValueOptional() {
        assertThat(ValueEnum.fromValueOptional(IntegerValueEnum.class, 1)).hasValue(IntegerValueEnum.ONE);
        assertThat(ValueEnum.fromValueOptional(IntegerValueEnum.class, 3)).isEmpty();

        assertThat(ValueEnum.fromValueOptional(StringValueEnum.class, "beta")).hasValue(StringValueEnum.B);
        assertThat(ValueEnum.fromValueOptional(StringValueEnum.class, "unknown")).isEmpty();
    }

    @Test
    void testValue() {
        assertThat(IntegerValueEnum.ONE.value()).isEqualTo(1);
        assertThat(StringValueEnum.A.value()).isEqualTo("alpha");
    }

    enum IntegerValueEnum implements ValueEnum<Integer> {
        ONE(1),
        TWO(2),
        TEN(10);

        private final int value;

        IntegerValueEnum(int value) {
            this.value = value;
        }

        @Override
        public Integer value() {
            return value;
        }
    }

    enum StringValueEnum implements ValueEnum<String> {
        A("alpha"),
        B("beta");

        private final String value;

        StringValueEnum(String value) {
            this.value = value;
        }

        @Override
        public String value() {
            return value;
        }
    }
}
