package wtf.reversed.toolbox.util;

import java.util.*;

final class FlagEnums {
    private static final Set<Class<?>> VALID = Collections.newSetFromMap(new IdentityHashMap<>());
    private static final Map<Class<?>, Enum<?>[]> LOOKUP = new IdentityHashMap<>();

    private FlagEnums() {
    }

    @SuppressWarnings("unchecked")
    static <E extends Enum<E> & FlagEnum> E[] lookup(Class<E> enumClass) {
        if (!VALID.contains(enumClass)) {
            validate(enumClass);
        }
        return (E[]) LOOKUP.computeIfAbsent(enumClass, clazz -> (Enum<?>[]) clazz.getEnumConstants());
    }

    static <E extends Enum<E> & FlagEnum> void validate(Class<E> enumClass) {
        long seen = 0;
        for (E flag : enumClass.getEnumConstants()) {
            // Check for zero values, which make no sense for flags
            if (flag.value() == 0) {
                throw new IllegalStateException(String.format(
                    "Flag value cannot be 0 in %s: '%s'.",
                    enumClass.getSimpleName(), flag.name()
                ));
            }

            // Check for bit overlap with any previously seen flags
            if ((seen & flag.value()) != 0) {
                throw new IllegalStateException(String.format(
                    "Bitflag collision in %s: '%s' (0x%X) overlaps with existing flags.",
                    enumClass.getSimpleName(), flag.name(), flag.value()
                ));
            }

            seen |= flag.value();
        }
        VALID.add(enumClass);
    }
}
