package wtf.reversed.toolbox.collect;

import wtf.reversed.toolbox.io.*;
import wtf.reversed.toolbox.util.*;

import javax.annotation.processing.*;
import java.io.*;
import java.nio.*;
import java.util.*;
import java.util.stream.*;

@Generated("wtf.reversed.toolbox.util.SliceGenerator")
public sealed class Ints extends Slice implements Comparable<Ints> {
    private static final Ints EMPTY = wrap(new int[0]);

    final int[] array;

    final int offset;

    final int length;

    private Ints(int[] array, int offset, int length) {
        Check.fromIndexSize(offset, length, array.length);
        this.array = array;
        this.offset = offset;
        this.length = length;
    }

    public static Ints empty() {
        return EMPTY;
    }

    public static Ints wrap(int[] array) {
        return new Ints(array, 0, array.length);
    }

    public static Ints wrap(int[] array, int offset, int length) {
        return new Ints(array, offset, length);
    }

    public static Mutable allocate(int length) {
        return new Mutable(new int[length], 0, length);
    }

    public static Ints from(IntBuffer buffer) {
        Check.argument(buffer.hasArray(), "buffer must be backed by an array");
        return new Ints(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
    }

    public int get(int index) {
        Check.index(index, length);
        return array[offset + index];
    }

    public long getUnsigned(int offset) {
        return Integer.toUnsignedLong(get(offset));
    }

    @Override
    public int length() {
        return length;
    }

    public boolean contains(int value) {
        return indexOf(value) >= 0;
    }

    public int indexOf(int value) {
        for (int i = offset, limit = offset + length; i < limit; i++) {
            if (array[i] == value) {
                return i - offset;
            }
        }
        return -1;
    }

    public int lastIndexOf(int value) {
        for (int i = offset + length - 1; i >= offset; i--) {
            if (array[i] == value) {
                return i - offset;
            }
        }
        return -1;
    }

    public Ints slice(int offset) {
        return slice(offset, length - offset);
    }

    public Ints slice(int offset, int length) {
        Check.fromIndexSize(offset, length, this.length);
        return new Ints(array, this.offset + offset, length);
    }

    public void copyTo(Mutable target, int offset) {
        Check.fromIndexSize(offset, length, target.length);
        System.arraycopy(array, this.offset, target.array, target.offset + offset, length);
    }

    @Override
    public IntBuffer asBuffer() {
        return IntBuffer.wrap(array, offset, length).slice().asReadOnlyBuffer();
    }

    @Override
    public Bytes asBytes() {
        var result = ByteBuffer.allocate(length * Integer.BYTES).order(ByteOrder.LITTLE_ENDIAN);
        result.asIntBuffer().put(array, offset, length);
        return Bytes.wrap(result.array());
    }

    public int[] toArray() {
        return Arrays.copyOfRange(array, offset, offset + length);
    }

    public IntStream stream() {
        return Arrays.stream(array, offset, offset + length);
    }

    @Override
    public int compareTo(Ints o) {
        return Arrays.compare(array, offset, offset + length, o.array, o.offset, o.offset + o.length);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Ints o && Arrays.equals(array, offset, offset + length, o.array, o.offset, o.offset + o.length);
    }

    @Override
    public int hashCode() {
        int result = 1;
        for (int i = offset, limit = offset + length; i < limit; i++) {
            result = 31 * result + Integer.hashCode(array[i]);
        }
        return result;
    }

    @Override
    public String toString() {
        return "[" + length + " ints]";
    }

    public static final class Mutable extends Ints {
        private Mutable(int[] array, int offset, int length) {
            super(array, offset, length);
        }

        public static Mutable wrap(int[] array) {
            return new Mutable(array, 0, array.length);
        }

        public static Mutable wrap(int[] array, int offset, int length) {
            return new Mutable(array, offset, length);
        }

        public Mutable set(int index, int value) {
            Check.index(index, length);
            array[offset + index] = value;
            return this;
        }

        public Mutable slice(int offset) {
            return slice(offset, length - offset);
        }

        public Mutable slice(int offset, int length) {
            Check.fromIndexSize(offset, length, this.length);
            return new Mutable(array, this.offset + offset, length);
        }

        public Mutable copyFrom(int[] src) {
            return copyFrom(src, 0, src.length);
        }

        public Mutable copyFrom(int[] src, int offset, int length) {
            Check.fromIndexSize(offset, length, src.length);
            System.arraycopy(src, offset, array, this.offset, length);
            return this;
        }

        public Mutable copyWithin(int srcIndex, int dstIndex, int length) {
            Check.fromIndexSize(srcIndex, length, this.length);
            Check.fromIndexSize(dstIndex, length, this.length);
            System.arraycopy(array, this.offset + srcIndex, array, this.offset + dstIndex, length);
            return this;
        }

        public Mutable fill(int value) {
            Arrays.fill(array, offset, offset + length, value);
            return this;
        }

        public Mutable fillFrom(BinarySource source) throws IOException {
            for (int i = 0; i < length; i++) {
                array[offset + i] = source.readInt();
            }
            return this;
        }

        public IntBuffer asMutableBuffer() {
            return IntBuffer.wrap(array, offset, length).slice();
        }
    }
}
