package wtf.reversed.toolbox.util;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

final class ValueEnums {
    private static final Map<Class<?>, Map<?, ?>> LOOKUP = new IdentityHashMap<>();

    private ValueEnums() {
    }

    @SuppressWarnings("unchecked")
    static <K, E extends ValueEnum<K>> E lookup(Class<E> enumType, K value) {
        return (E) LOOKUP
            .computeIfAbsent(enumType, _ -> Arrays.stream(enumType.getEnumConstants())
                .collect(Collectors.toUnmodifiableMap(ValueEnum::value, Function.identity())))
            .get(value);
    }
}
