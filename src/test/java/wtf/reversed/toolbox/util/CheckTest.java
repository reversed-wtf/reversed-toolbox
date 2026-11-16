package wtf.reversed.toolbox.util;

import org.junit.jupiter.api.*;

import java.util.*;

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
                .isThrownBy(() -> Check.index(-1, 10))
                .withMessage("index must not be negative, but was -1");
        }

        @Test
        void throwsWhenIndexEqualsSize() {
            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> Check.index(10, 10))
                .withMessage("index must be less than 10, but was 10");
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
                .withMessage("'pos' must not be negative, but was -1");
        }

        @Test
        void throwsWhenExceedsLimit() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> Check.position(101L, 100L, "pos"))
                .withMessage("'pos' must be at most 100, but was 101");
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
        void acceptsEmptyRangeAtEnd() {
            assertThat(Check.fromToIndex(10, 10, 10)).isEqualTo(10);
        }

        @Test
        void throwsWhenFromIndexNegative() {
            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> Check.fromToIndex(-1, 2, 10))
                .withMessage("fromIndex must not be negative, but was -1");
        }

        @Test
        void throwsWhenFromIndexGreaterThanToIndex() {
            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> Check.fromToIndex(8, 2, 10))
                .withMessage("fromIndex must be at most toIndex (2), but was 8");
        }

        @Test
        void throwsWhenToIndexGreaterThanSize() {
            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> Check.fromToIndex(2, 11, 10))
                .withMessage("toIndex must be at most 10, but was 11");
        }

        @Test
        void throwsForLongOverload() {
            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> Check.fromToIndex(2L, 11L, 10L))
                .withMessage("toIndex must be at most 10, but was 11");
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
        void acceptsEmptyRangeAtEnd() {
            assertThat(Check.fromIndexSize(10, 0, 10)).isEqualTo(10);
        }

        @Test
        void throwsWhenFromIndexNegative() {
            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> Check.fromIndexSize(-1, 5, 10))
                .withMessage("fromIndex must not be negative, but was -1");
        }

        @Test
        void throwsWhenSizeNegative() {
            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> Check.fromIndexSize(2, -3, 10))
                .withMessage("size must not be negative, but was -3");
        }

        @Test
        void throwsWhenRangeExceedsLength() {
            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> Check.fromIndexSize(8, 5, 10))
                .withMessage("fromIndex + size must be at most 10, but was 13");
        }

        @Test
        void throwsWhenSumOverflowsInt() {
            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> Check.fromIndexSize(Integer.MAX_VALUE, 1, 10))
                .withMessage("fromIndex + size must be at most 10, but was 2147483648");
        }

        @Test
        void throwsWhenSumOverflowsLong() {
            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> Check.fromIndexSize(Long.MAX_VALUE, 1L, 10L))
                .withMessage("fromIndex + size must be at most 10, but was 9223372036854775808");
        }
    }

    @Nested
    class NonEmpty {
        @Test
        void returnsArrayWhenNonEmpty() {
            String[] array = {"a"};
            assertThat(Check.nonEmpty(array, "param")).isSameAs(array);
        }

        @Test
        void returnsCollectionWhenNonEmpty() {
            List<String> list = List.of("a");
            assertThat(Check.nonEmpty(list, "param")).isSameAs(list);
        }

        @Test
        void returnsMapWhenNonEmpty() {
            Map<String, String> map = Map.of("a", "b");
            assertThat(Check.nonEmpty(map, "param")).isSameAs(map);
        }

        @Test
        void throwsWhenArrayEmpty() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> Check.nonEmpty(new String[0], "param"))
                .withMessage("'param' must not be empty");
        }

        @Test
        void throwsWhenCollectionEmpty() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> Check.nonEmpty(List.of(), "param"))
                .withMessage("'param' must not be empty");
        }

        @Test
        void throwsWhenMapEmpty() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> Check.nonEmpty(Map.of(), "param"))
                .withMessage("'param' must not be empty");
        }

        @Test
        void throwsWhenArrayNull() {
            assertThatNullPointerException()
                .isThrownBy(() -> Check.nonEmpty((String[]) null, "param"))
                .withMessage("'param' must not be null");
        }

        @Test
        void throwsWhenCollectionNull() {
            assertThatNullPointerException()
                .isThrownBy(() -> Check.nonEmpty((List<String>) null, "param"))
                .withMessage("'param' must not be null");
        }

        @Test
        void throwsWhenMapNull() {
            assertThatNullPointerException()
                .isThrownBy(() -> Check.nonEmpty((Map<String, String>) null, "param"))
                .withMessage("'param' must not be null");
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
                .withMessage("'param' must be greater than 0, but was 0");
        }

        @Test
        void throwsWhenNegative() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> Check.positive(-1, "param"))
                .withMessage("'param' must be greater than 0, but was -1");
        }
    }

    @Nested
    class NonNegative {
        @Test
        void returnsValueWhenPositiveOrZero() {
            assertThat(Check.nonNegative(0, "param")).isEqualTo(0);
            assertThat(Check.nonNegative(5, "param")).isEqualTo(5);
            assertThat(Check.nonNegative(0L, "param")).isEqualTo(0L);
        }

        @Test
        void throwsWhenNegative() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> Check.nonNegative(-1, "param"))
                .withMessage("'param' must not be negative, but was -1");
        }
    }

    @Nested
    class MultipleOf {
        @Test
        void doesNotThrowWhenMultiple() {
            assertThatNoException().isThrownBy(() -> Check.multipleOf(12, 4, "param"));
            assertThatNoException().isThrownBy(() -> Check.multipleOf(0, 4, "param"));
            assertThatNoException().isThrownBy(() -> Check.multipleOf(-12, 4, "param"));
            assertThatNoException().isThrownBy(() -> Check.multipleOf(Long.MAX_VALUE - 7, 8L, "param"));
        }

        @Test
        void throwsWhenNotMultiple() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> Check.multipleOf(6, 4, "param"))
                .withMessage("'param' must be a multiple of 4, but was 6");
        }

        @Test
        void throwsWhenNegativeNotMultiple() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> Check.multipleOf(-6, 4, "param"))
                .withMessage("'param' must be a multiple of 4, but was -6");
        }
    }
}
