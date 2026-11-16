package wtf.reversed.toolbox.util;

import java.util.*;
import java.util.function.*;

/**
 * Static utility methods for validating arguments, state, and indices.
 */
public final class Check {
    private Check() {
        throw new AssertionError();
    }

    /**
     * Checks that the specified object reference is not {@code null} and throws a customized {@link NullPointerException} if it is.
     *
     * @param obj   the object reference to check for nullity
     * @param param the parameter name to include in the exception message
     * @param <T>   the type of the reference
     * @return {@code obj} if not {@code null}
     * @throws NullPointerException if {@code obj} is {@code null}
     */
    public static <T> T nonNull(T obj, String param) {
        if (obj == null) {
            throw new NullPointerException("'" + param + "' must not be null");
        }
        return obj;
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the calling method.
     *
     * @param condition a boolean expression
     * @param message   the exception message to use if the check fails
     * @throws IllegalArgumentException if {@code condition} is false
     */
    public static void argument(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the calling method.
     *
     * @param condition       a boolean expression
     * @param messageSupplier supplier of the exception message to use if the check fails
     * @throws IllegalArgumentException if {@code condition} is false
     */
    public static void argument(boolean condition, Supplier<String> messageSupplier) {
        if (!condition) {
            throw new IllegalArgumentException(messageSupplier == null ? null : messageSupplier.get());
        }
    }

    /**
     * Ensures the truth of an expression involving the state of the calling instance.
     *
     * @param condition a boolean expression
     * @param message   the exception message to use if the check fails
     * @throws IllegalStateException if {@code condition} is false
     */
    public static void state(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    /**
     * Ensures the truth of an expression involving the state of the calling instance.
     *
     * @param condition       a boolean expression
     * @param messageSupplier supplier of the exception message to use if the check fails
     * @throws IllegalStateException if {@code condition} is false
     */
    public static void state(boolean condition, Supplier<String> messageSupplier) {
        if (!condition) {
            throw new IllegalStateException(messageSupplier == null ? null : messageSupplier.get());
        }
    }

    /**
     * Checks if the {@code index} is within the bounds of the range from {@code 0} (inclusive) to {@code size} (exclusive).
     *
     * @param index the index
     * @param size  the upper-bound (exclusive) of the range
     * @return {@code index} if it is within bounds
     * @throws IndexOutOfBoundsException if the {@code index} is out of bounds
     */
    public static int index(int index, int size) {
        if (index < 0 || index >= size) {
            throw badIndex(index, size);
        }
        return index;
    }

    /**
     * Checks if the {@code index} is within the bounds of the range from {@code 0} (inclusive) to {@code size} (exclusive).
     *
     * @param index the index
     * @param size  the upper-bound (exclusive) of the range
     * @return {@code index} if it is within bounds
     * @throws IndexOutOfBoundsException if the {@code index} is out of bounds
     */
    public static long index(long index, long size) {
        if (index < 0 || index >= size) {
            throw badIndex(index, size);
        }
        return index;
    }

    private static IndexOutOfBoundsException badIndex(long index, long size) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("index must not be negative, but was " + index);
        }
        throw new IndexOutOfBoundsException("index must be less than " + size + ", but was " + index);
    }

    /**
     * Checks that the given position is valid and within the specified limit.
     *
     * @param position the position to check
     * @param limit    the upper bound (inclusive)
     * @param param    the name of the parameter for the error message
     * @return {@code position} if it is valid
     * @throws IllegalArgumentException if {@code position} is negative or > limit
     */
    public static int position(int position, int limit, String param) {
        if (position < 0 || position > limit) {
            throw badPosition(position, limit, param);
        }
        return position;
    }

    /**
     * Checks that the given position is valid and within the specified limit.
     *
     * @param position the position to check
     * @param limit    the upper bound (inclusive)
     * @param param    the name of the parameter for the error message
     * @return {@code position} if it is valid
     * @throws IllegalArgumentException if {@code position} is negative or > limit
     */
    public static long position(long position, long limit, String param) {
        if (position < 0 || position > limit) {
            throw badPosition(position, limit, param);
        }
        return position;
    }

    private static IllegalArgumentException badPosition(long position, long limit, String param) {
        if (position < 0) {
            throw new IllegalArgumentException("'" + param + "' must not be negative, but was " + position);
        }
        throw new IllegalArgumentException("'" + param + "' must be at most " + limit + ", but was " + position);
    }

    /**
     * Checks if the sub-range from {@code fromIndex} (inclusive) to {@code toIndex} (exclusive) is within the bounds of range from {@code 0} (inclusive) to {@code size} (exclusive).
     *
     * @param fromIndex the lower-bound (inclusive) of the sub-range
     * @param toIndex   the upper-bound (exclusive) of the sub-range
     * @param size      the upper-bound (exclusive) of the range
     * @return {@code fromIndex} if the sub-range is within bounds
     * @throws IndexOutOfBoundsException if the sub-range is out of bounds
     */
    public static int fromToIndex(int fromIndex, int toIndex, int size) {
        if (fromIndex < 0 || fromIndex > toIndex || toIndex > size) {
            throw badFromToIndex(fromIndex, toIndex, size);
        }
        return fromIndex;
    }

    /**
     * Checks if the sub-range from {@code fromIndex} (inclusive) to {@code toIndex} (exclusive) is within the bounds of range from {@code 0} (inclusive) to {@code size} (exclusive).
     *
     * @param fromIndex the lower-bound (inclusive) of the sub-range
     * @param toIndex   the upper-bound (exclusive) of the sub-range
     * @param size      the upper-bound (exclusive) of the range
     * @return {@code fromIndex} if the sub-range is within bounds
     * @throws IndexOutOfBoundsException if the sub-range is out of bounds
     */
    public static long fromToIndex(long fromIndex, long toIndex, long size) {
        if (fromIndex < 0 || fromIndex > toIndex || toIndex > size) {
            throw badFromToIndex(fromIndex, toIndex, size);
        }
        return fromIndex;
    }

    private static IndexOutOfBoundsException badFromToIndex(long fromIndex, long toIndex, long size) {
        if (fromIndex < 0) {
            throw new IndexOutOfBoundsException("fromIndex must not be negative, but was " + fromIndex);
        }
        if (fromIndex > toIndex) {
            throw new IndexOutOfBoundsException("fromIndex must be at most toIndex (" + toIndex + "), but was " + fromIndex);
        }
        throw new IndexOutOfBoundsException("toIndex must be at most " + size + ", but was " + toIndex);
    }

    /**
     * Checks if the sub-range from {@code fromIndex} (inclusive) to {@code fromIndex + size} (exclusive) is within the bounds of range from {@code 0} (inclusive) to {@code length} (exclusive).
     *
     * @param fromIndex the lower-bound (inclusive) of the sub-interval
     * @param size      the size of the sub-range
     * @param length    the upper-bound (exclusive) of the range
     * @return {@code fromIndex} if the sub-range is within bounds
     * @throws IndexOutOfBoundsException if the sub-range is out of bounds
     */
    public static int fromIndexSize(int fromIndex, int size, int length) {
        // Rolling our own because I feel the Need for Speed
        if (fromIndex < 0 || size < 0 || fromIndex > length - size) {
            throw badFromIndexSize(fromIndex, size, length);
        }
        return fromIndex;
    }

    /**
     * Checks if the sub-range from {@code fromIndex} (inclusive) to {@code fromIndex + size} (exclusive) is within the bounds of range from {@code 0} (inclusive) to {@code length} (exclusive).
     *
     * @param fromIndex the lower-bound (inclusive) of the sub-interval
     * @param size      the size of the sub-range
     * @param length    the upper-bound (exclusive) of the range
     * @return {@code fromIndex} if the sub-range is within bounds
     * @throws IndexOutOfBoundsException if the sub-range is out of bounds
     */
    public static long fromIndexSize(long fromIndex, long size, long length) {
        if (fromIndex < 0 || size < 0 || fromIndex > length - size) {
            throw badFromIndexSize(fromIndex, size, length);
        }
        return fromIndex;
    }

    private static IndexOutOfBoundsException badFromIndexSize(long fromIndex, long size, long length) {
        if (fromIndex < 0) {
            throw new IndexOutOfBoundsException("fromIndex must not be negative, but was " + fromIndex);
        }
        if (size < 0) {
            throw new IndexOutOfBoundsException("size must not be negative, but was " + size);
        }
        throw new IndexOutOfBoundsException("fromIndex + size must be at most " + length + ", but was " + Long.toUnsignedString(fromIndex + size));
    }

    /**
     * Checks that the given array is not {@code null} and not empty.
     *
     * @param array the array to check
     * @param param the name of the parameter for the error message
     * @param <T>   the component type of the array
     * @return {@code array} if it is not empty
     * @throws NullPointerException     if {@code array} is {@code null}
     * @throws IllegalArgumentException if {@code array} is empty
     */
    public static <T> T[] nonEmpty(T[] array, String param) {
        if (nonNull(array, param).length == 0) {
            throw new IllegalArgumentException("'" + param + "' must not be empty");
        }
        return array;
    }

    /**
     * Checks that the given collection is not {@code null} and not empty.
     *
     * @param collection the collection to check
     * @param param      the name of the parameter for the error message
     * @param <C>        the type of the collection
     * @return {@code collection} if it is not empty
     * @throws NullPointerException     if {@code collection} is {@code null}
     * @throws IllegalArgumentException if {@code collection} is empty
     */
    public static <C extends Collection<?>> C nonEmpty(C collection, String param) {
        if (nonNull(collection, param).isEmpty()) {
            throw new IllegalArgumentException("'" + param + "' must not be empty");
        }
        return collection;
    }

    /**
     * Checks that the given map is not {@code null} and not empty.
     *
     * @param map   the map to check
     * @param param the name of the parameter for the error message
     * @param <M>   the type of the map
     * @return {@code map} if it is not empty
     * @throws NullPointerException     if {@code map} is {@code null}
     * @throws IllegalArgumentException if {@code map} is empty
     */
    public static <M extends Map<?, ?>> M nonEmpty(M map, String param) {
        if (nonNull(map, param).isEmpty()) {
            throw new IllegalArgumentException("'" + param + "' must not be empty");
        }
        return map;
    }

    /**
     * Checks that the given value is strictly positive.
     *
     * @param value the value to check
     * @param param the name of the parameter for the error message
     * @return {@code value} if it is greater than 0
     * @throws IllegalArgumentException if {@code value} is 0 or less
     */
    public static int positive(int value, String param) {
        if (value <= 0) {
            throw new IllegalArgumentException("'" + param + "' must be greater than 0, but was " + value);
        }
        return value;
    }

    /**
     * Checks that the given value is strictly positive.
     *
     * @param value the value to check
     * @param param the name of the parameter for the error message
     * @return {@code value} if it is greater than 0
     * @throws IllegalArgumentException if {@code value} is 0 or less
     */
    public static long positive(long value, String param) {
        if (value <= 0) {
            throw new IllegalArgumentException("'" + param + "' must be greater than 0, but was " + value);
        }
        return value;
    }

    /**
     * @deprecated Use {@link #nonNegative(int, String)} instead. Or just inline this method.
     */
    @Deprecated(forRemoval = true)
    public static int positiveOrZero(int value, String param) {
        return nonNegative(value, param);
    }

    /**
     * @deprecated Use {@link #nonNegative(long, String)} instead. Or just inline this method.
     */
    @Deprecated(forRemoval = true)
    public static long positiveOrZero(long value, String param) {
        return nonNegative(value, param);
    }

    /**
     * Checks that the given value is positive or zero.
     *
     * @param value the value to check
     * @param param the name of the parameter for the error message
     * @return {@code value} if it is 0 or greater
     * @throws IllegalArgumentException if {@code value} is less than 0
     */
    public static int nonNegative(int value, String param) {
        if (value < 0) {
            throw new IllegalArgumentException("'" + param + "' must not be negative, but was " + value);
        }
        return value;
    }

    /**
     * Checks that the given value is positive or zero.
     *
     * @param value the value to check
     * @param param the name of the parameter for the error message
     * @return {@code value} if it is 0 or greater
     * @throws IllegalArgumentException if {@code value} is less than 0
     */
    public static long nonNegative(long value, String param) {
        if (value < 0) {
            throw new IllegalArgumentException("'" + param + "' must not be negative, but was " + value);
        }
        return value;
    }

    /**
     * Checks that the given value is between min and max inclusive.
     * <p>
     * Also asserts that min and max are ordered.
     *
     * @param value the value to check
     * @param min   the minimum value
     * @param max   the maximum value
     * @param param the name of the parameter for the error message
     * @return {@code value} if it is between min and max
     * @throws IllegalArgumentException if {@code value} is less than min or greater than max
     */
    public static int between(int value, int min, int max, String param) {
        assert min <= max : "min > max";
        if (min > value || value > max) {
            throw new IllegalArgumentException("'" + param + "' must be between " + min + " and " + max + ", but was " + value);
        }
        return value;
    }

    /**
     * Checks that the given value is between min and max inclusive.
     * <p>
     * Also asserts that min and max are ordered.
     *
     * @param value the value to check
     * @param min   the minimum value
     * @param max   the maximum value
     * @param param the name of the parameter for the error message
     * @return {@code value} if it is between min and max
     * @throws IllegalArgumentException if {@code value} is less than min or greater than max
     */
    public static long between(long value, long min, long max, String param) {
        assert min <= max : "min > max";
        if (min > value || value > max) {
            throw new IllegalArgumentException("'" + param + "' must be between " + min + " and " + max + ", but was " + value);
        }
        return value;
    }

    /**
     * Checks that the given value is a multiple of the divisor.
     * <p>
     * Also asserts that the divisor is greater than 0.
     *
     * @param value   the value to check
     * @param divisor the divisor
     * @param param   the name of the parameter for the error message
     * @throws IllegalArgumentException if {@code value} is not a multiple of {@code divisor}
     */
    public static void multipleOf(long value, long divisor, String param) {
        assert divisor > 0 : "divisor <= 0";
        if (value % divisor != 0) {
            throw new IllegalArgumentException("'" + param + "' must be a multiple of " + divisor + ", but was " + value);
        }
    }

}
