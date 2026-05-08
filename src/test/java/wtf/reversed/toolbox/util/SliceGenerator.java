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

    public static void main(String[] args) throws Exception {
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
        builder.addMethod(generateWrapCopyOfArray1(thisType));
        builder.addMethod(generateWrapCopyOfArray3(thisType));
        builder.addMethod(generateWrapCopyOfBuffer());
        builder.addMethod(generateAllocate());

        builder.addMethod(generateGet());
        if (type.isByte()) builder.addMethods(generateByteOnlyAccessors());
        builder.addMethods(generateUnsignedGetters());
        builder.addMethod(generateGetInternal());

        builder.addMethod(generateLength());
        builder.addMethod(generateContains());
        builder.addMethod(generateIndexOf());
        builder.addMethod(generateLastIndexOf());

        builder.addMethod(generateAsBuffer());
        if (type.isByte()) builder.addMethod(generateAsBytesOverride());
        if (type.isByte()) builder.addMethods(generateAsTypes());
        if (type.isByte()) builder.addMethod(generateAsInputStream());
        builder.addMethod(generateCopyTo());
        builder.addMethod(generateCopyToArray1());
        builder.addMethod(generateCopyToArray3());
        builder.addMethod(generateSlice1(thisType));
        builder.addMethod(generateSlice2(thisType));
        builder.addMethod(generateStream());
        builder.addMethod(generateToArray());
        if (type.isByte()) builder.addMethod(generateToHexStringWithFormat());
        if (type.isByte()) builder.addMethod(generateToStringWithCharset());

        if (!type.isByte()) builder.addMethod(generateAsTypedBuffer());

        builder.addMethod(generateCompareTo());

        builder.addType(createMutableWrapperClass());

        return builder.build();
    }

    private TypeSpec createMutableWrapperClass() {
        var builder = TypeSpec.classBuilder(mutableType)
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .superclass(thisType);

        builder.addMethod(generateMutableConstructor());
        builder.addMethod(generateWrapCopyOfArray1(mutableType));
        builder.addMethod(generateWrapCopyOfArray3(mutableType));

        builder.addMethod(generateSet());
        if (type.isByte()) builder.addMethods(generateByteOnlyMutators());
        builder.addMethod(generateSetInternal());

        builder.addMethod(generateAsMutableBuffer());
        builder.addMethod(generateSlice1(mutableType));
        builder.addMethod(generateSlice2(mutableType));
        builder.addMethod(generateCopyFromArray1());
        builder.addMethod(generateCopyFromArray3());
        builder.addMethod(generateFill());
        builder.addMethod(generateFillFrom());
        if (type.isByte()) builder.addMethod(generateFillFromInputStream());

        return builder.build();
    }

    // region Constructors and factories

    private MethodSpec generateConstructor() {
        var builder = MethodSpec.constructorBuilder()
            .addParameter(byte[].class, "array")
            .addParameter(int.class, "offset")
            .addParameter(int.class, "length")
            .addStatement("super(array, offset, length)");
        if (!type.isByte()) {
            builder.addStatement("$T.argument((length & ($T.BYTES - 1)) == 0, $S)",
                CHECK_CLASS, boxedType, "length must be a multiple of " + (1 << type.primitiveShift()));
        }
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

    private MethodSpec generateEmpty() {
        return MethodSpec.methodBuilder("empty")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(thisType)
            .addStatement("return EMPTY")
            .build();
    }

    private MethodSpec generateWrapCopyOfArray1(ClassName className) {
        return MethodSpec.methodBuilder(factoryName())
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(arrayType, "array")
            .returns(className)
            .addStatement("return $L(array, 0, array.length)", factoryName())
            .build();
    }

    private MethodSpec generateWrapCopyOfArray3(ClassName className) {
        var builder = MethodSpec.methodBuilder(factoryName())
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

    private MethodSpec generateWrapCopyOfBuffer() {
        return MethodSpec.methodBuilder(factoryName())
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(bufferType, "buffer")
            .returns(thisType)
            .addStatement("$T.argument(buffer.hasArray(), \"buffer must be backed by an array\")", CHECK_CLASS)
            .addStatement("return $L(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining())",
                factoryName())
            .build();
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

    private String factoryName() {
        return type.isByte() ? "wrap" : "copyOf";
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

    private List<MethodSpec> generateByteOnlyAccessors() {
        return Arrays.stream(SliceType.values())
            .filter(value -> !value.isByte())
            .map(this::generateByteOnlyAccessor)
            .toList();
    }

    private MethodSpec generateByteOnlyAccessor(SliceType valueType) {
        return MethodSpec.methodBuilder("get" + valueType.capitalizedPrimitiveName())
            .addModifiers(Modifier.PUBLIC)
            .addParameter(int.class, "offset")
            .returns(valueType.primitiveType())
            .addStatement("$T.fromIndexSize(offset, $T.BYTES, length)", CHECK_CLASS, valueType.boxedType())
            .addStatement("return ($T) $L.get(array, this.offset + offset)", valueType.primitiveType(), valueType.varHandleName(ByteOrder.LITTLE_ENDIAN))
            .build();
    }

    private List<MethodSpec> generateUnsignedGetters() {
        return switch (type) {
            case Bytes -> List.of(
                generateGetUnsigned(int.class, "getUnsigned", "get", Byte.class, "toUnsignedInt"),
                generateGetUnsigned(int.class, "getUnsignedShort", "getShort", Short.class, "toUnsignedInt"),
                generateGetUnsigned(long.class, "getUnsignedInt", "getInt", Integer.class, "toUnsignedLong")
            );
            case Shorts -> List.of(generateGetUnsigned(int.class, "getUnsigned", "get", Short.class, "toUnsignedInt"));
            case Ints ->
                List.of(generateGetUnsigned(long.class, "getUnsigned", "get", Integer.class, "toUnsignedLong"));
            default -> List.of();
        };
    }

    private MethodSpec generateGetUnsigned(Class<?> returnType, String name, String accessor, Class<?> converterClass, String converterMethod) {
        return MethodSpec.methodBuilder(name)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(int.class, "offset")
            .returns(returnType)
            .addStatement("return $T.$L($L(offset))", converterClass, converterMethod, accessor)
            .build();
    }

    private MethodSpec generateGetInternal() {
        // Package-private rather than private because the Mutable subtype needs it.
        var builder = MethodSpec.methodBuilder("getInternal")
            .addParameter(int.class, "index")
            .returns(primitiveType);

        if (type.isByte()) {
            builder.addStatement("return array[offset + index]");
        } else {
            builder.addStatement("return ($T) $L.get(array, offset + $L)",
                primitiveType, type.varHandleName(ByteOrder.LITTLE_ENDIAN), toByteOffsetUnchecked("index"));
        }
        return builder.build();
    }

    // endregion

    // region List-equivalent methods

    private MethodSpec generateLength() {
        var builder = override("length")
            .returns(int.class);
        if (type.isByte()) {
            builder.addStatement("return length");
        } else {
            builder.addStatement("return length >>> $L", type.primitiveShift());
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
        MethodSpec.Builder builder = MethodSpec.methodBuilder("indexOf")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(primitiveType, "value")
            .returns(int.class);

        forEachElement(builder, CodeBlock.builder()
            .beginControlFlow("if ($L)", generateEquals("getInternal(i)", "value", type))
            .addStatement("return i")
            .endControlFlow()
            .build());

        return builder
            .addStatement("return -1")
            .build();
    }

    private MethodSpec generateLastIndexOf() {
        return MethodSpec.methodBuilder("lastIndexOf")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(primitiveType, "value")
            .returns(int.class)
            .beginControlFlow("for (int i = $L - 1; i >= 0; i--)", elementCount())
            .beginControlFlow("if ($L)", generateEquals("getInternal(i)", "value", type))
            .addStatement("return i")
            .endControlFlow()
            .endControlFlow()
            .addStatement("return -1")
            .build();
    }

    public static CodeBlock generateEquals(String left, String right, SliceType type) {
        return switch (type) {
            case Bytes, Shorts, Ints, Longs -> CodeBlock.of("$L == $L", left, right);
            case Floats ->
                CodeBlock.of("$T.floatToRawIntBits($L) == $T.floatToRawIntBits($L)", Float.class, left, Float.class, right);
            case Doubles ->
                CodeBlock.of("$T.doubleToRawLongBits($L) == $T.doubleToRawLongBits($L)", Double.class, left, Double.class, right);
        };
    }

    // endregion

    // region Mutable Accessors

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
                type.varHandleName(ByteOrder.LITTLE_ENDIAN), toByteOffsetUnchecked("index"));
        }
        return builder
            .addStatement("return this")
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

    // endregion

    // region Views, conversions, copies

    private MethodSpec generateAsBytesOverride() {
        return override("asBytes")
            .returns(thisType)
            .addStatement("return this")
            .build();
    }

    private List<MethodSpec> generateAsTypes() {
        return Arrays.stream(SliceType.values())
            .filter(t -> !t.isByte())
            .map(this::generateAsTypeOverride)
            .toList();
    }

    private MethodSpec generateAsTypeOverride(SliceType type) {
        return MethodSpec.methodBuilder("as" + type.name())
            .addModifiers(Modifier.PUBLIC)
            .returns(ClassName.get("", type.typeName()))
            .addStatement("return new $L(array, offset, length)", type.typeName())
            .build();
    }

    private MethodSpec generateAsBuffer() {
        var method = type.isByte() ? "asByteBuffer" : "asTypedBuffer";
        return override("asBuffer")
            .returns(bufferType)
            .addStatement("return $L().slice().asReadOnlyBuffer()", method)
            .build();
    }

    private MethodSpec generateAsInputStream() {
        return MethodSpec.methodBuilder("asInputStream")
            .addModifiers(Modifier.PUBLIC)
            .returns(InputStream.class)
            .addStatement("return new $T(array, offset, length)", ByteArrayInputStream.class)
            .build();
    }


    private MethodSpec generateCopyTo() {
        return MethodSpec.methodBuilder("copyTo")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(mutableType, "target")
            .addParameter(int.class, "offset")
            .returns(void.class)
            .addStatement("$T.fromIndexSize(offset, length(), target.length())", CHECK_CLASS)
            .addStatement("System.arraycopy(array, this.offset, target.array, target.offset + $L, length)", toByteOffset("offset"))
            .build();
    }

    private MethodSpec generateCopyToArray1() {
        return MethodSpec.methodBuilder("copyTo")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(arrayType, "target")
            .returns(void.class)
            .addStatement("copyTo(target, 0, $L)", elementCount())
            .build();
    }

    private MethodSpec generateCopyToArray3() {
        var builder = MethodSpec.methodBuilder("copyTo")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(arrayType, "target")
            .addParameter(int.class, "offset")
            .addParameter(int.class, "length")
            .returns(void.class)
            .addStatement("$T.fromIndexSize(offset, length, target.length)", CHECK_CLASS)
            .addStatement("$T.fromIndexSize(0, length, $L)", CHECK_CLASS, elementCount());

        if (type.isByte()) {
            builder.addStatement("System.arraycopy(array, this.offset, target, offset, length)");
        } else {
            builder.addStatement("asTypedBuffer().get(target, offset, length)");
        }
        return builder.build();
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
        return MethodSpec.methodBuilder("toArray")
            .addModifiers(Modifier.PUBLIC).returns(arrayType)
            .addStatement("$T result = new $T[length()]", arrayType, primitiveType)
            .addStatement("copyTo(result)")
            .addStatement("return result")
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

    // endregion

    // region Generated Helpers

    private MethodSpec generateAsTypedBuffer() {
        // Package-private (no modifier) rather than private: the nested static
        // Mutable inherits this helper, and private members aren't inherited.
        return MethodSpec.methodBuilder("asTypedBuffer")
            .returns(bufferType)
            .addStatement("return asByteBuffer().as$LBuffer()", type.capitalizedPrimitiveName())
            .build();
    }

    // endregion

    // region Object overrides

    private MethodSpec generateCompareTo() {
        var builder = override("compareTo")
            .returns(int.class)
            .addParameter(thisType, "o");

        if (type.isByte()) {
            builder.addStatement("return $T.compare(array, offset, offset + length, o.array, o.offset, o.offset + o.length)", Arrays.class);
        } else {
            builder.addStatement("int prefix = Math.min(length, o.length)");
            builder.addStatement("int mismatch = $T.mismatch(array, offset, offset + prefix, o.array, o.offset, o.offset + prefix)", Arrays.class);
            builder.beginControlFlow("if (mismatch < 0)");
            builder.addStatement("return Integer.compare(length(), o.length())");
            builder.endControlFlow();
            builder.addStatement("int idx = mismatch >>> $L", type.primitiveShift());
            if (primitiveType == float.class) {
                builder.addStatement("return Integer.compare($T.floatToRawIntBits(getInternal(idx)), $T.floatToRawIntBits(o.getInternal(idx)))", Float.class, Float.class);
            } else if (primitiveType == double.class) {
                builder.addStatement("return Long.compare($T.doubleToRawLongBits(getInternal(idx)), $T.doubleToRawLongBits(o.getInternal(idx)))", Double.class, Double.class);
            } else {
                builder.addStatement("return $T.compare(getInternal(idx), o.getInternal(idx))", boxedType);
            }
        }
        return builder.build();
    }

    // endregion

    // region Mutable methods

    private List<MethodSpec> generateByteOnlyMutators() {
        return Arrays.stream(SliceType.values())
            .filter(t -> !t.isByte())
            .map(this::generateTypedSet)
            .toList();
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

    private MethodSpec generateAsMutableBuffer() {
        var method = type.isByte() ? "asByteBuffer" : "asTypedBuffer";
        return MethodSpec.methodBuilder("asMutableBuffer")
            .addModifiers(Modifier.PUBLIC)
            .returns(bufferType)
            .addStatement("return $L().slice()", method)
            .build();
    }

    private MethodSpec generateCopyFromArray1() {
        return MethodSpec.methodBuilder("copyFrom")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(arrayType, "source")
            .returns(mutableType)
            .addStatement("return copyFrom(source, 0, source.length)")
            .build();
    }

    private MethodSpec generateCopyFromArray3() {
        var builder = MethodSpec.methodBuilder("copyFrom")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(arrayType, "source")
            .addParameter(int.class, "offset")
            .addParameter(int.class, "length")
            .returns(mutableType)
            .addStatement("$T.fromIndexSize(offset, length, source.length)", CHECK_CLASS)
            .addStatement("$T.fromIndexSize(0, length, $L)", CHECK_CLASS, elementCount());

        if (type.isByte()) {
            builder.addStatement("System.arraycopy(source, offset, array, this.offset, length)");
        } else {
            builder.addStatement("asTypedBuffer().put(source, offset, length)");
        }
        return builder
            .addStatement("return this")
            .build();
    }

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
            forEachElement(builder, CodeBlock.of("setInternal(i, value);"));
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
                primitiveType, type.varHandleName(ByteOrder.BIG_ENDIAN), toByteOffsetUnchecked("i")));
            builder.endControlFlow();
        }
        return builder.addStatement("return this").build();
    }

    private MethodSpec generateFillFromInputStream() {
        return MethodSpec.methodBuilder("fillFrom")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(InputStream.class, "in")
            .addException(IOException.class)
            .returns(mutableType)
            .addStatement("int read = in.readNBytes(array, offset, length)")
            .beginControlFlow("if (read != length)")
            .addStatement("throw new $T(\"Expected \" + length + \" bytes, got \" + read)", IOException.class)
            .endControlFlow()
            .addStatement("return this")
            .build();
    }

    // endregion

    // region Helpers

    private CodeBlock toByteOffset(String elementExpr) {
        if (type.isByte()) {
            return CodeBlock.of("$L", elementExpr);
        }
        return CodeBlock.of("Math.multiplyExact($L, $T.BYTES)", elementExpr, boxedType);
    }

    private CodeBlock toByteOffsetUnchecked(String elementExpr) {
        if (type.isByte()) {
            return CodeBlock.of("$L", elementExpr);
        }
        return CodeBlock.of("$L * $T.BYTES", elementExpr, boxedType);
    }

    private CodeBlock elementCount() {
        return CodeBlock.of(type.isByte() ? "this.length" : "length()");
    }

    private void forEachElement(MethodSpec.Builder builder, CodeBlock body) {
        builder
            .beginControlFlow("for (int i = 0, len = $L; i < len; i++)", elementCount())
            .addCode(body)
            .endControlFlow();
    }

    public MethodSpec.Builder override(String name) {
        return MethodSpec.methodBuilder(name)
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Override.class);
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
