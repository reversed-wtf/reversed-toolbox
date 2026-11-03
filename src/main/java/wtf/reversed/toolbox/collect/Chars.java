package wtf.reversed.toolbox.collect;

import wtf.reversed.toolbox.io.*;
import wtf.reversed.toolbox.util.*;

import javax.annotation.processing.*;
import java.io.*;
import java.nio.*;
import java.util.*;
import java.util.stream.*;

@Generated("wtf.reversed.toolbox.util.SliceGenerator")
public sealed class Chars extends Slice implements Comparable<Chars> {
    private static final Chars EMPTY = new Chars(EMPTY_ARRAY, 0, 0);

    Chars(byte[] array, int offset, int length) {
        super(array, offset, length);
        Check.argument((length & (Character.BYTES - 1)) == 0, "length must be a multiple of 2");
    }

    public static Chars empty() {
        return EMPTY;
    }

    public static Chars copyOf(char[] array) {
        return copyOf(array, 0, array.length);
    }

    public static Chars copyOf(char[] array, int offset, int length) {
        byte[] buffer = new byte[Math.multiplyExact(length, Character.BYTES)];
        ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asCharBuffer().put(array, offset, length);
        return new Chars(buffer, 0, buffer.length);
    }

    public static Chars copyOf(CharBuffer buffer) {
        Check.argument(buffer.hasArray(), "buffer must be backed by an array");
        return copyOf(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
    }

    public static Mutable allocate(int length) {
        int byteLength = Math.multiplyExact(length, Character.BYTES);
        return new Mutable(new byte[byteLength], 0, byteLength);
    }

    public char get(int index) {
        Check.index(index, length());
        return getInternal(index);
    }

    char getInternal(int index) {
        return (char) VH_CHAR_LE.get(array, offset + index * Character.BYTES);
    }

    @Override
    public int length() {
        return length >>> 1;
    }

    public boolean contains(char value) {
        return indexOf(value) >= 0;
    }

    public int indexOf(char value) {
        for (int i = 0, len = length(); i < len; i++) {
            if (getInternal(i) == value) {
                return i;
            }
        }
        return -1;
    }

    public int lastIndexOf(char value) {
        for (int i = length() - 1; i >= 0; i--) {
            if (getInternal(i) == value) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public CharBuffer asBuffer() {
        return asTypedBuffer().slice().asReadOnlyBuffer();
    }

    public void copyTo(Mutable target, int offset) {
        Check.fromIndexSize(offset, length(), target.length());
        System.arraycopy(array, this.offset, target.array, target.offset + Math.multiplyExact(offset, Character.BYTES), length);
    }

    public void copyTo(char[] target) {
        copyTo(target, 0, length());
    }

    public void copyTo(char[] target, int offset, int length) {
        Check.fromIndexSize(offset, length, target.length);
        Check.fromIndexSize(0, length, length());
        asTypedBuffer().get(target, offset, length);
    }

    public Chars slice(int offset) {
        return slice(offset, length() - offset);
    }

    public Chars slice(int offset, int length) {
        Check.fromIndexSize(offset, length, length());
        return new Chars(array, this.offset + Math.multiplyExact(offset, Character.BYTES), Math.multiplyExact(length, Character.BYTES));
    }

    public IntStream stream() {
        return IntStream.range(0, length()).map(i -> getInternal(i));
    }

    public char[] toArray() {
        char[] result = new char[length()];
        copyTo(result);
        return result;
    }

    CharBuffer asTypedBuffer() {
        return asByteBuffer().asCharBuffer();
    }

    @Override
    public int compareTo(Chars o) {
        int prefix = Math.min(length, o.length);
        int mismatch = Arrays.mismatch(array, offset, offset + prefix, o.array, o.offset, o.offset + prefix);
        if (mismatch < 0) {
            return Integer.compare(length(), o.length());
        }
        int idx = mismatch >>> 1;
        return Character.compare(getInternal(idx), o.getInternal(idx));
    }

    public static final class Mutable extends Chars {
        Mutable(byte[] array, int offset, int length) {
            super(array, offset, length);
        }

        public static Mutable copyOf(char[] array) {
            return copyOf(array, 0, array.length);
        }

        public static Mutable copyOf(char[] array, int offset, int length) {
            byte[] buffer = new byte[Math.multiplyExact(length, Character.BYTES)];
            ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asCharBuffer().put(array, offset, length);
            return new Mutable(buffer, 0, buffer.length);
        }

        public Mutable set(int index, char value) {
            Check.index(index, length());
            return setInternal(index, value);
        }

        private Mutable setInternal(int index, char value) {
            VH_CHAR_LE.set(array, offset + index * Character.BYTES, value);
            return this;
        }

        public CharBuffer asMutableBuffer() {
            return asTypedBuffer().slice();
        }

        public Mutable slice(int offset) {
            return slice(offset, length() - offset);
        }

        public Mutable slice(int offset, int length) {
            Check.fromIndexSize(offset, length, length());
            return new Mutable(array, this.offset + Math.multiplyExact(offset, Character.BYTES), Math.multiplyExact(length, Character.BYTES));
        }

        public Mutable copyFrom(char[] source) {
            return copyFrom(source, 0, source.length);
        }

        public Mutable copyFrom(char[] source, int offset, int length) {
            Check.fromIndexSize(offset, length, source.length);
            Check.fromIndexSize(0, length, length());
            asTypedBuffer().put(source, offset, length);
            return this;
        }

        public Mutable fill(char value) {
            if (value == (value & 0xFF) * 0x0101) {
                Arrays.fill(array, offset, offset + length, (byte) value);
            } else {
                for (int i = 0, len = length(); i < len; i++) {
                    setInternal(i, value);
                }
            }
            return this;
        }

        public Mutable fillFrom(BinarySource source) throws IOException {
            source.readBytes(new Bytes.Mutable(array, offset, length));
            if (source.order() == ByteOrder.BIG_ENDIAN) {
                for (int i = 0, len = length(); i < len; i++) {
                    setInternal(i, (char) VH_CHAR_BE.get(array, offset + i * Character.BYTES));
                }
            }
            return this;
        }
    }
}
