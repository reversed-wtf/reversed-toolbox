package wtf.reversed.toolbox.hash;

import wtf.reversed.toolbox.util.Arrays;

import java.nio.*;
import java.util.*;

public abstract sealed class HashCode {
    public static HashCode ofBytes(byte[] bytes) {
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

    public abstract ByteBuffer asBuffer();

    public abstract int asInt();

    public abstract long asLong();

    public abstract int bits();

    @Override
    public boolean equals(Object obj) {
        return obj instanceof HashCode that && bits() == that.bits() && equalsSameBits(that);
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract String toString();

    abstract boolean equalsSameBits(HashCode that);

    private static final class BytesHashCode extends HashCode {
        private final byte[] hash;

        BytesHashCode(byte[] hash) {
            this.hash = hash;
        }

        @Override
        public ByteBuffer asBuffer() {
            return ByteBuffer
                .allocate(hash.length)
                .order(ByteOrder.LITTLE_ENDIAN)
                .put(hash)
                .flip();
        }

        @Override
        public int asInt() {
            return Arrays.getInt(hash, 0, ByteOrder.LITTLE_ENDIAN);
        }

        @Override
        public long asLong() {
            return Arrays.getLong(hash, 0, ByteOrder.LITTLE_ENDIAN);
        }

        @Override
        public int bits() {
            return hash.length * Byte.SIZE;
        }

        @Override
        boolean equalsSameBits(HashCode that) {
            return asBuffer().mismatch(that.asBuffer()) == -1;
        }

        @Override
        public int hashCode() {
            return java.util.Arrays.hashCode(hash);
        }

        @Override
        public String toString() {
            return HexFormat.of().formatHex(hash);
        }
    }

    private static final class IntHashCode extends HashCode {
        private final int hash;

        IntHashCode(int hash) {
            this.hash = hash;
        }

        @Override
        public ByteBuffer asBuffer() {
            return ByteBuffer
                .allocate(Integer.BYTES)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(hash)
                .flip();
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
        public int bits() {
            return Integer.SIZE;
        }

        @Override
        boolean equalsSameBits(HashCode that) {
            return hash == that.asInt();
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

    private static final class LongHashCode extends HashCode {
        private final long hash;

        LongHashCode(long hash) {
            this.hash = hash;
        }

        @Override
        public ByteBuffer asBuffer() {
            return ByteBuffer
                .allocate(Long.BYTES)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putLong(hash)
                .flip();
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
        public int bits() {
            return Long.SIZE;
        }

        @Override
        boolean equalsSameBits(HashCode that) {
            return hash == that.asLong();
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
