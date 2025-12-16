package wtf.reversed.toolbox.hash;

import wtf.reversed.toolbox.collect.*;

import java.util.*;

public abstract sealed class HashCode {
    public static HashCode ofBytes(Bytes bytes) {
        return new BytesHashCode(bytes);
    }

    public static HashCode ofInt(int hash) {
        return new IntHashCode(hash);
    }

    public static HashCode ofLong(long hash) {
        return new LongHashCode(hash);
    }

    HashCode() {
    }

    public abstract Bytes asBytes();

    public abstract int asInt();

    public abstract long asLong();

    static final class BytesHashCode extends HashCode {
        private final Bytes hash;

        BytesHashCode(Bytes hash) {
            this.hash = hash;
        }

        @Override
        public Bytes asBytes() {
            return hash;
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
        public boolean equals(Object obj) {
            return obj instanceof BytesHashCode other && hash.equals(other.hash);
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

    static final class IntHashCode extends HashCode {
        private final int hash;

        IntHashCode(int hash) {
            this.hash = hash;
        }

        @Override
        public Bytes asBytes() {
            return MutableBytes.allocate(Integer.BYTES).setInt(0, hash);
        }

        @Override
        public int asInt() {
            return hash;
        }

        @Override
        public long asLong() {
            throw new IllegalStateException("This hash code has only 32 bits; cannot be converted to a long");
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof IntHashCode other && hash == other.hash;
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

    static final class LongHashCode extends HashCode {
        private final long hash;

        LongHashCode(long hash) {
            this.hash = hash;
        }

        @Override
        public Bytes asBytes() {
            return MutableBytes.allocate(Long.BYTES).setLong(0, hash);
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
        public boolean equals(Object obj) {
            return obj instanceof LongHashCode other && hash == other.hash;
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
}
