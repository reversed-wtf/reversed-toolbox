package wtf.reversed.toolbox.util;

import java.util.*;

/**
 * Utility class for determining the current operating system and architecture.
 *
 * @param os   the operating system
 * @param arch the CPU architecture
 */
public record Platform(OS os, Arch arch) {
    private static final Platform CURRENT = new Platform(
        OS.current(),
        Arch.current()
    );

    /**
     * Returns the platform the JVM is running on.
     *
     * @return the current platform
     */
    public static Platform current() {
        return CURRENT;
    }

    @Override
    public String toString() {
        return os.name().toLowerCase() + "-" + arch.name().toLowerCase();
    }

    /**
     * A CPU architecture, detected from the {@code os.arch} system property.
     */
    public enum Arch {
        X86_64,
        X86,
        ARM_64,
        ARM,
        ;

        private static final Arch CURRENT;

        static {
            String arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);
            CURRENT = switch (arch) {
                case "x86_64", "amd64", "em64t", "universal" -> X86_64;
                case "x86", "i386", "i486", "i586", "i686", "pentium" -> X86;
                case "aarch64" -> ARM_64;
                case "arm", "armv7l", "armv8l" -> ARM;
                default -> throw new ExceptionInInitializerError("Unsupported Architecture: " + arch);
            };
        }

        /**
         * Returns the architecture the JVM is running on.
         *
         * @return the current architecture
         */
        public static Arch current() {
            return CURRENT;
        }
    }

    /**
     * An operating system, detected from the {@code os.name} system property.
     */
    public enum OS {
        LINUX,
        WINDOWS,
        MAC,
        ;

        private static final OS CURRENT;

        static {
            String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
            CURRENT = switch (os.split(" ")[0]) {
                case "linux" -> LINUX;
                case "windows" -> WINDOWS;
                case "mac" -> MAC;
                default -> throw new ExceptionInInitializerError("Unsupported OS: " + os);
            };
        }

        /**
         * Returns the operating system the JVM is running on.
         *
         * @return the current operating system
         */
        public static OS current() {
            return CURRENT;
        }
    }
}
