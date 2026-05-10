package wtf.reversed.toolbox.io;

import wtf.reversed.toolbox.collect.*;
import wtf.reversed.toolbox.util.*;

import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;

/**
 * Random-access source of binary data with a configurable byte order. Supports reading primitives, bulk reads into
 * typed buffers, strings, and arbitrary user objects via a {@link Mapper}.
 */
public abstract class BinarySource implements Closeable {
    final long size;
    boolean bigEndian;

    BinarySource(long size) {
        this.size = Check.positiveOrZero(size, "size");
        this.bigEndian = false;
    }

    /**
     * Opens the file at the given path as a binary source.
     *
     * @param path file to open
     * @return a binary source backed by the file
     * @throws IOException if the file cannot be opened
     */
    public static BinarySource open(Path path) throws IOException {
        return FileBinarySource.create(path);
    }

    /**
     * Wraps the given byte sequence as a binary source.
     *
     * @param bytes bytes to wrap
     * @return a binary source backed by the bytes
     */
    public static BinarySource wrap(Bytes bytes) {
        return new BytesBinarySource(bytes);
    }

    /**
     * Creates a binary source that reads through the given sources in order, as if they were concatenated.
     *
     * @param sources non-empty list of sources to concatenate
     * @return a binary source over the concatenation
     */
    public static BinarySource sequence(List<? extends BinarySource> sources) {
        return new SequenceBinarySource(sources);
    }


    /**
     * Returns the byte order used when reading multibyte primitives.
     *
     * @return the current byte order
     */
    public final ByteOrder order() {
        return bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
    }

    /**
     * Sets the byte order used when reading multibyte primitives.
     *
     * @param order new byte order
     * @return this source, for chaining
     */
    public final BinarySource order(ByteOrder order) {
        this.bigEndian = Check.nonNull(order, "order") == ByteOrder.BIG_ENDIAN;
        return this;
    }

    /**
     * Returns the total size of this source in bytes.
     *
     * @return total size in bytes
     */
    public final long size() {
        return size;
    }

    /**
     * Returns the number of bytes between the current position and the end of this source.
     *
     * @return number of bytes remaining
     */
    public final long remaining() {
        return size - position();
    }

    /**
     * Advances the cursor by {@code count} bytes.
     *
     * @param count non-negative number of bytes to skip
     * @return this source, for chaining
     */
    public final BinarySource skip(long count) {
        return position(Math.addExact(position(), Check.positiveOrZero(count, "count")));
    }

    /**
     * Returns the current cursor position in bytes.
     *
     * @return current position
     */
    public abstract long position();

    /**
     * Sets the current cursor position in bytes.
     *
     * @param position new position, in {@code [0, size()]}
     * @return this source, for chaining
     */
    public abstract BinarySource position(long position);

    /**
     * Fills the given mutable byte buffer from the current position.
     *
     * @param target buffer to fill
     * @throws IOException if the read fails or fewer bytes than {@code target.length()} remain
     */
    public abstract void readBytes(Bytes.Mutable target) throws IOException;

    /**
     * Reads a single signed byte and advances the cursor by one byte.
     *
     * @return the byte read
     * @throws IOException if the read fails or no bytes remain
     */
    public abstract byte readByte() throws IOException;

    /**
     * Reads a 16-bit signed integer using the current byte order and advances the cursor by two bytes.
     *
     * @return the value read
     * @throws IOException if the read fails or fewer than two bytes remain
     */
    public abstract short readShort() throws IOException;

    /**
     * Reads a 32-bit signed integer using the current byte order and advances the cursor by four bytes.
     *
     * @return the value read
     * @throws IOException if the read fails or fewer than four bytes remain
     */
    public abstract int readInt() throws IOException;

    /**
     * Reads a 64-bit signed integer using the current byte order and advances the cursor by eight bytes.
     *
     * @return the value read
     * @throws IOException if the read fails or fewer than eight bytes remain
     */
    public abstract long readLong() throws IOException;

    /**
     * Reads a 64-bit signed integer and narrows it to an {@code int}, throwing if the value does not fit.
     *
     * @return the value read, as an {@code int}
     * @throws IOException         if the read fails or fewer than eight bytes remain
     * @throws ArithmeticException if the value does not fit in an {@code int}
     */
    public final int readLongAsInt() throws IOException {
        return Math.toIntExact(readLong());
    }

    /**
     * Reads a 16-bit IEEE-754 half-precision float and returns it as a {@code float}.
     *
     * @return the value read
     * @throws IOException if the read fails or fewer than two bytes remain
     */
    public final float readHalf() throws IOException {
        return Float.float16ToFloat(readShort());
    }

    /**
     * Reads a 32-bit IEEE-754 single-precision float.
     *
     * @return the value read
     * @throws IOException if the read fails or fewer than four bytes remain
     */
    public final float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    /**
     * Reads a 64-bit IEEE-754 double-precision float.
     *
     * @return the value read
     * @throws IOException if the read fails or fewer than eight bytes remain
     */
    public final double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    /**
     * Reads {@code count} bytes into a new {@link Bytes}.
     *
     * @param count non-negative number of bytes to read
     * @return the bytes read
     * @throws IOException if the read fails or fewer than {@code count} bytes remain
     */
    public final Bytes readBytes(int count) throws IOException {
        Check.positiveOrZero(count, "count");
        if (count == 0) {
            return Bytes.empty();
        }
        ensureRemaining(count * (long) Byte.BYTES);
        return Bytes.allocate(count).fillFrom(this);
    }

    /**
     * Reads {@code count} 16-bit signed integers using the current byte order.
     *
     * @param count non-negative number of values to read
     * @return the values read
     * @throws IOException if the read fails or insufficient bytes remain
     */
    public final Shorts readShorts(int count) throws IOException {
        Check.positiveOrZero(count, "count");
        if (count == 0) {
            return Shorts.empty();
        }
        ensureRemaining(count * (long) Short.BYTES);
        return Shorts.allocate(count).fillFrom(this);
    }

    /**
     * Reads {@code count} 32-bit signed integers using the current byte order.
     *
     * @param count non-negative number of values to read
     * @return the values read
     * @throws IOException if the read fails or insufficient bytes remain
     */
    public final Ints readInts(int count) throws IOException {
        Check.positiveOrZero(count, "count");
        if (count == 0) {
            return Ints.empty();
        }
        ensureRemaining(count * (long) Integer.BYTES);
        return Ints.allocate(count).fillFrom(this);
    }

    /**
     * Reads {@code count} 64-bit signed integers using the current byte order.
     *
     * @param count non-negative number of values to read
     * @return the values read
     * @throws IOException if the read fails or insufficient bytes remain
     */
    public final Longs readLongs(int count) throws IOException {
        Check.positiveOrZero(count, "count");
        if (count == 0) {
            return Longs.empty();
        }
        ensureRemaining(count * (long) Long.BYTES);
        return Longs.allocate(count).fillFrom(this);
    }

    /**
     * Reads {@code count} 32-bit single-precision floats using the current byte order.
     *
     * @param count non-negative number of values to read
     * @return the values read
     * @throws IOException if the read fails or insufficient bytes remain
     */
    public final Floats readFloats(int count) throws IOException {
        Check.positiveOrZero(count, "count");
        if (count == 0) {
            return Floats.empty();
        }
        ensureRemaining(count * (long) Float.BYTES);
        return Floats.allocate(count).fillFrom(this);
    }

    /**
     * Reads {@code count} 64-bit double-precision floats using the current byte order.
     *
     * @param count non-negative number of values to read
     * @return the values read
     * @throws IOException if the read fails or insufficient bytes remain
     */
    public final Doubles readDoubles(int count) throws IOException {
        Check.positiveOrZero(count, "count");
        if (count == 0) {
            return Doubles.empty();
        }
        ensureRemaining(count * (long) Double.BYTES);
        return Doubles.allocate(count).fillFrom(this);
    }

    /**
     * Reads {@code count} 64-bit signed integers and narrows each to an {@code int}.
     *
     * @param count non-negative number of values to read
     * @return the values read, as {@code int}s
     * @throws IOException         if the read fails or insufficient bytes remain
     * @throws ArithmeticException if any value does not fit in an {@code int}
     */
    public final Ints readLongsAsInts(int count) throws IOException {
        Check.positiveOrZero(count, "count");
        if (count == 0) {
            return Ints.empty();
        }
        ensureRemaining(count * (long) Long.BYTES);

        Ints.Mutable result = Ints.Mutable.allocate(count);
        for (int i = 0; i < result.length(); i++) {
            result.set(i, readLongAsInt());
        }
        return result;
    }

    /**
     * Reads {@code count} 16-bit half-precision floats and widens each to a {@code float}.
     *
     * @param count non-negative number of values to read
     * @return the values read, as {@code float}s
     * @throws IOException if the read fails or insufficient bytes remain
     */
    public final Floats readHalfs(int count) throws IOException {
        Check.positiveOrZero(count, "count");
        if (count == 0) {
            return Floats.empty();
        }
        ensureRemaining(count * (long) Short.BYTES);

        Floats.Mutable result = Floats.Mutable.allocate(count);
        for (int i = 0; i < result.length(); i++) {
            result.set(i, readHalf());
        }
        return result;
    }


    /**
     * Reads a boolean encoded in the given format. Only {@code 0} and {@code 1} are accepted.
     *
     * @param format width of the underlying integer
     * @return {@code true} if the value is {@code 1}, {@code false} if it is {@code 0}
     * @throws IOException if the read fails or the value is neither {@code 0} nor {@code 1}
     */
    public final boolean readBool(BoolFormat format) throws IOException {
        int value = switch (format) {
            case BYTE -> readByte();
            case SHORT -> readShort();
            case INT -> readInt();
        };
        return switch (value) {
            case 0 -> false;
            case 1 -> true;
            default -> throw new IOException("Unexpected value for bool: " + value);
        };
    }

    /**
     * Reads a string in the given format using UTF-8.
     *
     * @param format string framing
     * @return the decoded string
     * @throws IOException if the read fails
     */
    public final String readString(StringFormat format) throws IOException {
        return readString(format, StandardCharsets.UTF_8);
    }

    /**
     * Reads a string in the given format and charset.
     *
     * @param format  string framing
     * @param charset charset to decode with
     * @return the decoded string
     * @throws IOException if the read fails
     */
    public final String readString(StringFormat format, Charset charset) throws IOException {
        Check.nonNull(charset, "charset");
        return switch (format) {
            case BYTE_LENGTH -> readString(Byte.toUnsignedInt(readByte()), charset);
            case SHORT_LENGTH -> readString(Short.toUnsignedInt(readShort()), charset);
            case INT_LENGTH -> readString(readInt(), charset);
            case NULL_TERM -> readNullTerminatedString(charset);
        };
    }

    /**
     * Reads a fixed-length UTF-8 string.
     *
     * @param length non-negative number of bytes to read
     * @return the decoded string
     * @throws IOException if the read fails or insufficient bytes remain
     */
    public final String readString(int length) throws IOException {
        return readString(length, StandardCharsets.UTF_8);
    }

    /**
     * Reads a fixed-length string in the given charset.
     *
     * @param length  non-negative number of bytes to read
     * @param charset charset to decode with
     * @return the decoded string
     * @throws IOException if the read fails or insufficient bytes remain
     */
    public final String readString(int length, Charset charset) throws IOException {
        Check.positiveOrZero(length, "length");
        Check.nonNull(charset, "charset");
        if (length == 0) {
            return "";
        }
        return readBytes(length).toString(charset);
    }

    /**
     * Reads {@code count} consecutive strings in the given format using UTF-8.
     *
     * @param count  non-negative number of strings to read
     * @param format string framing
     * @return the decoded strings
     * @throws IOException if any read fails
     */
    public final List<String> readStrings(int count, StringFormat format) throws IOException {
        return readStrings(count, format, StandardCharsets.UTF_8);
    }

    /**
     * Reads {@code count} consecutive strings in the given format and charset.
     *
     * @param count   non-negative number of strings to read
     * @param format  string framing
     * @param charset charset to decode with
     * @return the decoded strings
     * @throws IOException if any read fails
     */
    public final List<String> readStrings(int count, StringFormat format, Charset charset) throws IOException {
        Check.nonNull(charset, "charset");
        return readObjects(count, reader -> reader.readString(format, charset));
    }

    /**
     * Reads a single object using the given mapper.
     *
     * @param mapper mapper that decodes the object from this source
     * @param <T>    object type produced by the mapper
     * @return the decoded object
     * @throws IOException if the mapper fails
     */
    public final <T> T readObject(Mapper<T> mapper) throws IOException {
        Check.nonNull(mapper, "mapper");
        return mapper.read(this);
    }

    /**
     * Reads {@code count} consecutive objects using the given mapper.
     *
     * @param count  non-negative number of objects to read
     * @param mapper mapper that decodes each object from this source
     * @param <T>    object type produced by the mapper
     * @return an immutable list of the decoded objects
     * @throws IOException if any mapper invocation fails
     */
    public final <T> List<T> readObjects(int count, Mapper<T> mapper) throws IOException {
        Check.nonNull(mapper, "mapper");
        List<T> result = new ArrayList<>(Check.positiveOrZero(count, "count"));
        for (int i = 0; i < count; i++) {
            result.add(mapper.read(this));
        }
        return List.copyOf(result);
    }


    /**
     * Reads a byte and throws if it does not match {@code expected}.
     *
     * @param expected expected byte value
     * @throws IOException if the read fails or the value does not match
     */
    public final void expectByte(byte expected) throws IOException {
        byte actual = readByte();
        if (actual != expected) {
            throw new IOException("Expected byte " + expected + ", but got " + actual);
        }
    }

    /**
     * Reads a short and throws if it does not match {@code expected}.
     *
     * @param expected expected short value
     * @throws IOException if the read fails or the value does not match
     */
    public final void expectShort(short expected) throws IOException {
        short actual = readShort();
        if (actual != expected) {
            throw new IOException("Expected short " + expected + ", but got " + actual);
        }
    }

    /**
     * Reads an int and throws if it does not match {@code expected}.
     *
     * @param expected expected int value
     * @throws IOException if the read fails or the value does not match
     */
    public final void expectInt(int expected) throws IOException {
        int actual = readInt();
        if (actual != expected) {
            throw new IOException("Expected int " + expected + ", but got " + actual);
        }
    }

    /**
     * Reads a long and throws if it does not match {@code expected}.
     *
     * @param expected expected long value
     * @throws IOException if the read fails or the value does not match
     */
    public final void expectLong(long expected) throws IOException {
        long actual = readLong();
        if (actual != expected) {
            throw new IOException("Expected long " + expected + ", but got " + actual);
        }
    }

    /**
     * Reads a float and throws if it does not match {@code expected}.
     *
     * @param expected expected float value
     * @throws IOException if the read fails or the value does not match
     */
    public final void expectFloat(float expected) throws IOException {
        float actual = readFloat();
        if (Float.compare(actual, expected) != 0) {
            throw new IOException("Expected float " + expected + ", but got " + actual);
        }
    }

    /**
     * Reads a double and throws if it does not match {@code expected}.
     *
     * @param expected expected double value
     * @throws IOException if the read fails or the value does not match
     */
    public final void expectDouble(double expected) throws IOException {
        double actual = readDouble();
        if (Double.compare(actual, expected) != 0) {
            throw new IOException("Expected double " + expected + ", but got " + actual);
        }
    }

    /**
     * Throws unless the cursor is at the end of this source.
     *
     * @throws IOException if any bytes remain
     */
    public final void expectEnd() throws IOException {
        if (remaining() > 0) {
            throw new IOException("Expected end of stream, but " + remaining() + " bytes remain");
        }
    }

    /**
     * Throws if fewer than {@code expected} bytes remain.
     *
     * @param expected minimum number of bytes that must remain
     * @throws EOFException if fewer than {@code expected} bytes remain
     * @throws IOException  if the position cannot be determined
     */
    public final void ensureRemaining(long expected) throws IOException {
        Check.positiveOrZero(expected, "expected");
        if (remaining() < expected) {
            throw eof(expected);
        }
    }

    final EOFException eof(long expected) {
        return new EOFException("Expected at least " + expected + " bytes remaining, but only " + remaining() + " are available");
    }

    private String readNullTerminatedString(Charset charset) throws IOException {
        return (switch (charset.name()) {
            case "UTF-16", "UTF-16BE", "UTF-16LE" -> readNullTerminatedString2();
            case "UTF-32", "UTF-32BE", "UTF-32LE" -> readNullTerminatedString4();
            default -> readNullTerminatedString1();
        }).toString(charset);
    }

    private ByteArrayOutputStream readNullTerminatedString1() throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        while (true) {
            byte b0 = readByte();
            if (b0 == 0) {
                break;
            }
            result.write(b0);
        }
        return result;
    }

    private ByteArrayOutputStream readNullTerminatedString2() throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        while (true) {
            byte b0 = readByte();
            byte b1 = readByte();
            if (b0 == 0 && b1 == 0) {
                break;
            }
            result.write(b0);
            result.write(b1);
        }
        return result;
    }

    private ByteArrayOutputStream readNullTerminatedString4() throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        while (true) {
            byte b0 = readByte();
            byte b1 = readByte();
            byte b2 = readByte();
            byte b3 = readByte();
            if (b0 == 0 && b1 == 0 && b2 == 0 && b3 == 0) {
                break;
            }
            result.write(b0);
            result.write(b1);
            result.write(b2);
            result.write(b3);
        }
        return result;
    }

    /**
     * Decodes a single object from a {@link BinarySource}.
     *
     * @param <T> type of object produced
     */
    @FunctionalInterface
    public interface Mapper<T> {
        /**
         * Reads one object from {@code source}, advancing its cursor by however many bytes the encoding occupies.
         *
         * @param source source to read from
         * @return the decoded object
         * @throws IOException if the read fails
         */
        T read(BinarySource source) throws IOException;
    }
}
