package wtf.reversed.toolbox.hash;

import wtf.reversed.toolbox.collect.*;

import java.util.*;

/**
 * The fixed-size value produced by a {@link HashFunction}.
 *
 * <p>A hash code is always created from one of {@link #ofInt(int)}, {@link #ofLong(long)}, or {@link #ofBytes(Bytes)},
 * and can be viewed as any of those forms via {@link #asInt()}, {@link #asLong()}, and {@link #asBytes()}.
 */
public abstract sealed class HashCode {
    private HashCode() {
    }

    /**
     * Returns a hash code backed by a 32-bit integer.
     */
    public static HashCode ofInt(int hash) {
        return new OfInt(hash);
    }

    /**
     * Returns a hash code backed by a 64-bit integer.
     */
    public static HashCode ofLong(long hash) {
        return new OfLong(hash);
    }

    /**
     * Returns a hash code backed by a byte sequence.
     */
    public static HashCode ofBytes(Bytes bytes) {
        return new OfBytes(bytes);
    }

    /**
     * Returns this hash code as a 32-bit integer, truncating if necessary.
     */
    public abstract int asInt();

    /**
     * Returns this hash code as a 64-bit integer, truncating or zero-extending if necessary.
     */
    public abstract long asLong();

    /**
     * Returns this hash code as a byte sequence, little-endian if converting from an int or long.
     */
    public abstract Bytes asBytes();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract int hashCode();

    @Override
    public abstract String toString();

    private static final class OfInt extends HashCode {
        private final int hash;

        private OfInt(int hash) {
            this.hash = hash;
        }

        @Override
        public int asInt() {
            return hash;
        }

        @Override
        public long asLong() {
            return Integer.toUnsignedLong(hash);
        }

        @Override
        public Bytes asBytes() {
            return Bytes.Mutable
                .allocate(Integer.BYTES)
                .setInt(0, hash);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof OfInt other
                && hash == other.hash;
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(hash);
        }

        @Override
        public String toString() {
            return HexFormat.of().toHexDigits(hash);
        }
    }

    private static final class OfLong extends HashCode {
        private final long hash;

        private OfLong(long hash) {
            this.hash = hash;
        }

        @Override
        public int asInt() {
            return (int) hash;
        }

        @Override
        public long asLong() {
            return hash;
        }

        @Override
        public Bytes asBytes() {
            return Bytes.Mutable
                .allocate(Long.BYTES)
                .setLong(0, hash);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof OfLong other
                && hash == other.hash;
        }

        @Override
        public int hashCode() {
            return Long.hashCode(hash);
        }

        @Override
        public String toString() {
            return HexFormat.of().toHexDigits(hash);
        }
    }

    private static final class OfBytes extends HashCode {
        private final Bytes hash;

        private OfBytes(Bytes hash) {
            this.hash = hash;
        }

        @Override
        public int asInt() {
            return hash.getInt(0);
        }

        @Override
        public long asLong() {
            return hash.getLong(0);
        }

        @Override
        public Bytes asBytes() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof OfBytes other
                && hash.equals(other.hash);
        }

        @Override
        public int hashCode() {
            return hash.hashCode();
        }

        @Override
        public String toString() {
            return hash.toHexString(HexFormat.of());
        }
    }
}
