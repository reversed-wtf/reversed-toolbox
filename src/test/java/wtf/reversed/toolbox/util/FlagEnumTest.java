package wtf.reversed.toolbox.util;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.*;

class FlagEnumTest {
    @Test
    void testFromValue() {
        assertThat(FlagEnum.fromValue(TestFlagEnum.class, 0)).isEqualTo(EnumSet.noneOf(TestFlagEnum.class));
        assertThat(FlagEnum.fromValue(TestFlagEnum.class, 0x1)).isEqualTo(EnumSet.of(TestFlagEnum.READ));
        assertThat(FlagEnum.fromValue(TestFlagEnum.class, 0x3)).isEqualTo(EnumSet.of(TestFlagEnum.READ, TestFlagEnum.WRITE));
        assertThat(FlagEnum.fromValue(TestFlagEnum.class, 0x7)).isEqualTo(EnumSet.of(TestFlagEnum.READ, TestFlagEnum.WRITE, TestFlagEnum.EXECUTE));
    }

    @Test
    void testFromValueThrowsOnUnknownBits() {
        assertThatIllegalArgumentException().isThrownBy(() -> FlagEnum.fromValue(TestFlagEnum.class, 0x8));
        assertThatIllegalArgumentException().isThrownBy(() -> FlagEnum.fromValue(TestFlagEnum.class, 0x9));
    }

    @Test
    void testToValue() {
        assertThat(FlagEnum.toValue(EnumSet.noneOf(TestFlagEnum.class))).isEqualTo(0);
        assertThat(FlagEnum.toValue(EnumSet.of(TestFlagEnum.READ))).isEqualTo(0x1);
        assertThat(FlagEnum.toValue(EnumSet.of(TestFlagEnum.READ, TestFlagEnum.WRITE))).isEqualTo(0x3);
        assertThat(FlagEnum.toValue(EnumSet.of(TestFlagEnum.READ, TestFlagEnum.WRITE, TestFlagEnum.EXECUTE))).isEqualTo(0x7);
    }

    @Test
    void testValue() {
        assertThat(TestFlagEnum.READ.value()).isEqualTo(1);
        assertThat(TestFlagEnum.WRITE.value()).isEqualTo(2);
        assertThat(TestFlagEnum.EXECUTE.value()).isEqualTo(4);
    }

    @Test
    void testZeroValue() {
        assertThatIllegalStateException().isThrownBy(() -> FlagEnum.fromValue(ZeroFlagEnum.class, 0));
    }

    @Test
    void testOverlappingBits() {
        assertThatIllegalStateException().isThrownBy(() -> FlagEnum.fromValue(OverlappingFlagEnum.class, 0x3));
    }

    enum TestFlagEnum implements FlagEnum {
        READ(0x1),
        WRITE(0x2),
        EXECUTE(0x4);

        private final int value;

        TestFlagEnum(int value) {
            this.value = value;
        }

        @Override
        public long value() {
            return value;
        }
    }

    enum OverlappingFlagEnum implements FlagEnum {
        A(0x1),
        B(0x2),
        AB(0x3);

        private final int value;

        OverlappingFlagEnum(int value) {
            this.value = value;
        }

        @Override
        public long value() {
            return value;
        }
    }

    enum ZeroFlagEnum implements FlagEnum {
        ZERO(0);

        private final int value;

        ZeroFlagEnum(int value) {
            this.value = value;
        }

        @Override
        public long value() {
            return value;
        }
    }
}
