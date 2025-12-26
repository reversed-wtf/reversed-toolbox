package wtf.reversed.toolbox.util;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

class CheckTest {
    @Nested
    class NonNull {
        @Test
        void returnsObjectWhenNonNull() {
            String obj = "test";
            assertThat(Check.nonNull(obj, "param")).isSameAs(obj);
        }

        @Test
        void throwsNullPointerExceptionWhenNull() {
            assertThatNullPointerException()
                .isThrownBy(() -> Check.nonNull(null, "param"))
                .withMessage("'param' must not be null");
        }
    }

    @Nested
    class Argument {
        @Test
        void doesNotThrowWhenConditionTrue() {
            assertThatNoException().isThrownBy(() -> Check.argument(true, "error"));
        }

        @Test
        void throwsIllegalArgumentExceptionWhenConditionFalse() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> Check.argument(false, "error"))
                .withMessage("error");
        }

        @Test
        void throwsWithSupplierMessage() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> Check.argument(false, () -> "supplied error"))
                .withMessage("supplied error");
        }

        @Test
        void doesNotEvaluateSupplierWhenConditionTrue() {
            assertThatNoException().isThrownBy(() ->
                Check.argument(true, () -> {
                    throw new RuntimeException();
                }));
        }
    }

    @Nested
    class State {
        @Test
        void doesNotThrowWhenConditionTrue() {
            assertThatNoException().isThrownBy(() -> Check.state(true, "error"));
        }

        @Test
        void throwsIllegalStateExceptionWhenConditionFalse() {
            assertThatIllegalStateException()
                .isThrownBy(() -> Check.state(false, "error"))
                .withMessage("error");
        }

        @Test
        void throwsWithSupplierMessage() {
            assertThatIllegalStateException()
                .isThrownBy(() -> Check.state(false, () -> "supplied error"))
                .withMessage("supplied error");
        }
    }

    @Nested
    class Index {
        @Test
        void returnsIndexWhenValid() {
            assertThat(Check.index(5, 10)).isEqualTo(5);
            assertThat(Check.index(5L, 10L)).isEqualTo(5L);
        }

        @Test
        void throwsWhenIndexNegative() {
            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> Check.index(-1, 10));
        }

        @Test
        void throwsWhenIndexEqualsSize() {
            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> Check.index(10, 10));
        }
    }

    @Nested
    class Position {
        @Test
        void returnsPositionWhenValid() {
            assertThat(Check.position(0L, 100L, "pos")).isEqualTo(0L);
            assertThat(Check.position(50L, 100L, "pos")).isEqualTo(50L);
            assertThat(Check.position(100L, 100L, "pos")).isEqualTo(100L);
        }

        @Test
        void throwsWhenNegative() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> Check.position(-1L, 100L, "pos"))
                .withMessageContaining("must be a valid position");
        }

        @Test
        void throwsWhenExceedsLimit() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> Check.position(101L, 100L, "pos"))
                .withMessageContaining("must be a valid position");
        }
    }

    @Nested
    class FromToIndex {
        @Test
        void returnsFromIndexWhenValid() {
            assertThat(Check.fromToIndex(2, 8, 10)).isEqualTo(2);
            assertThat(Check.fromToIndex(2L, 8L, 10L)).isEqualTo(2L);
        }

        @Test
        void throwsWhenFromIndexGreaterThanToIndex() {
            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> Check.fromToIndex(8, 2, 10));
        }

        @Test
        void throwsWhenToIndexGreaterThanSize() {
            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> Check.fromToIndex(2, 11, 10));
        }
    }

    @Nested
    class FromIndexSize {
        @Test
        void returnsFromIndexWhenValid() {
            assertThat(Check.fromIndexSize(2, 5, 10)).isEqualTo(2);
            assertThat(Check.fromIndexSize(2L, 5L, 10L)).isEqualTo(2L);
        }

        @Test
        void throwsWhenRangeExceedsLength() {
            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> Check.fromIndexSize(8, 5, 10));
        }
    }

    @Nested
    class Positive {
        @Test
        void returnsValueWhenPositive() {
            assertThat(Check.positive(5, "param")).isEqualTo(5);
            assertThat(Check.positive(5L, "param")).isEqualTo(5L);
        }

        @Test
        void throwsWhenZero() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> Check.positive(0, "param"))
                .withMessage("'param' must be greater than 0");
        }

        @Test
        void throwsWhenNegative() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> Check.positive(-1, "param"))
                .withMessage("'param' must be greater than 0");
        }
    }

    @Nested
    class PositiveOrZero {
        @Test
        void returnsValueWhenPositiveOrZero() {
            assertThat(Check.positiveOrZero(0, "param")).isEqualTo(0);
            assertThat(Check.positiveOrZero(5, "param")).isEqualTo(5);
            assertThat(Check.positiveOrZero(0L, "param")).isEqualTo(0L);
        }

        @Test
        void throwsWhenNegative() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> Check.positiveOrZero(-1, "param"))
                .withMessage("'param' must be greater than or equal to 0");
        }
    }
}
