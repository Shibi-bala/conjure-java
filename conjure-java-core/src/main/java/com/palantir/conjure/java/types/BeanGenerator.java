/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.conjure.java.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.palantir.conjure.CaseConverter;
import com.palantir.conjure.java.ConjureAnnotations;
import com.palantir.conjure.java.Options;
import com.palantir.conjure.java.util.JavaNameSanitizer;
import com.palantir.conjure.java.util.Javadoc;
import com.palantir.conjure.java.util.Packages;
import com.palantir.conjure.java.util.Primitives;
import com.palantir.conjure.java.util.TypeFunctions;
import com.palantir.conjure.java.visitor.DefaultTypeVisitor;
import com.palantir.conjure.java.visitor.MoreVisitors;
import com.palantir.conjure.spec.FieldDefinition;
import com.palantir.conjure.spec.FieldName;
import com.palantir.conjure.spec.ListType;
import com.palantir.conjure.spec.MapType;
import com.palantir.conjure.spec.ObjectDefinition;
import com.palantir.conjure.spec.OptionalType;
import com.palantir.conjure.spec.PrimitiveType;
import com.palantir.conjure.spec.SetType;
import com.palantir.conjure.spec.Type;
import com.palantir.conjure.spec.TypeDefinition;
import com.palantir.conjure.visitor.TypeVisitor;
import com.palantir.logsafe.SafeArg;
import com.palantir.logsafe.exceptions.SafeIllegalArgumentException;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.lang.model.element.Modifier;
import org.apache.commons.lang3.StringUtils;
import org.immutables.value.Value;

public final class BeanGenerator {

    private BeanGenerator() {}

    /** The maximum number of parameters for which a static factory method is generated in addition to the builder. */
    private static final int MAX_NUM_PARAMS_FOR_FACTORY = 3;

    /** The name of the singleton instance field generated for empty types. */
    private static final String SINGLETON_INSTANCE_NAME = "INSTANCE";

    public static JavaFile generateBeanType(
            TypeMapper typeMapper,
            SafetyEvaluator safetyEvaluator,
            ObjectDefinition typeDef,
            Map<com.palantir.conjure.spec.TypeName, TypeDefinition> typesMap,
            Options options) {
        ImmutableList<EnrichedField> fields = createFields(typeMapper, typeDef.getFields());
        ImmutableList<FieldSpec> poetFields = EnrichedField.toPoetSpecs(fields);
        ImmutableList<EnrichedField> nonPrimitiveEnrichedFields =
                fields.stream().filter(field -> !field.isPrimitive()).collect(ImmutableList.toImmutableList());

        com.palantir.conjure.spec.TypeName prefixedName =
                Packages.getPrefixedName(typeDef.getTypeName(), options.packagePrefix());
        ClassName objectClass = ClassName.get(prefixedName.getPackage(), prefixedName.getName());
        ClassName builderClass = ClassName.get(objectClass.packageName(), objectClass.simpleName(), "Builder");

        // Use the fully-resolved/computed safety value for the top-level class, however
        // fields (setters/getters) are annotated with declared values because complex objects
        // provide safety information themselves.
        ImmutableList<AnnotationSpec> safety =
                ConjureAnnotations.safety(safetyEvaluator.evaluate(TypeDefinition.object(typeDef)));

        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(prefixedName.getName())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotations(safety);

        if (poetFields.isEmpty()) {
            addEmptyBean(typeBuilder, prefixedName, safety, objectClass, options);
        } else {
            typeBuilder
                    .addFields(poetFields)
                    .addMethod(createConstructor(fields, poetFields))
                    .addMethods(createGetters(fields, typesMap, options, safetyEvaluator));

            boolean useCachedHashCode = useCachedHashCode(fields);
            typeBuilder
                    .addMethod(MethodSpecs.createEquals(objectClass))
                    .addMethod(MethodSpecs.createEqualTo(objectClass, poetFields, useCachedHashCode));
            if (useCachedHashCode) {
                MethodSpecs.addCachedHashCode(typeBuilder, poetFields);
            } else {
                typeBuilder.addMethod(MethodSpecs.createHashCode(poetFields));
            }

            typeBuilder.addMethod(MethodSpecs.createToString(
                            prefixedName.getName(),
                            fields.stream().map(EnrichedField::fieldName).collect(Collectors.toList()))
                    .toBuilder()
                    .addAnnotations(safety)
                    .build());

            if (poetFields.size() <= MAX_NUM_PARAMS_FOR_FACTORY) {
                typeBuilder.addMethod(createStaticFactoryMethod(
                        fields,
                        objectClass,
                        safetyEvaluator,
                        options.useStagedBuilders() && !options.useStrictStagedBuilders()));
            }

            if (!nonPrimitiveEnrichedFields.isEmpty()) {
                typeBuilder
                        .addMethod(createValidateFields(nonPrimitiveEnrichedFields))
                        .addMethod(createAddFieldIfMissing(nonPrimitiveEnrichedFields.size()));
            }

            if (options.useStrictStagedBuilders()) {
                BeanBuilderGenerator.addStrictStagedBuilder(
                        typeBuilder,
                        typeMapper,
                        safetyEvaluator,
                        objectClass,
                        builderClass,
                        typeDef,
                        typesMap,
                        options);
            } else if (options.useStagedBuilders()) {
                BeanBuilderGenerator.addStagedBuilder(
                        typeBuilder,
                        typeMapper,
                        safetyEvaluator,
                        objectClass,
                        builderClass,
                        typeDef,
                        typesMap,
                        options);
            } else {
                BeanBuilderGenerator.addBuilder(
                        typeBuilder,
                        typeMapper,
                        safetyEvaluator,
                        objectClass,
                        builderClass,
                        typeDef,
                        typesMap,
                        options);
            }
        }
        typeBuilder.addAnnotation(ConjureAnnotations.getConjureGeneratedAnnotation(BeanGenerator.class));

        typeDef.getDocs().ifPresent(docs -> typeBuilder.addJavadoc("$L", Javadoc.render(docs)));

        return JavaFile.builder(prefixedName.getPackage(), typeBuilder.build())
                .skipJavaLangImports(true)
                .indent("    ")
                .build();
    }

    private static void addEmptyBean(
            TypeSpec.Builder typeBuilder,
            com.palantir.conjure.spec.TypeName prefixedName,
            ImmutableList<AnnotationSpec> safety,
            ClassName objectClass,
            Options options) {
        typeBuilder.addMethod(createConstructor(ImmutableList.of(), ImmutableList.of()));

        typeBuilder.addMethod(MethodSpecs.createToString(prefixedName.getName(), Collections.emptyList()).toBuilder()
                .addAnnotations(safety)
                .build());

        typeBuilder.addMethod(createStaticFactoryMethodForEmptyBean(objectClass));

        // Need to add JsonSerialize annotation which indicates that the empty bean serializer should be used to
        // serialize this class. Without this annotation no serializer will be set for this class, thus preventing
        // serialization.
        typeBuilder.addAnnotation(JsonSerialize.class).addField(createSingletonField(objectClass));
        if (!options.strictObjects()) {
            typeBuilder.addAnnotation(AnnotationSpec.builder(JsonIgnoreProperties.class)
                    .addMember("ignoreUnknown", "$L", true)
                    .build());
        }
    }

    private static boolean useCachedHashCode(Collection<EnrichedField> fields) {
        if (fields.size() == 1) {
            EnrichedField field = Iterables.getOnlyElement(fields);
            return field.conjureDef().getType().accept(FieldRequiresMemoizedHashCode.INSTANCE);
        }
        return true;
    }

    private static FieldSpec createSingletonField(ClassName objectClass) {
        return FieldSpec.builder(
                        objectClass, SINGLETON_INSTANCE_NAME, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("new $T()", objectClass)
                .build();
    }

    private static ImmutableList<EnrichedField> createFields(TypeMapper typeMapper, List<FieldDefinition> fields) {
        return fields.stream()
                .map(e -> EnrichedField.of(
                        e.getFieldName(),
                        e,
                        FieldSpec.builder(
                                        // fields are guarded against using reserved keywords
                                        typeMapper.getClassName(e.getType()),
                                        JavaNameSanitizer.sanitize(e.getFieldName()),
                                        Modifier.PRIVATE,
                                        Modifier.FINAL)
                                .build()))
                .collect(ImmutableList.toImmutableList());
    }

    private static MethodSpec createConstructor(Collection<EnrichedField> fields, Collection<FieldSpec> poetFields) {
        MethodSpec.Builder builder = MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE);

        Collection<FieldSpec> nonPrimitivePoetFields =
                Collections2.filter(poetFields, f -> !Primitives.isPrimitive(f.type));
        if (!nonPrimitivePoetFields.isEmpty()) {
            builder.addStatement("$L", Expressions.localMethodCall("validateFields", nonPrimitivePoetFields));
        }

        CodeBlock.Builder body = CodeBlock.builder();
        for (EnrichedField field : fields) {
            FieldSpec spec = field.poetSpec();

            builder.addParameter(spec.type, spec.name);

            // Collection and Map types not copied in constructor for performance. This assumes that the constructor
            // is private and necessarily called from the builder, which does its own defensive copying.
            if (field.conjureDef().getType().accept(TypeVisitor.IS_LIST)) {
                // TODO(melliot): contribute a fix to JavaPoet that parses $T correctly for a JavaPoet FieldSpec
                body.addStatement("this.$1N = $2T.unmodifiableList($1N)", spec, Collections.class);
            } else if (field.conjureDef().getType().accept(TypeVisitor.IS_SET)) {
                body.addStatement("this.$1N = $2T.unmodifiableSet($1N)", spec, Collections.class);
            } else if (field.conjureDef().getType().accept(TypeVisitor.IS_MAP)) {
                body.addStatement("this.$1N = $2T.unmodifiableMap($1N)", spec, Collections.class);
            } else {
                body.addStatement("this.$1N = $1N", spec);
            }
        }

        builder.addCode(body.build());

        return builder.build();
    }

    private static Collection<MethodSpec> createGetters(
            Collection<EnrichedField> fields,
            Map<com.palantir.conjure.spec.TypeName, TypeDefinition> typesMap,
            Options featureFlags,
            SafetyEvaluator safetyEvaluator) {
        return fields.stream()
                .map(field -> BeanGenerator.createGetter(field, typesMap, featureFlags, safetyEvaluator))
                .collect(Collectors.toList());
    }

    private static MethodSpec createGetter(
            EnrichedField field,
            Map<com.palantir.conjure.spec.TypeName, TypeDefinition> typesMap,
            Options featureFlags,
            SafetyEvaluator safetyEvaluator) {
        MethodSpec.Builder getterBuilder = MethodSpec.methodBuilder(field.getterName())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(JsonProperty.class)
                        .addMember("value", "$S", field.fieldName().get())
                        .build())
                .addAnnotations(ConjureAnnotations.safety(safetyEvaluator.getUsageTimeSafety(field.conjureDef())))
                .returns(field.poetSpec().type);
        Type conjureDefType = field.conjureDef().getType();
        if (featureFlags.excludeEmptyOptionals()) {
            if (conjureDefType.accept(TypeVisitor.IS_OPTIONAL)) {
                // NON_ABSENT is most accurate for java Optional types (including OptionalDouble/OptionalInt, etc)
                getterBuilder.addAnnotation(AnnotationSpec.builder(JsonInclude.class)
                        .addMember("value", "$T.NON_ABSENT", JsonInclude.Include.class)
                        .build());
            } else if (conjureDefType.accept(TypeVisitor.IS_REFERENCE)
                    && TypeFunctions.toConjureTypeWithoutAliases(conjureDefType, typesMap)
                            .accept(TypeVisitor.IS_OPTIONAL)) {
                // Aliases are special, as usual. Inclusion cannot take advantage of the optional delegate types,
                // however the default (hidden no-arg constructor) can be leveraged using NON_EMPTY.
                getterBuilder.addAnnotation(AnnotationSpec.builder(JsonInclude.class)
                        .addMember("value", "$T.NON_EMPTY", JsonInclude.Include.class)
                        .build());
            }
        }

        if (featureFlags.excludeEmptyCollections()) {
            Type dealiased = conjureDefType.accept(TypeVisitor.IS_REFERENCE)
                    ? TypeFunctions.toConjureTypeWithoutAliases(conjureDefType, typesMap)
                    : conjureDefType;
            if (dealiased.accept(MoreVisitors.IS_COLLECTION)) {
                getterBuilder.addAnnotation(AnnotationSpec.builder(JsonInclude.class)
                        .addMember("value", "$T.NON_EMPTY", JsonInclude.Include.class)
                        .build());
            }
        }

        if (conjureDefType.accept(TypeVisitor.IS_BINARY) && !featureFlags.useImmutableBytes()) {
            getterBuilder.addStatement("return this.$N.asReadOnlyBuffer()", field.poetSpec().name);
        } else {
            getterBuilder.addStatement("return this.$N", field.poetSpec().name);
        }

        Javadoc.render(field.conjureDef().getDocs(), field.conjureDef().getDeprecated())
                .ifPresent(javadoc -> getterBuilder.addJavadoc("$L", javadoc));
        field.conjureDef().getDeprecated().ifPresent(_deprecated -> getterBuilder.addAnnotation(Deprecated.class));
        return getterBuilder.build();
    }

    private static MethodSpec createValidateFields(Collection<EnrichedField> fields) {
        MethodSpec.Builder builder =
                MethodSpec.methodBuilder("validateFields").addModifiers(Modifier.PRIVATE, Modifier.STATIC);

        builder.addStatement("$T missingFields = null", ParameterizedTypeName.get(List.class, String.class));
        for (EnrichedField field : fields) {
            FieldSpec spec = field.poetSpec();
            builder.addParameter(ParameterSpec.builder(spec.type, spec.name).build());
            builder.addStatement(
                    "missingFields = addFieldIfMissing(missingFields, $N, $S)",
                    spec,
                    field.fieldName().get());
        }

        builder.beginControlFlow("if (missingFields != null)")
                .addStatement(
                        "throw new $T(\"Some required fields have not been set\","
                                + " $T.of(\"missingFields\", missingFields))",
                        SafeIllegalArgumentException.class,
                        SafeArg.class)
                .endControlFlow();
        return builder.build();
    }

    /**
     * Generate a static factory method using the provided {@code fields}. If {@code useStagedBuilders} is true, the
     * fields will be ordered with collections and optional fields last. {@code fields} is expected to be an ordered
     * collection.
     */
    private static MethodSpec createStaticFactoryMethod(
            ImmutableList<EnrichedField> fields,
            ClassName objectClass,
            SafetyEvaluator safetyEvaluator,
            boolean useNonStrictStagedBuilders) {
        if (fields.isEmpty()) {
            createStaticFactoryMethodForEmptyBean(objectClass);
        }

        MethodSpec.Builder builder = MethodSpec.methodBuilder("of")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(objectClass);

        builder.addCode("return builder()");
        fields.forEach(field -> builder.addParameter(ParameterSpec.builder(
                        getTypeNameWithoutOptional(field.poetSpec()), field.poetSpec().name)
                .addAnnotations(ConjureAnnotations.safety(safetyEvaluator.getUsageTimeSafety(field.conjureDef())))
                .build()));

        Stream<EnrichedField> methodArgs = useNonStrictStagedBuilders
                ? fields.stream()
                        .sorted(Comparator.comparing(BeanBuilderGenerator::stagedBuilderFieldShouldBeInFinalStage))
                : fields.stream();
        methodArgs.map(EnrichedField::poetSpec).forEach(spec -> {
            if (isOptional(spec)) {
                builder.addCode("\n    .$L(Optional.of($L))", spec.name, spec.name);
            } else {
                builder.addCode("\n    .$L($L)", spec.name, spec.name);
            }
        });
        builder.addCode("\n    .build();\n");

        return builder.build();
    }

    private static MethodSpec createStaticFactoryMethodForEmptyBean(ClassName objectClass) {
        return MethodSpec.methodBuilder("of")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(objectClass)
                .addAnnotation(ConjureAnnotations.delegatingJsonCreator())
                .addCode("return $L;", SINGLETON_INSTANCE_NAME)
                .build();
    }

    private static MethodSpec createAddFieldIfMissing(int fieldCount) {
        ParameterizedTypeName listOfStringType = ParameterizedTypeName.get(List.class, String.class);
        ParameterSpec listParam =
                ParameterSpec.builder(listOfStringType, "prev").build();
        ParameterSpec fieldValueParam =
                ParameterSpec.builder(ClassName.OBJECT, "fieldValue").build();
        ParameterSpec fieldNameParam =
                ParameterSpec.builder(ClassName.get(String.class), "fieldName").build();

        return MethodSpec.methodBuilder("addFieldIfMissing")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .returns(listOfStringType)
                .addParameter(listParam)
                .addParameter(fieldValueParam)
                .addParameter(fieldNameParam)
                .addStatement("$T missingFields = $N", listOfStringType, listParam)
                .beginControlFlow("if ($N == null)", fieldValueParam)
                .beginControlFlow("if (missingFields == null)")
                .addStatement("missingFields = new $T<>($L)", ArrayList.class, fieldCount)
                .endControlFlow()
                .addStatement("missingFields.add($N)", fieldNameParam)
                .endControlFlow()
                .addStatement("return missingFields")
                .build();
    }

    private static TypeName getTypeNameWithoutOptional(FieldSpec spec) {
        if (!isOptional(spec)) {
            return spec.type;
        }
        TypeName typeName = ((ParameterizedTypeName) spec.type).typeArguments.get(0);
        return Primitives.isBoxedPrimitive(typeName) ? Primitives.unbox(typeName) : typeName;
    }

    private static boolean isOptional(FieldSpec spec) {
        if (!(spec.type instanceof ParameterizedTypeName)) {
            // spec isn't a wrapper class
            return false;
        }
        return ((ParameterizedTypeName) spec.type).rawType.simpleName().equals("Optional");
    }

    /** Note, this is an implementation detail shared between {@link BeanBuilderGenerator} and {@link BeanGenerator}. */
    @Value.Immutable
    interface EnrichedField {
        @Value.Parameter
        FieldName fieldName();

        @Value.Derived
        default String getterName() {
            return asGetterName(fieldName().get());
        }

        @Value.Parameter
        FieldDefinition conjureDef();

        @Value.Parameter
        FieldSpec poetSpec();

        @Value.Derived
        default boolean isPrimitive() {
            return Primitives.isPrimitive(poetSpec().type);
        }

        static EnrichedField of(FieldName fieldName, FieldDefinition conjureDef, FieldSpec poetSpec) {
            return ImmutableEnrichedField.of(fieldName, conjureDef, poetSpec);
        }

        static ImmutableList<FieldSpec> toPoetSpecs(Collection<EnrichedField> fields) {
            return fields.stream().map(EnrichedField::poetSpec).collect(ImmutableList.toImmutableList());
        }
    }

    public static String asGetterName(String fieldName) {
        String lowerCamelCaseName = CaseConverter.toCase(fieldName, CaseConverter.Case.LOWER_CAMEL_CASE);
        return JavaNameSanitizer.sanitizeMethod("get" + StringUtils.capitalize(lowerCamelCaseName));
    }

    /**
     * Any types we generate directly will produce an efficient hashCode, but collections may be large, and we must
     * traverse through optionals.
     */
    static final class FieldRequiresMemoizedHashCode extends DefaultTypeVisitor<Boolean> {
        static final DefaultTypeVisitor<Boolean> INSTANCE = new FieldRequiresMemoizedHashCode();

        @Override
        public Boolean visitOptional(OptionalType value) {
            return value.getItemType().accept(INSTANCE);
        }

        @Override
        public Boolean visitList(ListType _value) {
            return Boolean.TRUE;
        }

        @Override
        public Boolean visitSet(SetType _value) {
            return Boolean.TRUE;
        }

        @Override
        public Boolean visitMap(MapType _value) {
            return Boolean.TRUE;
        }

        @Override
        public Boolean visitPrimitive(PrimitiveType value) {
            // datetime hashing is special, the instant is hashed rather than the time itself.
            return PrimitiveType.DATETIME.equals(value);
        }

        @Override
        public Boolean visitDefault() {
            return Boolean.FALSE;
        }
    }
}
