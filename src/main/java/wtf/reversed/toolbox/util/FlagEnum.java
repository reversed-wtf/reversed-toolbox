package wtf.reversed.toolbox.util;

import java.util.*;

/**
 * Represents an enum constant that can be combined into a bitwise flag value.
 */
public interface FlagEnum {
    int value();

    /**
     * Converts a flag value to a set of enum constants.
     *
     * @param enumClass the enum class
     * @param value     the flag value
     * @param <E>       the enum type
     * @return the set of flags
     */
    static <E extends Enum<E> & FlagEnum> Set<E> fromValue(Class<E> enumClass, int value) {
        EnumSet<E> result = EnumSet.noneOf(enumClass);
        for (E flag : FlagEnums.lookup(enumClass)) {
            int flagValue = flag.value();
            if ((value & flagValue) == flagValue) {
                result.add(flag);
                value &= ~flagValue;
            }
        }
        if (value != 0) {
            throw new IllegalArgumentException("Unknown bits: 0x" + Integer.toHexString(value));
        }
        return result;
    }


    /**
     * Converts a set of enum constants implementing the {@link FlagEnum} interface into an integer
     * value by combining their individual values using a bitwise OR operation.
     *
     * @param <E>   the type of the enum constants, which extends both {@link Enum} and {@link FlagEnum}
     * @param flags the set of enum constants to be converted to an integer value
     * @return the integer value representing the combined flags
     */
    static <E extends Enum<E> & FlagEnum> int toValue(Set<E> flags) {
        return flags.stream()
            .mapToInt(FlagEnum::value)
            .reduce(0, (a, b) -> a | b);
    }

    /**
     * Validates that the enum constants implementing the {@link FlagEnum} interface have exclusive bit flags.
     * <p>
     * This method is here in case you want to call it in a static initializer block.
     *
     * @param enumClass the class of the enum constants
     * @param <E>       the type of the enum constants, which extends both {@link Enum} and {@link FlagEnum}
     */
    static <E extends Enum<E> & FlagEnum> void validate(Class<E> enumClass) {
        FlagEnums.validate(enumClass);
    }
}
