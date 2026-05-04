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
    private static final ClassName BYTES_CLASS = ClassName.get(PACKAGE_NAME, "Bytes");
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

    private static void generateParent() throws IOException {
        writeClass(createInterface());
    }

    private void generate() throws IOException {
        writeClass(createWrapperClass());
    }

    private static TypeSpec createInterface() {
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
            .addAnnotation(AnnotationSpec.builder(Generated.class)
                .addMember("value", "$S", this.getClass().getName())
                .build())
            /*.addAnnotation(AnnotationSpec.builder(Debug.Renderer.class)
                .addMember("childrenArray", "$S", "java.util.Arrays.copyOfRange(array, offset, offset + length)")
                .build())*/;

        // Fields
        builder.addField(FieldSpec.builder(thisType, "EMPTY", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
            .initializer("new $T(new byte[0], 0, 0)", thisType)
            .build());

        addConstructors(builder);
        addFactories(builder);
        addGetters(builder);

        // List equivalent methods
        builder.addMethod(generateLength());
        builder.addMethod(generateContains());
        builder.addMethod(generateIndexOf());
        builder.addMethod(generateLastIndexOf());

        builder.addMethods(generateSliceMethods(thisType));
        builder.addMethod(generateCopyTo());
        builder.addMethods(generateConversions());
        addComparableMethods(builder);
        addObjectMethods(builder);
        addMutableWrapperClass(builder);

        return builder.build();
    }

    private void addConstructors(TypeSpec.Builder builder) {
        builder.addMethod(MethodSpec.constructorBuilder()
            .addParameter(byte[].class, "array")
            .addParameter(int.class, "offset")
            .addParameter(int.class, "length")
            .addStatement("super(array, offset, length)", CHECK_CLASS)
            .build());
    }

    private void addFactories(TypeSpec.Builder builder) {
        builder.addMethod(MethodSpec.methodBuilder("empty")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(thisType)
            .addStatement("return EMPTY")
            .build());

        builder.addMethod(generateWrap1(thisType));
        builder.addMethod(generateWrap3(thisType));

        builder.addMethod(MethodSpec.methodBuilder("allocate")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(int.class, "length")
            .returns(mutableType)
            .addStatement("int byteLength = Math.multiplyExact(length, $L)", bytes())
            .addStatement("return new $L(new byte[byteLength], 0, byteLength)", mutableType)
            .build());

        builder.addMethod(MethodSpec.methodBuilder("from")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(bufferType, "buffer")
            .returns(thisType)
            .addStatement("$T.argument(buffer.hasArray(), \"buffer must be backed by an array\")", CHECK_CLASS)
            .addStatement("return wrap(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining())", thisType)
            .build());
    }

    private void addGetters(TypeSpec.Builder builder) {
        builder.addMethod(generateGet());
        builder.addMethod(generateGetInternal());

        // Add extra methods for Primitives
        if (type == SliceType.Bytes) {
            builder.addMethod(generateGet(short.class, "getShort", "Short.BYTES"));
            builder.addMethod(generateGet(int.class, "getInt", "Integer.BYTES"));
            builder.addMethod(generateGet(long.class, "getLong", "Long.BYTES"));
            builder.addMethod(generateGet(float.class, "getFloat", "Float.BYTES"));
            builder.addMethod(generateGet(double.class, "getDouble", "Double.BYTES"));
            builder.addMethod(generateGetUnsigned(int.class, "getUnsigned", "get", "Byte.toUnsignedInt"));
            builder.addMethod(generateGetUnsigned(int.class, "getUnsignedShort", "getShort", "Short.toUnsignedInt"));
            builder.addMethod(generateGetUnsigned(long.class, "getUnsignedInt", "getInt", "Integer.toUnsignedLong"));
        } else if (type == SliceType.Shorts) {
            builder.addMethod(generateGetUnsigned(int.class, "getUnsigned", "get", "Short.toUnsignedInt"));
        } else if (type == SliceType.Ints) {
            builder.addMethod(generateGetUnsigned(long.class, "getUnsigned", "get", "Integer.toUnsignedLong"));
        }
    }

    private MethodSpec generateGet() {
        return MethodSpec.methodBuilder("get")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(int.class, "index")
            .returns(type.primitiveType())
            .addStatement("$T.index(index, length)", CHECK_CLASS)
            .addStatement("return getInternal(index)")
            .build();
    }

    private MethodSpec generateGetInternal() {
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
            .beginControlFlow("if (" + JavaPoetUtils.primitiveEquals("getInternal(i)", "value", primitiveType) + ")")
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
            .beginControlFlow("if (" + JavaPoetUtils.primitiveEquals("getInternal(i)", "value", primitiveType) + ")")
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
            .addStatement("$T.fromIndexSize($L, length, target.length)", CHECK_CLASS, adjust("offset"))
            .addStatement("System.arraycopy(array, this.offset, target.array, target.offset + $L, length)", adjust("offset"))
            .build();
    }

    private ArrayList<MethodSpec> generateConversions() {
        var result = new ArrayList<MethodSpec>();
        result.add(generateAsBuffer());

        if (type.isByte()) {
            result.add(generateAsInputStream());
        }

        result.add(generateStream());
        result.add(generateToArray());

        if (type.isByte()) {
            result.add(generateToHexStringWithFormat());
            result.add(generateToStringWithCharset());
        }
        return result;
    }

    private MethodSpec generateAsBuffer() {
        return JavaPoetUtils.override("asBuffer")
            .returns(bufferType)
            .addStatement("return asByteBuffer()$L.slice().asReadOnlyBuffer()",
                type.isByte() ? "" : ".as" + shortPrimitiveName() + "Buffer()")
            .build();
    }

    private static MethodSpec generateAsInputStream() {
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

    private static MethodSpec generateToHexStringWithFormat() {
        return MethodSpec.methodBuilder("toHexString")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(HexFormat.class, "format")
            .returns(String.class)
            .addStatement("return format.formatHex(array, offset, offset + length)")
            .build();
    }

    private static MethodSpec generateToStringWithCharset() {
        return MethodSpec.methodBuilder("toString")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(Charset.class, "charset")
            .returns(String.class)
            .addStatement("return new String(array, offset, length, charset)")
            .build();
    }

    private void addComparableMethods(TypeSpec.Builder builder) {
        JavaPoetUtils.implementComparable(builder, thisType, methodBuilder -> {
            if (type.isByte()) {
                methodBuilder.addStatement("return $T.compare(array, offset, offset + length, o.array, o.offset, o.offset + o.length)", Arrays.class);
            } else {
                methodBuilder.addStatement("int min = Math.min(length(), o.length())");
                methodBuilder.beginControlFlow("for (int i = 0; i < min; i++)");
                methodBuilder.addStatement("int c = $T.compare(getInternal(i), o.getInternal(i))", boxedType);
                methodBuilder.beginControlFlow("if (c != 0)");
                methodBuilder.addStatement("return c");
                methodBuilder.endControlFlow();
                methodBuilder.endControlFlow();
                methodBuilder.addStatement("return Integer.compare(length(), o.length())");
            }
        });
    }

    private void addObjectMethods(TypeSpec.Builder builder) {
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

        builder.addMethod(JavaPoetUtils.equalsBuilder("obj")
            .beginControlFlow("if (obj == this)")
            .addStatement("return true")
            .endControlFlow()
            .beginControlFlow("if (!(obj instanceof $L o))", thisType)
            .addStatement("return false")
            .endControlFlow()
            .addCode("$L", equalsBlock)
            .build());

        builder.addMethod(JavaPoetUtils.hashCodeBuilder()
            .addStatement("int result = 1")
            .beginControlFlow("for (int i = 0, len = length(); i < len; i++)")
            .addStatement("result = 31 * result + $T.hashCode(getInternal(i))", boxedType)
            .endControlFlow()
            .addStatement("return result")
            .build());

        builder.addMethod(JavaPoetUtils.toStringBuilder()
            .addStatement("return $S + $L + $S", "[", length(), " " + primitiveType.toString() + "s]")
            .build());
    }

    private void addMutableWrapperClass(TypeSpec.Builder builder) {
        builder.addType(createMutableWrapperClass());
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

        builder.addMethods(generateSliceMethods(mutableType));
        addMutableBulkMethods(builder);
        addMutableConversions(builder);

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

    //    private MethodSpec generateGet() {
//        return MethodSpec.methodBuilder("get")
//            .addModifiers(Modifier.PUBLIC)
//            .addParameter(int.class, "index")
//            .returns(primitiveType)
//            .addStatement("$T.index(index, length)", CHECK_CLASS)
//            .addStatement("return getInternal(index)")
//            .build();
//    }
//
//    private MethodSpec generateGetInternal() {
//        var builder = MethodSpec.methodBuilder("getInternal")
//            .addModifiers(Modifier.PRIVATE)
//            .addParameter(int.class, "index")
//            .returns(primitiveType);
//
//        if (type.isByte()) {
//            builder.addStatement("return array[offset + index]");
//        } else {
//            builder.addStatement("return ($T) $L.get(array, offset + $L)",
//                primitiveType, varHandleName(primitiveType), adjust("index"));
//        }
//
//        return builder.build();
//    }


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
            .addModifiers(Modifier.PUBLIC)
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

    private void addMutableBulkMethods(TypeSpec.Builder builder) {
        builder.addMethod(MethodSpec.methodBuilder("copyFrom")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(arrayType, "src")
            .returns(mutableType)
            .addStatement("return copyFrom(src, 0, src.length)")
            .build());

        builder.addMethod(MethodSpec.methodBuilder("copyFrom")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(arrayType, "src")
            .addParameter(int.class, "offset")
            .addParameter(int.class, "length")
            .returns(mutableType)
            .addStatement("$T.fromIndexSize(offset, length, src.length)", CHECK_CLASS)
            .addStatement("$T.fromIndexSize(0, length, $L)", CHECK_CLASS, length())
            .addStatement("asByteBuffer()$L.put(src, offset, length)", type.isByte() ? "" : ".as" + shortPrimitiveName() + "Buffer()")
            .addStatement("return this")
            .build());

        builder.addMethod(MethodSpec.methodBuilder("copyWithin")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(int.class, "srcIndex")
            .addParameter(int.class, "dstIndex")
            .addParameter(int.class, "length")
            .returns(mutableType)
            .addStatement("copyWithinBytes($L, $L, $L)", adjust("srcIndex"), adjust("dstIndex"), adjust("length"))
            .addStatement("return this")
            .build());

        builder.addMethod(generateFill());

        builder.addMethod(generateFillFrom());
    }

    private MethodSpec generateFill() {
        var builder = MethodSpec.methodBuilder("fill")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(primitiveType, "value")
            .returns(mutableType);

        if (type.isByte()) {
            builder.addStatement("$T.fill(array, offset, offset + length, value)", Arrays.class);
        } else /*if(primitiveType == short.class || primitiveType == int.class || primitiveType == long.class) {
            var multiplier = new StringBuilder("0x");
            multiplier.repeat("01", primitiveSize);
            multiplier.append(primitiveType == long.class ? "L" : "");
            builder.beginControlFlow("if (value == (value & 0xFF) * $L)", multiplier);
            builder.addStatement("$T.fill(array, offset, offset + length, value)", Arrays.class);
            builder.nextControlFlow("else");
            builder.endControlFlow();
        }*/ {
            builder.beginControlFlow("for (int i = 0; i < length(); i++)");
            builder.addStatement("setInternal(i, value)");
            builder.endControlFlow();
        }

        return builder
            .addStatement("return this")
            .build();
    }

    private MethodSpec generateFillFrom() {
        var builder = MethodSpec.methodBuilder("fillFrom")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(BinarySource.class, "source")
            .addException(IOException.class)
            .returns(mutableType)
            .addStatement("source.readBytes(new Bytes.Mutable(array, offset, length))");

        if (primitiveType != byte.class) {
            builder
                .beginControlFlow("if (source.order() == $T.BIG_ENDIAN)", ByteOrder.class)
                .beginControlFlow("for (int i = 0, len = length(); i < len; i++)");

            if (primitiveType == float.class) {
                builder.addStatement("setInternal(i, Float.intBitsToFloat(Integer.reverseBytes(Float.floatToRawIntBits(getInternal(i)))))");
            } else if (primitiveType == double.class) {
                builder.addStatement("setInternal(i, Double.longBitsToDouble(Long.reverseBytes(Double.doubleToRawLongBits(getInternal(i)))))");
            } else {
                builder.addStatement("setInternal(i, $T.reverseBytes(getInternal(i)))", boxedType);
            }
            builder
                .endControlFlow()
                .endControlFlow();
        }
        return builder.addStatement("return this").build();
    }

    private void addMutableConversions(TypeSpec.Builder builder) {
        builder.addMethod(MethodSpec.methodBuilder("asMutableBuffer")
            .addModifiers(Modifier.PUBLIC)
            .returns(bufferType)
            .addStatement("return asByteBuffer()$L.slice()", type.isByte() ? "" : ".as" + shortPrimitiveName() + "Buffer()")
            .build());
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

    private List<MethodSpec> generateSliceMethods(ClassName className) {
        var sliceWithOffset = MethodSpec.methodBuilder("slice")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(int.class, "offset")
            .returns(className)
            .addStatement("return slice(offset, $L - offset)", length())
            .build();

        var sliceWithOffsetAndLength = MethodSpec.methodBuilder("slice")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(int.class, "offset")
            .addParameter(int.class, "length")
            .returns(className)
            .addStatement("$T.fromIndexSize(offset, length, $L)", CHECK_CLASS, length())
            .addStatement("return new $L(array, this.offset + $L, $L)",
                className, adjust("offset"), adjust("length"))
            .build();

        return List.of(
            sliceWithOffset,
            sliceWithOffsetAndLength
        );
    }

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
        return type.isByte() ? value : value + " * " + bytes();
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

    private static void writeClass(TypeSpec typeSpec) throws IOException {
        JavaFile
            .builder(PACKAGE_NAME, typeSpec)
            .indent("    ")
            .build()
            .writeTo(Path.of("src/main/java"));
    }
}
