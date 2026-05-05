package wtf.reversed.toolbox.util;

import com.squareup.javapoet.*;
import wtf.reversed.toolbox.io.*;

import javax.annotation.processing.*;
import javax.lang.model.element.*;
import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

final class SliceGenerator {
    private static final String PACKAGE_NAME = "wtf.reversed.toolbox.collect";
    private static final ClassName PARENT_CLASS = ClassName.get(PACKAGE_NAME, "Slice");
    private static final ClassName CHECK_CLASS = ClassName.get("wtf.reversed.toolbox.util", "Check");

    private final SliceType type;
    private final ClassName thisType;
    private final ClassName mutableType;
    private final Class<?> primitiveType;
    private final TypeName arrayType;
    private final ClassName boxedType;
    private final ClassName bufferType;
    private final int primitiveSize;

    SliceGenerator(SliceType type) {
        this.type = type;
        this.thisType = ClassName.get("", type.typeName());
        this.mutableType = ClassName.get("", "Mutable");
        this.primitiveType = type.primitiveType();
        this.arrayType = ArrayTypeName.of(TypeName.get(primitiveType));
        this.boxedType = ClassName.get(type.boxedType());
        this.bufferType = ClassName.get(type.bufferType());
        this.primitiveSize = type.primitiveSize();
    }

    static void main() throws Exception {
        // generateParent();
        new SliceGenerator(SliceType.Bytes).generate();
        new SliceGenerator(SliceType.Shorts).generate();
        new SliceGenerator(SliceType.Ints).generate();
        new SliceGenerator(SliceType.Longs).generate();
        new SliceGenerator(SliceType.Floats).generate();
        new SliceGenerator(SliceType.Doubles).generate();
    }

    private void generateParent() throws IOException {
        writeClass(createInterface());
    }

    private void generate() throws IOException {
        writeClass(createWrapperClass());
    }

    private TypeSpec createInterface() {
        return TypeSpec.interfaceBuilder(PARENT_CLASS)
            .addModifiers(Modifier.PUBLIC)
            .addMethod(MethodSpec.methodBuilder("length")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(int.class)
                .build())
            .addMethod(MethodSpec.methodBuilder("asBuffer")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(Buffer.class)
                .build())
            .build();
    }

    private TypeSpec createWrapperClass() {
        var builder = TypeSpec.classBuilder(thisType)
            .addModifiers(Modifier.PUBLIC, Modifier.SEALED)
            .superclass(PARENT_CLASS)
            .addSuperinterface(ParameterizedTypeName.get(ClassName.get(Comparable.class), thisType))
            .addAnnotation(AnnotationSpec.builder(Generated.class)
                .addMember("value", "$S", this.getClass().getName())
                .build());

        // Fields
        builder.addField(FieldSpec.builder(thisType, "EMPTY", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
            .initializer("new $T(EMPTY_ARRAY, 0, 0)", thisType)
            .build());

        // Constructors
        builder.addMethod(generateConstructor());
        builder.addMethod(generateEmpty());
        builder.addMethod(generateWrap1(thisType));
        builder.addMethod(generateWrap3(thisType));
        builder.addMethod(generateAllocate());
        builder.addMethod(generateFrom());

        // Getters
        builder.addMethod(generateGet());
        builder.addMethod(generateGetInternal());
        switch (type) {
            case Bytes -> {
                builder.addMethod(generateGet(short.class, "getShort", "Short.BYTES"));
                builder.addMethod(generateGet(int.class, "getInt", "Integer.BYTES"));
                builder.addMethod(generateGet(long.class, "getLong", "Long.BYTES"));
                builder.addMethod(generateGet(float.class, "getFloat", "Float.BYTES"));
                builder.addMethod(generateGet(double.class, "getDouble", "Double.BYTES"));
                builder.addMethod(generateGetUnsigned(int.class, "getUnsigned", "get", "Byte.toUnsignedInt"));
                builder.addMethod(generateGetUnsigned(int.class, "getUnsignedShort", "getShort", "Short.toUnsignedInt"));
                builder.addMethod(generateGetUnsigned(long.class, "getUnsignedInt", "getInt", "Integer.toUnsignedLong"));
            }
            case Shorts ->
                builder.addMethod(generateGetUnsigned(int.class, "getUnsigned", "get", "Short.toUnsignedInt"));
            case Ints ->
                builder.addMethod(generateGetUnsigned(long.class, "getUnsigned", "get", "Integer.toUnsignedLong"));
        }

        // List equivalent methods
        builder.addMethod(generateLength());
        builder.addMethod(generateContains());
        builder.addMethod(generateIndexOf());
        builder.addMethod(generateLastIndexOf());

        // Slice methods
        builder.addMethod(generateSlice1(thisType));
        builder.addMethod(generateSlice2(thisType));
        builder.addMethod(generateCopyTo());
        builder.addMethod(generateAsBuffer());
        builder.addMethod(generateStream());
        builder.addMethod(generateToArray());
        if (type.isByte()) {
            builder.addMethod(generateAsBytesOverride());
            builder.addMethod(generateAsInputStream());
            builder.addMethod(generateToHexStringWithFormat());
            builder.addMethod(generateToStringWithCharset());
        }

        builder.addMethod(generateCompareTo());
        builder.addMethod(generateEquals());
        builder.addMethod(generateHashCode());
        builder.addMethod(generateToString());

        builder.addType(createMutableWrapperClass());

        return builder.build();
    }

    private MethodSpec generateConstructor() {
        return MethodSpec.constructorBuilder()
            .addParameter(byte[].class, "array")
            .addParameter(int.class, "offset")
            .addParameter(int.class, "length")
            .addStatement("super(array, offset, length)", CHECK_CLASS)
            .build();
    }

    private MethodSpec generateEmpty() {
        return MethodSpec.methodBuilder("empty")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(thisType)
            .addStatement("return EMPTY")
            .build();
    }

    private MethodSpec generateAllocate() {
        return MethodSpec.methodBuilder("allocate")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(int.class, "length")
            .returns(mutableType)
            .addStatement("int byteLength = $L", adjust("length"))
            .addStatement("return new $L(new byte[byteLength], 0, byteLength)", mutableType)
            .build();
    }

    private MethodSpec generateFrom() {
        return MethodSpec.methodBuilder("from")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(bufferType, "buffer")
            .returns(thisType)
            .addStatement("$T.argument(buffer.hasArray(), \"buffer must be backed by an array\")", CHECK_CLASS)
            .addStatement("return wrap(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining())", thisType)
            .build();
    }

    private MethodSpec generateGet() {
        return MethodSpec.methodBuilder("get")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(int.class, "index")
            .returns(type.primitiveType())
            .addStatement("$T.index(index, $L)", CHECK_CLASS, length())
            .addStatement("return getInternal(index)")
            .build();
    }

    private MethodSpec generateGetInternal() {
        // Can't make this private, as it's used by the mutable subtype
        var builder = MethodSpec.methodBuilder("getInternal")
            .addParameter(int.class, "index")
            .returns(type.primitiveType());

        if (type == SliceType.Bytes) {
            builder.addStatement("return array[offset + index]");
        } else {
            builder.addStatement("return ($T) $L.get(array, offset + $L)",
                primitiveType, varHandleName(primitiveType), adjust("index"));
        }

        return builder.build();
    }

    private MethodSpec generateLength() {
        return JavaPoetUtils.override("length")
            .returns(int.class)
            .addStatement(type.isByte() ? "return length" : "return length >>> $L",
                Integer.numberOfTrailingZeros(primitiveSize))
            .build();
    }

    private MethodSpec generateContains() {
        return MethodSpec.methodBuilder("contains")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(primitiveType, "value")
            .returns(boolean.class)
            .addStatement("return indexOf(value) >= 0")
            .build();
    }

    private MethodSpec generateIndexOf() {
        return MethodSpec.methodBuilder("indexOf")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(primitiveType, "value")
            .returns(int.class)
            .beginControlFlow("for (int i = 0, limit = $L; i < limit; i++)", length())
            .beginControlFlow("if ($L)", JavaPoetUtils.primitiveEquals("getInternal(i)", "value", primitiveType))
            .addStatement("return i")
            .endControlFlow()
            .endControlFlow()
            .addStatement("return -1")
            .build();
    }

    private MethodSpec generateLastIndexOf() {
        return MethodSpec.methodBuilder("lastIndexOf")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(primitiveType, "value")
            .returns(int.class)
            .beginControlFlow("for (int i = $L - 1; i >= 0; i--)", length())
            .beginControlFlow("if ($L)", JavaPoetUtils.primitiveEquals("getInternal(i)", "value", primitiveType))
            .addStatement("return i")
            .endControlFlow()
            .endControlFlow()
            .addStatement("return -1")
            .build();
    }

    private MethodSpec generateCopyTo() {
        return MethodSpec.methodBuilder("copyTo")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(mutableType, "target")
            .addParameter(int.class, "offset")
            .returns(void.class)
            .addStatement("$T.fromIndexSize(offset, length(), target.length())", CHECK_CLASS)
            .addStatement("System.arraycopy(array, this.offset, target.array, target.offset + $L, length)", adjust("offset"))
            .build();
    }

    private MethodSpec generateAsBuffer() {
        return JavaPoetUtils.override("asBuffer")
            .returns(bufferType)
            .addStatement("return asByteBuffer()$L.slice().asReadOnlyBuffer()",
                type.isByte() ? "" : ".as" + shortPrimitiveName() + "Buffer()")
            .build();
    }

    private MethodSpec generateAsBytesOverride() {
        return JavaPoetUtils.override("asBytes")
            .returns(thisType)
            .addStatement("return this")
            .build();
    }

    private MethodSpec generateAsInputStream() {
        return MethodSpec.methodBuilder("asInputStream")
            .addModifiers(Modifier.PUBLIC)
            .returns(InputStream.class)
            .addStatement("return new $T(array, offset, length)", ByteArrayInputStream.class)
            .build();
    }

    private MethodSpec generateStream() {
        var returnType = switch (type) {
            case Bytes, Shorts, Ints -> IntStream.class;
            case Longs -> LongStream.class;
            case Floats, Doubles -> DoubleStream.class;
        };

        var mapMethod = switch (type) {
            case Bytes, Shorts, Ints -> "";
            case Longs -> "ToLong";
            case Floats, Doubles -> "ToDouble";
        };

        return MethodSpec.methodBuilder("stream")
            .addModifiers(Modifier.PUBLIC)
            .returns(returnType)
            .addStatement("return $T.range(0, $L).map$L(i -> getInternal(i))", IntStream.class, length(), mapMethod)
            .build();
    }

    private MethodSpec generateToArray() {
        var codeBlock = type.isByte()
            ? CodeBlock.of("return $T.copyOfRange(array, offset, offset + length);", Arrays.class)
            : CodeBlock.builder()
              .addStatement("$T result = new $T[length()]", arrayType, type.primitiveType())
              .addStatement("asBuffer().get(result)")
              .addStatement("return result")
              .build();

        return MethodSpec.methodBuilder("toArray")
            .addModifiers(Modifier.PUBLIC)
            .returns(arrayType)
            .addCode(codeBlock)
            .build();
    }

    private MethodSpec generateToHexStringWithFormat() {
        return MethodSpec.methodBuilder("toHexString")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(HexFormat.class, "format")
            .returns(String.class)
            .addStatement("return format.formatHex(array, offset, offset + length)")
            .build();
    }

    private MethodSpec generateToStringWithCharset() {
        return MethodSpec.methodBuilder("toString")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(Charset.class, "charset")
            .returns(String.class)
            .addStatement("return new String(array, offset, length, charset)")
            .build();
    }

    private MethodSpec generateCompareTo() {
        var builder = JavaPoetUtils.override("compareTo")
            .returns(int.class)
            .addParameter(thisType, "o");

        if (type.isByte()) {
            builder.addStatement("return $T.compare(array, offset, offset + length, o.array, o.offset, o.offset + o.length)", Arrays.class);
        } else {
            builder.addStatement("int min = Math.min(length(), o.length())");
            builder.beginControlFlow("for (int i = 0; i < min; i++)");
            builder.addStatement("int c = $T.compare(getInternal(i), o.getInternal(i))", boxedType);
            builder.beginControlFlow("if (c != 0)");
            builder.addStatement("return c");
            builder.endControlFlow();
            builder.endControlFlow();
            builder.addStatement("return Integer.compare(length(), o.length())");
        }

        return builder.build();
    }

    private MethodSpec generateEquals() {
        var equalsBlock = type.isIntegral()
            ? CodeBlock.of("return $T.equals(array, offset, offset + length, o.array, o.offset, o.offset + o.length);", Arrays.class)
            : CodeBlock.builder()
              .beginControlFlow("if (length() != o.length())")
              .addStatement("return false")
              .endControlFlow()
              .beginControlFlow("for (int i = 0, len = length(); i < len; i++)")
              .beginControlFlow("if ($T.compare(getInternal(i), o.getInternal(i)) != 0)", boxedType)
              .addStatement("return false")
              .endControlFlow()
              .endControlFlow()
              .addStatement("return true")
              .build();

        return JavaPoetUtils.equalsBuilder("obj")
            .beginControlFlow("if (obj == this)")
            .addStatement("return true")
            .endControlFlow()
            .beginControlFlow("if (!(obj instanceof $L o))", thisType)
            .addStatement("return false")
            .endControlFlow()
            .addCode("$L", equalsBlock)
            .build();
    }

    private MethodSpec generateHashCode() {
        return JavaPoetUtils.hashCodeBuilder()
            .addStatement("int result = 1")
            .beginControlFlow("for (int i = 0, len = length(); i < len; i++)")
            .addStatement("result = 31 * result + $T.hashCode(getInternal(i))", boxedType)
            .endControlFlow()
            .addStatement("return result")
            .build();
    }

    private MethodSpec generateToString() {
        return JavaPoetUtils.toStringBuilder()
            .addStatement("return $S + $L + $S", "[", length(), " " + primitiveType.toString() + "s]")
            .build();
    }

    private MethodSpec generateGet(Class<?> returnType, String name, String length) {
        return MethodSpec.methodBuilder(name)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(int.class, "offset")
            .returns(returnType)
            .addStatement("$T.fromIndexSize(offset, $L, length)", CHECK_CLASS, length)
            .addStatement("return ($T) $L.get(array, this.offset + offset)", returnType, varHandleName(returnType))
            .build();
    }

    private MethodSpec generateGetUnsigned(Class<?> returnType, String name, String accessor, String converter) {
        return MethodSpec.methodBuilder(name)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(int.class, "offset")
            .returns(returnType)
            .addStatement("return $L($L(offset))", converter, accessor)
            .build();
    }

    // region Mutable

    private TypeSpec createMutableWrapperClass() {
        var builder = TypeSpec.classBuilder(mutableType)
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .superclass(thisType);

        builder.addMethod(generateMutableConstructor());
        builder.addMethod(generateWrap1(mutableType));
        builder.addMethod(generateWrap3(mutableType));
        builder.addMethod(generateSet());
        builder.addMethod(generateSetInternal());

        if (type.isByte()) {
            builder.addMethod(generateHandleSet(short.class, Short.class, "setShort"));
            builder.addMethod(generateHandleSet(int.class, Integer.class, "setInt"));
            builder.addMethod(generateHandleSet(long.class, Long.class, "setLong"));
            builder.addMethod(generateHandleSet(float.class, Float.class, "setFloat"));
            builder.addMethod(generateHandleSet(double.class, Double.class, "setDouble"));
        }

        builder.addMethod(generateSlice1(mutableType));
        builder.addMethod(generateSlice2(mutableType));
        builder.addMethod(generateCopyFrom1());
        builder.addMethod(generateCopyFrom3());
        builder.addMethod(generateCopyWithin());
        builder.addMethod(generateFill());
        builder.addMethod(generateFillFrom());
        builder.addMethod(generateAsMutableBuffer());

        return builder.build();
    }

    private MethodSpec generateMutableConstructor() {
        return MethodSpec.constructorBuilder()
            .addParameter(byte[].class, "array")
            .addParameter(int.class, "offset")
            .addParameter(int.class, "length")
            .addStatement("super(array, offset, length)")
            .build();
    }

    private MethodSpec generateSet() {
        return MethodSpec.methodBuilder("set")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(int.class, "index")
            .addParameter(primitiveType, "value")
            .returns(mutableType)
            .addStatement("$T.index(index, $L)", CHECK_CLASS, length())
            .addStatement("return setInternal(index, value)")
            .build();
    }

    private MethodSpec generateSetInternal() {
        var builder = MethodSpec.methodBuilder("setInternal")
            .addModifiers(Modifier.PRIVATE)
            .addParameter(int.class, "index")
            .addParameter(primitiveType, "value")
            .returns(mutableType);

        if (type.isByte()) {
            builder.addStatement("array[offset + index] = value");
        } else {
            builder.addStatement("$L.set(array, offset + $L, value)",
                varHandleName(primitiveType), adjust("index"));
        }

        return builder
            .addStatement("return this")
            .build();
    }

    private MethodSpec generateCopyFrom1() {
        return MethodSpec.methodBuilder("copyFrom")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(arrayType, "src")
            .returns(mutableType)
            .addStatement("return copyFrom(src, 0, src.length)")
            .build();
    }

    private MethodSpec generateCopyFrom3() {
        var builder = MethodSpec.methodBuilder("copyFrom")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(arrayType, "src")
            .addParameter(int.class, "offset")
            .addParameter(int.class, "length")
            .returns(mutableType)
            .addStatement("$T.fromIndexSize(offset, length, src.length)", CHECK_CLASS)
            .addStatement("$T.fromIndexSize(0, length, $L)", CHECK_CLASS, length());

        if (type.isByte()) {
            builder.addStatement("System.arraycopy(src, offset, array, this.offset, length)");
        } else {
            builder.addStatement("asByteBuffer().as$LBuffer().put(src, offset, length)", shortPrimitiveName());
        }

        return builder
            .addStatement("return this")
            .build();
    }

    private MethodSpec generateCopyWithin() {
        return MethodSpec.methodBuilder("copyWithin")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(int.class, "srcIndex")
            .addParameter(int.class, "dstIndex")
            .addParameter(int.class, "length")
            .returns(mutableType)
            .addStatement("copyWithinBytes($L, $L, $L)", adjust("srcIndex"), adjust("dstIndex"), adjust("length"))
            .addStatement("return this")
            .build();
    }

    private MethodSpec generateFill() {
        var builder = MethodSpec.methodBuilder("fill")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(primitiveType, "value")
            .returns(mutableType);

        switch (type) {
            case Bytes:
                builder.addStatement("$T.fill(array, offset, offset + length, value)", Arrays.class);
                break;
            case Shorts:
            case Ints:
            case Longs:
                var multiplier = "0x" + "01".repeat(primitiveSize) + (primitiveType == long.class ? "L" : "");
                builder.beginControlFlow("if (value == (value & 0xFF) * $L)", multiplier);
                builder.addStatement("$T.fill(array, offset, offset + length, (byte) value)", Arrays.class);
                builder.nextControlFlow("else");
                generateFillLoop(builder);
                builder.endControlFlow();
                break;
            case Floats:
                builder.beginControlFlow("if ($T.floatToRawIntBits(value) == 0)", Float.class);
                builder.addStatement("$T.fill(array, offset, offset + length, (byte) 0)", Arrays.class);
                builder.nextControlFlow("else");
                generateFillLoop(builder);
                builder.endControlFlow();
                break;
            case Doubles:
                builder.beginControlFlow("if ($T.doubleToRawLongBits(value) == 0L)", Double.class);
                builder.addStatement("$T.fill(array, offset, offset + length, (byte) 0)", Arrays.class);
                builder.nextControlFlow("else");
                generateFillLoop(builder);
                builder.endControlFlow();
                break;
        }

        return builder
            .addStatement("return this")
            .build();
    }

    private void generateFillLoop(MethodSpec.Builder builder) {
        builder.beginControlFlow("for (int i = 0; i < length(); i++)");
        builder.addStatement("setInternal(i, value)");
        builder.endControlFlow();
    }

    private MethodSpec generateFillFrom() {
        var builder = MethodSpec.methodBuilder("fillFrom")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(BinarySource.class, "source")
            .addException(IOException.class)
            .returns(mutableType);

        if (type.isByte()) {
            builder.addStatement("source.readBytes(this)");
        } else {
            builder
                .addStatement("source.readBytes(new Bytes.Mutable(array, offset, length))")
                .beginControlFlow("if (source.order() == $T.BIG_ENDIAN)", ByteOrder.class)
                .beginControlFlow("for (int i = 0, len = length(); i < len; i++)")
                .addStatement("setInternal(i, ($T) $L.get(array, offset + $L))",
                    primitiveType, varHandleName(primitiveType) + "_BE", adjust("i"))
                .endControlFlow()
                .endControlFlow();
        }
        return builder.addStatement("return this").build();
    }

    private MethodSpec generateAsMutableBuffer() {
        return MethodSpec.methodBuilder("asMutableBuffer")
            .addModifiers(Modifier.PUBLIC)
            .returns(bufferType)
            .addStatement("return asByteBuffer()$L.slice()", type.isByte() ? "" : ".as" + shortPrimitiveName() + "Buffer()")
            .build();
    }

    private MethodSpec generateHandleSet(Class<?> valueType, Class<?> boxedType, String name) {
        return MethodSpec.methodBuilder(name)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(int.class, "offset")
            .addParameter(valueType, "value")
            .returns(mutableType)
            .addStatement("$T.fromIndexSize(offset, $T.BYTES, $L)", CHECK_CLASS, boxedType, length())
            .addStatement("$L.set(array, this.offset + $L, value)", varHandleName(valueType), adjust("offset"))
            .addStatement("return this")
            .build();
    }

    private MethodSpec generateSlice1(ClassName className) {
        return MethodSpec.methodBuilder("slice")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(int.class, "offset")
            .returns(className)
            .addStatement("return slice(offset, $L - offset)", length())
            .build();
    }

    private MethodSpec generateSlice2(ClassName className) {
        return MethodSpec.methodBuilder("slice")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(int.class, "offset")
            .addParameter(int.class, "length")
            .returns(className)
            .addStatement("$T.fromIndexSize(offset, length, $L)", CHECK_CLASS, length())
            .addStatement("return new $L(array, this.offset + $L, $L)",
                className, adjust("offset"), adjust("length"))
            .build();
    }

    // endregion

    private MethodSpec generateWrap1(ClassName className) {
        return MethodSpec.methodBuilder("wrap")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(arrayType, "array")
            .returns(className)
            .addStatement("return wrap(array, 0, array.length)", className)
            .build();
    }

    private MethodSpec generateWrap3(ClassName className) {
        var builder = MethodSpec.methodBuilder("wrap")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(arrayType, "array")
            .addParameter(int.class, "offset")
            .addParameter(int.class, "length")
            .returns(className);

        if (type.isByte()) {
            builder.addStatement("return new $L(array, offset, length)", className);
        } else {
            builder.addStatement("byte[] buffer = new byte[$L]", adjust("length"));
            builder.addStatement("$T.wrap(buffer).order($T.LITTLE_ENDIAN).as$LBuffer().put(array, offset, length)",
                ByteBuffer.class, ByteOrder.class, shortPrimitiveName());
            builder.addStatement("return new $L(buffer, 0, buffer.length)", className);
        }
        return builder.build();
    }

    private String adjust(String value) {
        return type.isByte() ? value : "Math.multiplyExact(" + value + ", " + bytes() + ")";
    }

    private String bytes() {
        return boxedType.simpleName() + ".BYTES";
    }

    private String length() {
        return type.isByte() ? "this.length" : "length()";
    }

    private String getter(String index) {
        return type.isByte() ? "array[offset + " + index + "]" : "getInternal(" + index + ")";
    }

    private String shortPrimitiveName() {
        var typeString = primitiveType.toString();
        return Character.toUpperCase(typeString.charAt(0)) + typeString.substring(1);
    }

    private String varHandleName(Class<?> type) {
        return "VH_" + type.getSimpleName().toUpperCase();
    }

    private void writeClass(TypeSpec typeSpec) throws IOException {
        JavaFile
            .builder(PACKAGE_NAME, typeSpec)
            .indent("    ")
            .build()
            .writeTo(Path.of("src/main/java"));
    }
}
