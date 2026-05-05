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

    SliceGenerator(SliceType type) {
        this.type = type;
        this.thisType = ClassName.get("", type.typeName());
        this.mutableType = ClassName.get("", "Mutable");
        this.primitiveType = type.primitiveType();
        this.arrayType = ArrayTypeName.of(TypeName.get(primitiveType));
        this.boxedType = ClassName.get(type.boxedType());
        this.bufferType = ClassName.get(type.bufferType());
    }

    static void main() throws Exception {
        for (var t : SliceType.values()) {
            new SliceGenerator(t).generate();
        }
    }

    private void generate() throws IOException {
        writeClass(createWrapperClass());
    }

    private TypeSpec createWrapperClass() {
        var builder = TypeSpec.classBuilder(thisType)
            .addModifiers(Modifier.PUBLIC, Modifier.SEALED)
            .superclass(PARENT_CLASS)
            .addSuperinterface(ParameterizedTypeName.get(ClassName.get(Comparable.class), thisType))
            .addAnnotation(AnnotationSpec.builder(Generated.class)
                .addMember("value", "$S", this.getClass().getName())
                .build());

        builder.addField(FieldSpec.builder(thisType, "EMPTY", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
            .initializer("new $T(EMPTY_ARRAY, 0, 0)", thisType)
            .build());

        builder.addMethod(generateConstructor());
        builder.addMethod(generateEmpty());
        builder.addMethod(generateWrap1(thisType));
        builder.addMethod(generateWrap3(thisType));
        builder.addMethod(generateAllocate());
        builder.addMethod(generateFrom());

        builder.addMethod(generateGet());
        builder.addMethod(generateGetInternal());
        if (type.isByte()) {
            addByteOnlyAccessors(builder);
        }
        addUnsignedGetters(builder);

        builder.addMethod(generateLength());
        builder.addMethod(generateContains());
        builder.addMethod(generateIndexOf());
        builder.addMethod(generateLastIndexOf());

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
            addByteOnlyMutators(builder);
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

    // region Byte-only methods

    private void addByteOnlyAccessors(TypeSpec.Builder builder) {
        for (var t : new SliceType[]{SliceType.Shorts, SliceType.Ints, SliceType.Longs, SliceType.Floats, SliceType.Doubles}) {
            builder.addMethod(generateTypedGet(t));
        }
    }

    private void addUnsignedGetters(TypeSpec.Builder builder) {
        switch (type) {
            case Bytes -> {
                builder.addMethod(generateGetUnsigned(int.class, "getUnsigned", "get", Byte.class, "toUnsignedInt"));
                builder.addMethod(generateGetUnsigned(int.class, "getUnsignedShort", "getShort", Short.class, "toUnsignedInt"));
                builder.addMethod(generateGetUnsigned(long.class, "getUnsignedInt", "getInt", Integer.class, "toUnsignedLong"));
            }
            case Shorts ->
                builder.addMethod(generateGetUnsigned(int.class, "getUnsigned", "get", Short.class, "toUnsignedInt"));
            case Ints ->
                builder.addMethod(generateGetUnsigned(long.class, "getUnsigned", "get", Integer.class, "toUnsignedLong"));
            default -> {
            }
        }
    }

    private void addByteOnlyMutators(TypeSpec.Builder builder) {
        for (var t : new SliceType[]{SliceType.Shorts, SliceType.Ints, SliceType.Longs, SliceType.Floats, SliceType.Doubles}) {
            builder.addMethod(generateTypedSet(t));
        }
    }

    private MethodSpec generateTypedGet(SliceType valueType) {
        return MethodSpec.methodBuilder("get" + valueType.capitalizedPrimitiveName())
            .addModifiers(Modifier.PUBLIC)
            .addParameter(int.class, "offset")
            .returns(valueType.primitiveType())
            .addStatement("$T.fromIndexSize(offset, $T.BYTES, length)", CHECK_CLASS, valueType.boxedType())
            .addStatement("return ($T) $L.get(array, this.offset + offset)", valueType.primitiveType(), valueType.varHandleName(ByteOrder.LITTLE_ENDIAN))
            .build();
    }

    private MethodSpec generateTypedSet(SliceType valueType) {
        return MethodSpec.methodBuilder("set" + valueType.capitalizedPrimitiveName())
            .addModifiers(Modifier.PUBLIC)
            .addParameter(int.class, "offset")
            .addParameter(valueType.primitiveType(), "value")
            .returns(mutableType)
            .addStatement("$T.fromIndexSize(offset, $T.BYTES, this.length)", CHECK_CLASS, valueType.boxedType())
            .addStatement("$L.set(array, this.offset + offset, value)", valueType.varHandleName(ByteOrder.LITTLE_ENDIAN))
            .addStatement("return this")
            .build();
    }

    private MethodSpec generateGetUnsigned(Class<?> returnType, String name, String accessor, Class<?> converterClass, String converterMethod) {
        return MethodSpec.methodBuilder(name)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(int.class, "offset")
            .returns(returnType)
            .addStatement("return $T.$L($L(offset))", converterClass, converterMethod, accessor)
            .build();
    }

    // endregion

    // region Constructors and factories

    private MethodSpec generateConstructor() {
        return MethodSpec.constructorBuilder()
            .addParameter(byte[].class, "array")
            .addParameter(int.class, "offset")
            .addParameter(int.class, "length")
            .addStatement("super(array, offset, length)")
            .build();
    }

    private MethodSpec generateMutableConstructor() {
        return MethodSpec.constructorBuilder()
            .addParameter(byte[].class, "array")
            .addParameter(int.class, "offset")
            .addParameter(int.class, "length")
            .addStatement("super(array, offset, length)")
            .build();
    }

    private MethodSpec generateEmpty() {
        return MethodSpec.methodBuilder("empty")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(thisType)
            .addStatement("return EMPTY")
            .build();
    }

    private MethodSpec generateWrap1(ClassName className) {
        return MethodSpec.methodBuilder("wrap")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(arrayType, "array")
            .returns(className)
            .addStatement("return wrap(array, 0, array.length)")
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
            builder.addStatement("byte[] buffer = new byte[$L]", toByteOffset("length"));
            builder.addStatement("$T.wrap(buffer).order($T.LITTLE_ENDIAN).as$LBuffer().put(array, offset, length)",
                ByteBuffer.class, ByteOrder.class, type.capitalizedPrimitiveName());
            builder.addStatement("return new $L(buffer, 0, buffer.length)", className);
        }
        return builder.build();
    }

    private MethodSpec generateAllocate() {
        return MethodSpec.methodBuilder("allocate")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(int.class, "length")
            .returns(mutableType)
            .addStatement("int byteLength = $L", toByteOffset("length"))
            .addStatement("return new $L(new byte[byteLength], 0, byteLength)", mutableType)
            .build();
    }

    private MethodSpec generateFrom() {
        return MethodSpec.methodBuilder("from")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(bufferType, "buffer")
            .returns(thisType)
            .addStatement("$T.argument(buffer.hasArray(), \"buffer must be backed by an array\")", CHECK_CLASS)
            .addStatement("return wrap(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining())")
            .build();
    }

    // endregion

    // region Accessors

    private MethodSpec generateGet() {
        return MethodSpec.methodBuilder("get")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(int.class, "index")
            .returns(primitiveType)
            .addStatement("$T.index(index, $L)", CHECK_CLASS, elementCount())
            .addStatement("return getInternal(index)")
            .build();
    }

    private MethodSpec generateGetInternal() {
        // Package-private rather than private because the Mutable subtype reads through it.
        var builder = MethodSpec.methodBuilder("getInternal")
            .addParameter(int.class, "index")
            .returns(primitiveType);

        if (type.isByte()) {
            builder.addStatement("return array[offset + index]");
        } else {
            builder.addStatement("return ($T) $L.get(array, offset + $L)",
                primitiveType, type.varHandleName(ByteOrder.LITTLE_ENDIAN), toByteOffset("index"));
        }
        return builder.build();
    }

    private MethodSpec generateSet() {
        return MethodSpec.methodBuilder("set")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(int.class, "index")
            .addParameter(primitiveType, "value")
            .returns(mutableType)
            .addStatement("$T.index(index, $L)", CHECK_CLASS, elementCount())
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
                type.varHandleName(ByteOrder.LITTLE_ENDIAN), toByteOffset("index"));
        }
        return builder
            .addStatement("return this")
            .build();
    }

    // endregion

    // region List-equivalent methods

    private MethodSpec generateLength() {
        var builder = JavaPoetUtils.override("length")
            .returns(int.class);
        if (type.isByte()) {
            builder.addStatement("return length");
        } else {
            builder.addStatement("return length >>> $L", Integer.numberOfTrailingZeros(type.primitiveSize()));
        }
        return builder.build();
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
            .beginControlFlow("for (int i = 0, limit = $L; i < limit; i++)", elementCount())
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
            .beginControlFlow("for (int i = $L - 1; i >= 0; i--)", elementCount())
            .beginControlFlow("if ($L)", JavaPoetUtils.primitiveEquals("getInternal(i)", "value", primitiveType))
            .addStatement("return i")
            .endControlFlow()
            .endControlFlow()
            .addStatement("return -1")
            .build();
    }

    // endregion

    // region Slice methods

    private MethodSpec generateSlice1(ClassName className) {
        return MethodSpec.methodBuilder("slice")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(int.class, "offset")
            .returns(className)
            .addStatement("return slice(offset, $L - offset)", elementCount())
            .build();
    }

    private MethodSpec generateSlice2(ClassName className) {
        return MethodSpec.methodBuilder("slice")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(int.class, "offset")
            .addParameter(int.class, "length")
            .returns(className)
            .addStatement("$T.fromIndexSize(offset, length, $L)", CHECK_CLASS, elementCount())
            .addStatement("return new $L(array, this.offset + $L, $L)",
                className, toByteOffset("offset"), toByteOffset("length"))
            .build();
    }

    private MethodSpec generateCopyTo() {
        return MethodSpec.methodBuilder("copyTo")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(mutableType, "target")
            .addParameter(int.class, "offset")
            .returns(void.class)
            .addStatement("$T.fromIndexSize(offset, length(), target.length())", CHECK_CLASS)
            .addStatement("System.arraycopy(array, this.offset, target.array, target.offset + $L, length)",
                toByteOffset("offset"))
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
            .addStatement("$T.fromIndexSize(0, length, $L)", CHECK_CLASS, elementCount());

        if (type.isByte()) {
            builder.addStatement("System.arraycopy(src, offset, array, this.offset, length)");
        } else {
            builder.addStatement("asByteBuffer().as$LBuffer().put(src, offset, length)", type.capitalizedPrimitiveName());
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
            .addStatement("copyWithinBytes($L, $L, $L)",
                toByteOffset("srcIndex"), toByteOffset("dstIndex"), toByteOffset("length"))
            .addStatement("return this")
            .build();
    }

    // endregion

    // region Buffers, streams, conversions

    private MethodSpec generateAsBuffer() {
        return JavaPoetUtils.override("asBuffer")
            .returns(bufferType)
            .addStatement("return asByteBuffer()$L.slice().asReadOnlyBuffer()", typedBufferConversion())
            .build();
    }

    private MethodSpec generateAsMutableBuffer() {
        return MethodSpec.methodBuilder("asMutableBuffer")
            .addModifiers(Modifier.PUBLIC)
            .returns(bufferType)
            .addStatement("return asByteBuffer()$L.slice()", typedBufferConversion())
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
        var streamType = switch (type) {
            case Bytes, Shorts, Ints -> IntStream.class;
            case Longs -> LongStream.class;
            case Floats, Doubles -> DoubleStream.class;
        };

        var streamMethod = switch (type) {
            case Bytes, Shorts, Ints -> "";
            case Longs -> "ToLong";
            case Floats, Doubles -> "ToDouble";
        };

        return MethodSpec.methodBuilder("stream")
            .addModifiers(Modifier.PUBLIC)
            .returns(streamType)
            .addStatement("return $T.range(0, $L).map$L(i -> getInternal(i))",
                IntStream.class, elementCount(), streamMethod)
            .build();
    }

    private MethodSpec generateToArray() {
        var builder = MethodSpec.methodBuilder("toArray")
            .addModifiers(Modifier.PUBLIC)
            .returns(arrayType);

        if (type.isByte()) {
            builder.addStatement("return $T.copyOfRange(array, offset, offset + length)", Arrays.class);
        } else {
            builder.addStatement("$T result = new $T[length()]", arrayType, primitiveType);
            builder.addStatement("asBuffer().get(result)");
            builder.addStatement("return result");
        }
        return builder.build();
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

    // endregion

    // region Object overrides

    private MethodSpec generateCompareTo() {
        var builder = JavaPoetUtils.override("compareTo")
            .returns(int.class)
            .addParameter(thisType, "o");

        if (type.isByte()) {
            builder.addStatement("return $T.compare(array, offset, offset + length, o.array, o.offset, o.offset + o.length)",
                Arrays.class);
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
        var builder = JavaPoetUtils.equalsBuilder("obj")
            .beginControlFlow("if (obj == this)")
            .addStatement("return true")
            .endControlFlow()
            .beginControlFlow("if (!(obj instanceof $L o))", thisType)
            .addStatement("return false")
            .endControlFlow();

        if (type.isIntegral()) {
            builder.addStatement(
                "return $T.equals(array, offset, offset + length, o.array, o.offset, o.offset + o.length)",
                Arrays.class);
        } else {
            builder.beginControlFlow("if (length() != o.length())")
                .addStatement("return false")
                .endControlFlow();
            forEachElement(builder, CodeBlock.builder()
                .beginControlFlow("if ($T.compare(getInternal(i), o.getInternal(i)) != 0)", boxedType)
                .addStatement("return false")
                .endControlFlow()
                .build());
            builder.addStatement("return true");
        }
        return builder.build();
    }

    private MethodSpec generateHashCode() {
        var builder = JavaPoetUtils.hashCodeBuilder()
            .addStatement("int result = 1");
        forEachElement(builder, CodeBlock.of(
            "result = 31 * result + $T.hashCode(getInternal(i));\n", boxedType));
        return builder.addStatement("return result").build();
    }

    private MethodSpec generateToString() {
        return JavaPoetUtils.toStringBuilder()
            .addStatement("return $S + $L + $S", "[", elementCount(), " " + primitiveType.toString() + "s]")
            .build();
    }

    // endregion

    // region Fill / FillFrom

    private MethodSpec generateFill() {
        var builder = MethodSpec.methodBuilder("fill")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(primitiveType, "value")
            .returns(mutableType);

        var fastPathCheck = switch (type) {
            case Bytes -> null;
            case Shorts -> CodeBlock.of("value == (value & 0xFF) * 0x0101");
            case Ints -> CodeBlock.of("value == (value & 0xFF) * 0x0101_0101");
            case Longs -> CodeBlock.of("value == (value & 0xFF) * 0x0101_0101_0101_0101L");
            case Floats -> CodeBlock.of("$T.floatToRawIntBits(value) == 0", Float.class);
            case Doubles -> CodeBlock.of("$T.doubleToRawLongBits(value) == 0L", Double.class);
        };

        var fillValue = switch (type) {
            case Bytes -> CodeBlock.of("value");
            case Shorts, Ints, Longs -> CodeBlock.of("(byte) value");
            case Floats, Doubles -> CodeBlock.of("(byte) 0");
        };

        if (fastPathCheck == null) {
            builder.addStatement("$T.fill(array, offset, offset + length, $L)", Arrays.class, fillValue);
        } else {
            builder.beginControlFlow("if ($L)", fastPathCheck);
            builder.addStatement("$T.fill(array, offset, offset + length, $L)", Arrays.class, fillValue);
            builder.nextControlFlow("else");
            builder.beginControlFlow("for (int i = 0; i < length(); i++)");
            builder.addStatement("setInternal(i, value)");
            builder.endControlFlow();
            builder.endControlFlow();
        }
        return builder.addStatement("return this").build();
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
            builder.addStatement("source.readBytes(new Bytes.Mutable(array, offset, length))")
                .beginControlFlow("if (source.order() == $T.BIG_ENDIAN)", ByteOrder.class);
            forEachElement(builder, CodeBlock.of(
                "setInternal(i, ($T) $L.get(array, offset + $L));\n",
                primitiveType, type.varHandleName(ByteOrder.BIG_ENDIAN), toByteOffset("i")));
            builder.endControlFlow();
        }
        return builder.addStatement("return this").build();
    }

    // endregion

    // region Helpers

    private CodeBlock toByteOffset(String elementExpr) {
        if (type.isByte()) {
            return CodeBlock.of("$L", elementExpr);
        }
        return CodeBlock.of("Math.multiplyExact($L, $T.BYTES)", elementExpr, boxedType);
    }

    private CodeBlock elementCount() {
        return CodeBlock.of(type.isByte() ? "this.length" : "length()");
    }

    private String typedBufferConversion() {
        return type.isByte() ? "" : ".as" + type.capitalizedPrimitiveName() + "Buffer()";
    }

    private void forEachElement(MethodSpec.Builder builder, CodeBlock body) {
        builder
            .beginControlFlow("for (int i = 0, len = $L; i < len; i++)", elementCount())
            .addCode(body)
            .endControlFlow();
    }

    private void writeClass(TypeSpec typeSpec) throws IOException {
        JavaFile
            .builder(PACKAGE_NAME, typeSpec)
            .indent("    ")
            .build()
            .writeTo(Path.of("src/main/java"));
    }

    // endregion
}
