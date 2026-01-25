package wtf.reversed.toolbox.util;

import java.util.*;

/**
 * Represents an enum that has a value associated with it.
 *
 * @param <V> the type of the enum constant's value
 */
public interface ValueEnum<V> {
    /**
     * Returns the enum constant with the specified value, if present.
     *
     * @param enumClass the enum class
     * @param value     the value to search for
     * @param <V>       the type of the enum constant's value
     * @param <E>       the type of the enum constant
     * @return the enum constant with the specified value, or null if not found
     */
    static <V, E extends Enum<E> & ValueEnum<V>> E fromValue(Class<E> enumClass, V value) {
        E enumValue = ValueEnums.lookup(enumClass, value);
        if (enumValue == null) {
            throw new NoSuchElementException("Unknown " + enumClass.getName() + " value: '" + value + "'");
        }
        return enumValue;
    }

    /**
     * Returns the enum constant with the specified value, if present.
     *
     * @param enumClass the enum class
     * @param value     the value to search for
     * @param <V>       the type of the enum constant's value
     * @param <E>       the type of the enum constant
     * @return the enum constant with the specified value, or null if not found
     */
    static <V, E extends Enum<E> & ValueEnum<V>> Optional<E> fromValueOptional(Class<E> enumClass, V value) {
        return Optional.ofNullable(ValueEnums.lookup(enumClass, value));
    }

    /**
     * Returns the underlying value of this enum constant. This value is used for
     * equality comparisons and can be used to retrieve the enum constant from
     * its value.
     *
     * @return the underlying value of this enum constant.
     */
    V value();
}
