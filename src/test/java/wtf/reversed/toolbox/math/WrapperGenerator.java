package wtf.reversed.toolbox.math;

import com.palantir.javapoet.*;

import javax.lang.model.element.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;

final class WrapperGenerator {
    private static final String PACKAGE_NAME = "wtf.reversed.toolbox.math";

    private static final List<Dimension> XY = List.of(Dimension.X, Dimension.Y);
    private static final List<Dimension> XYZ = List.of(Dimension.X, Dimension.Y, Dimension.Z);
    private static final List<Dimension> XYZW = List.of(Dimension.X, Dimension.Y, Dimension.Z, Dimension.W);

    private static final CodeBlock CODE_BLOCK_0_0F = CodeBlock.of("0.0f");
    private static final CodeBlock CODE_BLOCK_1_0F = CodeBlock.of("1.0f");

    public static void main(String[] args) throws IOException {
        generateVector("Vector2f", float.class, XY);
        generateVector("Vector3f", float.class, XYZ);
        generateVector("Vector4f", float.class, XYZW);
    }

    private static void generateVector(String name, Class<?> type, List<Dimension> dimensions) throws IOException {
        writeClass(createVectorRecord(ClassName.get("", name), TypeName.get(type), dimensions));
    }

    private static TypeSpec createVectorRecord(ClassName className, TypeName componentName, List<Dimension> dims) {
        var constructor = MethodSpec.constructorBuilder();
        for (Dimension dim : dims) {
            constructor.addParameter(componentName, dim.name);
        }

        var spec = TypeSpec.recordBuilder(className)
            .addModifiers(Modifier.PUBLIC)
            .recordConstructor(constructor.build());

        // static constants
        spec.addFields(createConstants(className, dims));

        // splat(value)
        spec.addMethod(createSplat(className, componentName, dims));

        // add(), subtract(), multiply(), divide()
        for (ArithmeticOperator operator : ArithmeticOperator.values()) {
            spec.addMethods(createOperator(className, componentName, dims, operator));
        }

        // negate()
        spec.addMethod(createNegate(className, dims));

        // dot()
        spec.addMethod(createDot(className, componentName, dims));

        // length(), lengthSquared()
        spec.addMethod(createLength(componentName));
        spec.addMethod(lengthSquared(componentName));

        // normalize()
        spec.addMethod(createNormalize(className));

        // fma()
        spec.addMethod(createFma(className, componentName, dims));

        return spec.build();
    }

    private static MethodSpec createFma(ClassName className, TypeName componentName, List<Dimension> dims) {
        var args = dims.stream()
            .map(dim -> CodeBlock.of("($T) $T.fma(this.$L, scale.$L, offset.$L)", componentName, Math.class, dim.name, dim.name, dim.name))
            .collect(CodeBlock.joining(",\n"));

        var code = CodeBlock.builder()
            .add("return new $T(\n", className)
            .indent().add(args).unindent()
            .add("\n);")
            .build();

        return MethodSpec.methodBuilder("fma")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(className, "scale")
            .addParameter(className, "offset")
            .returns(className)
            .addCode(code)
            .build();
    }

    private static MethodSpec lengthSquared(TypeName componentName) {
        return MethodSpec.methodBuilder("lengthSquared")
            .addModifiers(Modifier.PUBLIC)
            .returns(componentName)
            .addCode("return dot(this);")
            .build();
    }

    private static MethodSpec createNormalize(ClassName className) {
        return MethodSpec.methodBuilder("normalize")
            .addModifiers(Modifier.PUBLIC)
            .returns(className)
            .addCode("return $L(length());", ArithmeticOperator.DIV.name)
            .build();
    }

    private static MethodSpec createLength(TypeName componentName) {
        return MethodSpec.methodBuilder("length")
            .addModifiers(Modifier.PUBLIC)
            .returns(componentName)
            .addCode("return ($T) $T.sqrt(lengthSquared());", componentName, Math.class)
            .build();
    }

    private static MethodSpec createDot(ClassName className, TypeName componentName, List<Dimension> dims) {
        var args = dims.stream()
            .map(dim -> CodeBlock.of("this.$L * other.$L", dim.name, dim.name))
            .collect(CodeBlock.joining(" + "));

        var code = CodeBlock.builder()
            .add("return ").add(args).add(";")
            .build();

        return MethodSpec.methodBuilder("dot")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(className, "other")
            .returns(componentName)
            .addCode(code)
            .build();
    }

    private static MethodSpec createNegate(ClassName className, List<Dimension> dims) {
        var args = dims.stream()
            .map(dim -> CodeBlock.of("-this.$L", dim.name))
            .collect(CodeBlock.joining(", "));

        var code = CodeBlock.builder()
            .add("return new $T(", className).add(args).add(");")
            .build();

        return MethodSpec.methodBuilder("negate")
            .addModifiers(Modifier.PUBLIC)
            .returns(className)
            .addCode(code)
            .build();
    }

    private static Iterable<MethodSpec> createOperator(ClassName className, TypeName componentName, List<Dimension> dims, ArithmeticOperator operator) {
        return List.of(
            createInstanceOperator(className, dims, operator),
            createComponentOperator(className, componentName, dims, operator),
            createScalarOperator(className, componentName, dims, operator)
        );
    }

    private static MethodSpec createInstanceOperator(ClassName className, List<Dimension> dims, ArithmeticOperator operator) {
        var args = dims.stream()
            .map(dim -> CodeBlock.of("this.$L $L other.$L", dim.name, operator.symbol, dim.name))
            .collect(CodeBlock.joining(", "));

        var code = CodeBlock.builder()
            .add("return new $T(", className).add(args).add(");")
            .build();

        return MethodSpec.methodBuilder(operator.name)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(className, "other")
            .returns(className)
            .addCode(code)
            .build();
    }

    private static MethodSpec createComponentOperator(ClassName className, TypeName componentName, List<Dimension> dims, ArithmeticOperator operator) {
        var args = dims.stream()
            .map(dim -> CodeBlock.of("this.$L $L $L", dim.name, operator.symbol, dim.name))
            .collect(CodeBlock.joining(", "));

        var code = CodeBlock.builder()
            .add("return new $T(", className).add(args).add(");")
            .build();

        var params = dims.stream()
            .map(dim -> ParameterSpec.builder(componentName, dim.name).build())
            .toList();

        return MethodSpec.methodBuilder(operator.name)
            .addModifiers(Modifier.PUBLIC)
            .addParameters(params)
            .returns(className)
            .addCode(code)
            .build();
    }

    private static MethodSpec createScalarOperator(ClassName className, TypeName componentName, List<Dimension> dims, ArithmeticOperator operator) {
        var args = dims.stream()
            .map(dim -> CodeBlock.of("this.$L $L scalar", dim.name, operator.symbol))
            .collect(CodeBlock.joining(", "));

        var code = CodeBlock.builder()
            .add("return new $T(", className).add(args).add(");")
            .build();

        return MethodSpec.methodBuilder(operator.name)
            .addModifiers(Modifier.PUBLIC)
            .returns(className)
            .addParameter(componentName, "scalar")
            .addCode(code)
            .build();
    }

    private static Iterable<FieldSpec> createConstants(ClassName className, List<Dimension> dims) {
        var fields = new ArrayList<FieldSpec>();
        fields.add(createConst("ZERO", className, dims, _ -> CODE_BLOCK_0_0F));
        fields.add(createConst("ONE", className, dims, _ -> CODE_BLOCK_1_0F));
        for (Dimension dim : dims) {
            fields.add(createConst(dim.constant, className, dims, dim1 -> dim == dim1 ? CODE_BLOCK_1_0F : CODE_BLOCK_0_0F));
        }
        return fields;
    }

    private static FieldSpec createConst(String name, ClassName className, List<Dimension> dims, Function<Dimension, CodeBlock> mapper) {
        var args = dims.stream()
            .map(mapper)
            .collect(CodeBlock.joining(", "));

        var code = CodeBlock.builder()
            .add("new $T(", className).add(args).add(")")
            .build();

        return FieldSpec.builder(className, name)
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .initializer(code)
            .build();
    }

    private static MethodSpec createSplat(ClassName className, TypeName componentName, List<Dimension> dims) {
        var args = dims.stream()
            .map(_ -> CodeBlock.of("value"))
            .collect(CodeBlock.joining(", "));

        var code = CodeBlock.builder()
            .add("return new $T(", className).add(args).add(");")
            .build();

        return MethodSpec.methodBuilder("splat")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(componentName, "value")
            .returns(className)
            .addCode(code)
            .build();
    }

    private static void writeClass(TypeSpec typeSpec) throws IOException {
        JavaFile
            .builder(PACKAGE_NAME, typeSpec)
            .indent("    ")
            .build()
            .writeTo(Path.of("src/main/java"));
    }

    private enum Dimension {
        X("x", "X"),
        Y("y", "Y"),
        Z("z", "Z"),
        W("w", "W");

        private final String name;
        private final String constant;

        Dimension(String name, String constant) {
            this.name = name;
            this.constant = constant;
        }
    }

    private enum ArithmeticOperator {
        ADD("add", '+'),
        SUB("subtract", '-'),
        MUL("multiply", '*'),
        DIV("divide", '/');

        private final String name;
        private final char symbol;

        ArithmeticOperator(String name, char symbol) {
            this.name = name;
            this.symbol = symbol;
        }
    }
}
