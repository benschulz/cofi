package de.benshu.cofi.runtime.serialization;

import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import de.benshu.cofi.binary.deserialization.internal.AbstractBinaryModelContext;
import de.benshu.cofi.binary.deserialization.internal.BinaryModelContext;
import de.benshu.cofi.binary.deserialization.internal.TypeParser;
import de.benshu.cofi.binary.internal.Ancestry;
import de.benshu.cofi.binary.internal.Constructor;
import de.benshu.cofi.binary.internal.MemoizingSupplier;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.runtime.Annotation;
import de.benshu.cofi.runtime.Closure;
import de.benshu.cofi.runtime.Companion;
import de.benshu.cofi.runtime.ExpressionStatement;
import de.benshu.cofi.runtime.FunctionInvocation;
import de.benshu.cofi.runtime.LiteralValue;
import de.benshu.cofi.runtime.LocalVariableDeclaration;
import de.benshu.cofi.runtime.MemberAccess;
import de.benshu.cofi.runtime.MemberDeclaration;
import de.benshu.cofi.runtime.MemberDeclarationVisitor;
import de.benshu.cofi.runtime.MethodDeclaration;
import de.benshu.cofi.runtime.ModelNode;
import de.benshu.cofi.runtime.Module;
import de.benshu.cofi.runtime.Multiton;
import de.benshu.cofi.runtime.NameExpression;
import de.benshu.cofi.runtime.ObjectSingleton;
import de.benshu.cofi.runtime.Package;
import de.benshu.cofi.runtime.Parameter;
import de.benshu.cofi.runtime.PropertyDeclaration;
import de.benshu.cofi.runtime.RootExpression;
import de.benshu.cofi.runtime.SingletonCompanion;
import de.benshu.cofi.runtime.ThisExpression;
import de.benshu.cofi.runtime.Trait;
import de.benshu.cofi.runtime.TypeBody;
import de.benshu.cofi.runtime.TypeDeclaration;
import de.benshu.cofi.runtime.Union;
import de.benshu.cofi.runtime.context.RuntimeContext;
import de.benshu.cofi.runtime.context.RuntimeTypeName;
import de.benshu.cofi.runtime.internal.Resolution;
import de.benshu.cofi.runtime.internal.TypeParameterListReference;
import de.benshu.cofi.runtime.internal.TypeReference;
import de.benshu.cofi.runtime.internal.TypeReferenceContext;
import de.benshu.cofi.types.ProperTypeConstructor;
import de.benshu.cofi.types.TemplateType;
import de.benshu.cofi.types.TemplateTypeConstructor;
import de.benshu.cofi.types.TypeList;
import de.benshu.cofi.types.impl.AdHoc;
import de.benshu.cofi.types.impl.ProperTypeConstructorMixin;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.TypeParameterImpl;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.impl.declarations.TemplateTypeDeclaration;
import de.benshu.cofi.types.impl.declarations.UnionTypeDeclaration;
import de.benshu.cofi.types.impl.declarations.source.SourceMemberDescriptor;
import de.benshu.cofi.types.impl.declarations.source.SourceMemberDescriptors;
import de.benshu.cofi.types.impl.declarations.source.SourceMethodDescriptor;
import de.benshu.cofi.types.impl.declarations.source.SourceMethodSignatureDescriptor;
import de.benshu.cofi.types.impl.declarations.source.SourcePropertyDescriptor;
import de.benshu.cofi.types.impl.declarations.source.SourceType;
import de.benshu.cofi.types.impl.declarations.source.SourceTypeDescriptor;
import de.benshu.cofi.types.impl.templates.AbstractTemplateTypeConstructor;
import de.benshu.cofi.types.impl.templates.TemplateTypeConstructorMixin;
import de.benshu.cofi.types.impl.templates.TemplateTypeImpl;
import de.benshu.cofi.types.impl.unions.AbstractUnionTypeConstructor;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.commons.core.Optional;

import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.Maps.immutableEntry;
import static com.google.common.collect.Tables.immutableCell;
import static de.benshu.commons.core.Optional.none;
import static de.benshu.commons.core.Optional.some;
import static de.benshu.commons.core.streams.Collectors.map;
import static de.benshu.commons.core.streams.Collectors.set;
import static de.benshu.commons.core.streams.Collectors.setMultimap;
import static de.benshu.commons.core.streams.Collectors.table;
import static java.util.Arrays.asList;

public class ModuleDeserializer {
    private static final ImmutableSet<Class<? extends ModelNode>> MODEL_NODE_TYPES = ImmutableSet.of(
            Annotation.class, de.benshu.cofi.runtime.Class.class, Closure.class, Closure.Case.class,
            Companion.MultitonCompanion.class, ExpressionStatement.class, FunctionInvocation.class, LiteralValue.class,
            LocalVariableDeclaration.class, MemberAccess.class, MethodDeclaration.class, MethodDeclaration.Piece.class,
            Module.class, NameExpression.class, ObjectSingleton.class, Package.class, Parameter.class,
            PropertyDeclaration.class, RootExpression.class, SingletonCompanion.class, ThisExpression.class, Trait.class,
            TypeBody.class, Union.class
    );

    private static final ImmutableMap<ImmutableSet<String>, Class<? extends ModelNode>> TYPE_BY_PROPERTIES = loadTypes(MODEL_NODE_TYPES);

    private static ImmutableMap<ImmutableSet<String>, Class<? extends ModelNode>> loadTypes(ImmutableSet<Class<? extends ModelNode>> types) {
        return types.stream()
                .flatMap(t -> determinePropertiesOf(t).map(ps -> Maps.<ImmutableSet<String>, Class<? extends ModelNode>>immutableEntry(ps, t)))
                .collect(map());
    }

    private static Stream<ImmutableSet<String>> determinePropertiesOf(Class<? extends ModelNode> type) {
        final FluentIterable<Field> allField = determineAllNonTransientFieldsOf(type);

        final FluentIterable<Field> nonOptional = allField.filter(f -> f.getType() != Optional.class);
        final ImmutableSet<Field> optional = allField.filter(f -> f.getType() == Optional.class).toSet();

        return Sets.powerSet(optional).stream()
                .map(nonOptional::append)
                .map(ps -> ps.transform(Field::getName))
                .map(FluentIterable::toSet);
    }

    private static FluentIterable<Field> determineAllNonTransientFieldsOf(Class<?> type) {
        final FluentIterable<Field> declaredFields = FluentIterable.of(type.getDeclaredFields())
                .filter(f -> (f.getModifiers() & Modifier.TRANSIENT) == 0);

        return type.getSuperclass() == null
                ? declaredFields
                : determineAllNonTransientFieldsOf(type.getSuperclass()).append(declaredFields);
    }

    static Class<? extends ModelNode> determineTypeOf(JsonObject jsonObject) {
        final ImmutableSet<String> properties = jsonObject.entrySet().stream()
                .map(Map.Entry::getKey)
                .collect(set());

        final Class<? extends ModelNode> type = TYPE_BY_PROPERTIES.get(properties);
        return checkNotNull(type);
    }

    private final RuntimeContext runtimeContext;
    private final ImmutableSet<Module> dependencies;

    public ModuleDeserializer(RuntimeContext runtimeContext, ImmutableSet<Module> dependencies) {
        this.runtimeContext = runtimeContext;
        this.dependencies = dependencies;
    }

    // TODO Decouple RuntimeContext and Module; RuntimeContext should be a constructor argument to ModuleDeserializer.
    public Module deserialize(Reader reader) {
        return new Deserialization(reader).getResult();
    }

    private class Deserialization {
        private final ImmutableTable<Class<? extends ModelNode>, String, SpecialPropertyDeserializer> specialProperties =
                ImmutableTable.<Class<? extends ModelNode>, String, SpecialPropertyDeserializer>builder()
                        .putAll(MODEL_NODE_TYPES.stream()
                                .filter(TypeDeclaration.class::isAssignableFrom)
                                .map(t -> immutableCell(t, "type", (SpecialPropertyDeserializer) this::createTypeDeclarationTypeFactory))
                                .collect(table()))
                        .put(Module.class, "root", (SimpleSpecialPropertyDeserializer) this::createRootObjectSingletonSupplier)
                        .put(Module.class, "typeParameters", this::createEmptyTypeParameters)
                        .put(MethodDeclaration.class, "signatureConstructor", this::deserializeMethodSignature)
                        .put(PropertyDeclaration.class, "type", (SimpleSpecialPropertyDeserializer) this::deserializePropertyType)
                        .put(PropertyDeclaration.class, "typeParameters", this::createEmptyTypeParameters)
                        .put(RootExpression.class, "type", (SimpleSpecialPropertyDeserializer) this::deserializeRootSignatureType)
                        .build();

        private final TypeParser typeParser = new TypeParser(
                new TypeParser.Namer() {
                    @Override
                    public IndividualTags name(String name) {
                        return IndividualTags.of(RuntimeTypeName.TAG, RuntimeTypeName.of(name));
                    }

                    @Override
                    public String getNameOf(TypeParameterImpl<?> typeParameter) {
                        return typeParameter.getTags().get(RuntimeTypeName.TAG).toDescriptor();
                    }
                });

        private final Supplier<Module> result;

        public Deserialization(Reader reader) {
            final Gson gson = new GsonBuilder().setPrettyPrinting()
                    .registerTypeHierarchyAdapter(ModelNode.class, (JsonDeserializer<ModelNode>) this::deserializeModelNode)
                    .registerTypeAdapter(Constructor.class, (JsonDeserializer<Constructor<?>>) this::deserializeConstructor)
                    .registerTypeAdapter(Fqn.class, (JsonDeserializer<Fqn>) this::deserializeFqn)
                    .registerTypeAdapter(Optional.class, (JsonDeserializer<Optional<?>>) this::deserializeOptional)
                    .registerTypeAdapter(Supplier.class, (JsonDeserializer<Supplier<?>>) this::deserializeSupplier)
                    .registerTypeAdapter(ImmutableList.class, (JsonDeserializer<ImmutableList<?>>) this::deserializeList)
                    .registerTypeAdapter(ImmutableSet.class, (JsonDeserializer<ImmutableSet<?>>) this::deserializeSet)
                    .registerTypeAdapter(TypeParameterListReference.class, (JsonDeserializer<TypeParameterListReference>) this::deserializeTypeParameterListReference)
                    .registerTypeAdapter(TypeReference.class, (JsonDeserializer<TypeReference<?>>) this::deserializeTypeReference)
                    .create();

            this.result = MemoizingSupplier.of(() -> gson.fromJson(reader, Module.class));
        }

        public Module getResult() {
            return result.get();
        }

        private ModelNode deserializeModelNode(JsonElement json, Type requiredType, JsonDeserializationContext context) throws JsonParseException {
            final Predicate<Object> isModule = x -> Module.class.equals(requiredType);

            return deserializeModelNode(some(Ancestry.empty()).filter(isModule), json, requiredType, context);
        }

        private ModelNode deserializeModelNode(Optional<Ancestry> ancestry, JsonElement json, Type requiredType, JsonDeserializationContext context) throws JsonParseException {
            final JsonObject jsonObject = json.getAsJsonObject();

            final Class<? extends ModelNode> type = determineTypeOf(jsonObject);
            checkArgument(TypeToken.of(requiredType).getRawType().isAssignableFrom(type));

            return deserializeModelNode(ancestry, context, type, jsonObject);
        }

        private <N extends ModelNode> N deserializeModelNode(Optional<Ancestry> ancestry, JsonDeserializationContext context, Class<N> requiredType, JsonObject jsonObject) {
            final java.lang.reflect.Constructor<N> constructor = getConstructorOf(requiredType);

            class NodeDeserialization {
                private final ImmutableMap<String, Supplier<?>> arguments = Stream.of(constructor.getParameters())
                        .map(this::deserializeArgument)
                        .collect(map());

                private Map.Entry<String, Supplier<?>> deserializeArgument(java.lang.reflect.Parameter parameter) {
                    return immutableEntry(parameter.getName(), MemoizingSupplier.of(() ->
                                    ancestry.map(a -> (Object) a)
                                            .filter(a -> parameter.getType() == Ancestry.class)
                                            .or(() -> Optional.from(specialProperties.get(requiredType, parameter.getName()))
                                                    .map(d -> d.deserialize(jsonObject, ancestry, NodeDeserialization.this.arguments)))
                                            .getOrSupply(() -> {
                                                final JsonElement jsonArgument = jsonObject.get(parameter.getName());
                                                final Type parameterType = parameter.getParameterizedType();

                                                return jsonArgument == null && TypeToken.of(parameterType).getRawType() == Optional.class ? none()
                                                        : context.deserialize(jsonArgument, parameterType);
                                            })
                    ));
                }

                N perform() {
                    try {
                        return constructor.newInstance(Stream.of(constructor.getParameters())
                                .map(p -> arguments.get(p.getName()).get())
                                .toArray());
                    } catch (Exception e) {
                        throw Throwables.propagate(e);
                    }
                }
            }

            return new NodeDeserialization().perform();
        }

        @SuppressWarnings("unchecked")
        private <N extends ModelNode> java.lang.reflect.Constructor<N> getConstructorOf(Class<N> modelNodeType) {
            return (java.lang.reflect.Constructor) getOnlyElement(asList(modelNodeType.getConstructors()));
        }

        private Function<TypeDeclaration, ProperTypeConstructor<?>> createTypeDeclarationTypeFactory(
                JsonObject jsonObject, Optional<Ancestry> ancestry, ImmutableMap<?, ?> otherProperties) {

            return d -> createTypeDeclarationType(d, ancestry.get(), jsonObject).unbind();
        }

        private ProperTypeConstructorMixin<RuntimeContext, ?, ? extends ProperTypeMixin<RuntimeContext, ?>> createTypeDeclarationType(
                TypeDeclaration declaration, Ancestry ancestry, JsonObject jsonObject) {

            final Ancestry ancestryIncludingMe = ancestry.append(declaration);

            final Function<RuntimeContext, IndividualTags> tagsSupplier = x -> {
                final String name = Optional.from(jsonObject.get("name"))
                        .map(JsonElement::getAsString)
                        .getOrSupply(() -> jsonObject.get("fqn").getAsString());

                final IndividualTags regular = IndividualTags.empty()
                        .set(RuntimeTypeName.TAG, RuntimeTypeName.of(name))
                        .set(TypeDeclaration.TAG, declaration);

                return declaration instanceof Companion
                        ? regular.set(AbstractBinaryModelContext.ACCOMPANIED_TAG, ProperTypeConstructorMixin.rebind(((Companion) declaration).getAccompanied().getType()))
                        : regular;

            };

            BiFunction<JsonArray, RuntimeContext, ImmutableList<SourceType<RuntimeContext>>> hierarchySupplier =
                    (ts, x) -> deserializeArray(ts)
                            .map(e -> typeParser
                                    .in(rebind(Resolution.extractTypeReferenceContextFrom(ancestryIncludingMe)))
                                    .parseType(e.getAsString()))
                            .map(t -> t.<RuntimeContext>bind(x))
                            .map(SourceType::of)
                            .collect(Collector.of(
                                    ImmutableList::<SourceType<RuntimeContext>>builder,
                                    ImmutableList.Builder::add,
                                    (left, right) -> left.addAll(right.build()),
                                    ImmutableList.Builder::build));

            // TODO use visitor
            if (declaration instanceof Union) {
                final JsonArray elements = jsonObject.getAsJsonArray("elements");

                return AbstractUnionTypeConstructor.create(UnionTypeDeclaration.<RuntimeContext>lazy(
                        (x, b) -> TypeParameterListImpl.rebind(declaration.getTypeParameters()),
                        (x, b) -> hierarchySupplier.apply(elements, x),
                        (x, b) -> tagsSupplier.apply(x)
                )).bind(runtimeContext);
            } else {
                final JsonArray supertypes = jsonObject.getAsJsonArray("supertypes");

                return AbstractTemplateTypeConstructor.create(TemplateTypeDeclaration.<RuntimeContext>memoizing(
                        (x, b) -> TypeParameterListImpl.<RuntimeContext>rebind(declaration.getTypeParameters()),
                        (x, b) -> hierarchySupplier.apply(supertypes, x),
                        (x, b) -> collectMemberDescriptorsOf(declaration),
                        (x, b) -> tagsSupplier.apply(x)
                )).bind(runtimeContext);
            }
        }

        private SourceMemberDescriptors<RuntimeContext> collectMemberDescriptorsOf(TypeDeclaration declaration) {
            final ImmutableSet<SourceMemberDescriptor<RuntimeContext>> descriptors = declaration.getMemberDeclarations()
                    .map(d -> immutableEntry(d.getName(), d))
                    .collect(setMultimap()).asMap().entrySet().stream()
                    .map(ds -> toMemberDescriptor(ImmutableSet.copyOf(ds.getValue())))
                    .collect(set());

            return SourceMemberDescriptors.create(
                    AbstractConstraints.rebind(declaration.getTypeParameters().getConstraints()),
                    descriptors
            );
        }

        private SourceMemberDescriptor<RuntimeContext> toMemberDescriptor(ImmutableSet<MemberDeclaration> memberDeclarations) {
            if (memberDeclarations.size() == 1)
                return toMemberDescriptor(memberDeclarations.iterator().next());
            else
                return toMethodDescriptor(memberDeclarations);
        }

        private SourceMemberDescriptor<RuntimeContext> toMemberDescriptor(MemberDeclaration memberDeclaration) {
            return memberDeclaration.accept(new MemberDeclarationVisitor<SourceMemberDescriptor<RuntimeContext>>() {
                @Override
                public SourceMemberDescriptor<RuntimeContext> visitMethodDeclaration(MethodDeclaration methodDeclaration) {
                    return Deserialization.this.toMethodDescriptor(methodDeclaration, ImmutableList.of(toMethodSignatureDescriptor(methodDeclaration)));
                }

                @Override
                public SourceMemberDescriptor<RuntimeContext> visitPropertyDeclaration(PropertyDeclaration propertyDeclaration) {
                    return toPropertyDescriptor(propertyDeclaration);
                }

                @Override
                public SourceMemberDescriptor<RuntimeContext> visitTypeDeclaration(TypeDeclaration typeDeclaration) {
                    return toTypeDescriptor(typeDeclaration);
                }
            });
        }

        private SourceMemberDescriptor<RuntimeContext> toMethodDescriptor(ImmutableSet<MemberDeclaration> memberDeclarations) {
            return toMethodDescriptor((MethodDeclaration) memberDeclarations.iterator().next(), memberDeclarations.stream()
                    .map(MethodDeclaration.class::cast).map(this::toMethodSignatureDescriptor)
                    .collect(Collector.of(
                            ImmutableList::<SourceMethodSignatureDescriptor<RuntimeContext>>builder,
                            ImmutableList.Builder::add,
                            (left, right) -> left.addAll(right.build()),
                            ImmutableList.Builder::build)));
        }

        private SourceMemberDescriptor<RuntimeContext> toMethodDescriptor(MethodDeclaration methodDeclaration, ImmutableList<SourceMethodSignatureDescriptor<RuntimeContext>> signatureDescriptors) {
            return new SourceMethodDescriptor<RuntimeContext>() {
                @Override
                public ImmutableList<SourceMethodSignatureDescriptor<RuntimeContext>> getMethodSignatureDescriptors() {
                    return signatureDescriptors;
                }

                @Override
                public String getName() {
                    return methodDeclaration.getName();
                }

                @Override
                public IndividualTags getTags(RuntimeContext context) {
                    return IndividualTags.of(MemberDeclaration.TAG, methodDeclaration);
                }
            };
        }

        private SourceMethodSignatureDescriptor<RuntimeContext> toMethodSignatureDescriptor(MethodDeclaration methodDeclaration) {
            return new SourceMethodSignatureDescriptor<RuntimeContext>() {
                @Override
                public TypeParameterListImpl<RuntimeContext> getTypeParameters() {
                    return TypeParameterListImpl.rebind(methodDeclaration.getTypeParameters());
                }

                @Override
                public ImmutableList<ImmutableList<SourceType<RuntimeContext>>> getParameterTypes() {
                    return determineParameterTypes(methodDeclaration.getSignature().applyTrivially());
                }

                @Override
                public SourceType<RuntimeContext> getReturnType() {
                    return determineReturnType(methodDeclaration.getSignature().applyTrivially());
                }
            };
        }

        private ImmutableList<ImmutableList<SourceType<RuntimeContext>>> determineParameterTypes(TemplateType signature) {
            final TypeList<?> signatureTypeArguments = signature.getArguments();
            final int parameterCount = signatureTypeArguments.size() - 1;
            final ImmutableList<SourceType<RuntimeContext>> firstParameterList = signatureTypeArguments.subList(0, parameterCount).stream()
                    .map(t -> SourceType.of(TypeMixin.<RuntimeContext>rebind(t)))
                    .collect(Collector.of(
                            ImmutableList::<SourceType<RuntimeContext>>builder,
                            ImmutableList.Builder::add,
                            (left, right) -> left.addAll(right.build()),
                            ImmutableList.Builder::build));

            final de.benshu.cofi.types.Type returnType = signatureTypeArguments.get(parameterCount);

            final boolean returnTypeIsFunction = isFunction(returnType);

            return returnTypeIsFunction
                    ? ImmutableList.<ImmutableList<SourceType<RuntimeContext>>>builder().add(firstParameterList).addAll(determineParameterTypes((TemplateType) returnType)).build()
                    : ImmutableList.of(firstParameterList);
        }

        private SourceType<RuntimeContext> determineReturnType(TemplateType signature) {
            final TypeList<?> signatureTypeArguments = signature.getArguments();
            final int parameterCount = signatureTypeArguments.size() - 1;

            final de.benshu.cofi.types.Type returnType = signatureTypeArguments.get(signatureTypeArguments.size() - 1);

            return returnType instanceof TemplateType && ((TemplateType) returnType).getConstructor() == runtimeContext.getTypeSystem().getFunction(parameterCount)
                    ? determineReturnType((TemplateType) returnType)
                    : SourceType.of(TypeMixin.<RuntimeContext>rebind(returnType));
        }

        private boolean isFunction(de.benshu.cofi.types.Type type) {
            if (!(type instanceof TemplateType))
                return false;

            final TemplateType templateType = (TemplateType) type;

            return TypeMixin.<RuntimeContext>rebind(templateType.getConstructor()).isSameAs(
                    runtimeContext.getTypeSystem().getFunctionOrNull(templateType.getArguments().size() - 1));
        }

        private SourceMemberDescriptor<RuntimeContext> toPropertyDescriptor(PropertyDeclaration propertyDeclaration) {
            return new SourcePropertyDescriptor<RuntimeContext>() {
                @Override
                public SourceType<RuntimeContext> getValueType() {
                    return SourceType.of(TypeMixin.<RuntimeContext>rebind(propertyDeclaration.getValueType()));
                }

                @Override
                public ImmutableList<SourceType<RuntimeContext>> getTraits() {
                    return propertyDeclaration.getTraits().stream()
                            .map(t -> SourceType.of(TypeMixin.<RuntimeContext>rebind(t)))
                            .collect(Collector.of(
                                    ImmutableList::<SourceType<RuntimeContext>>builder,
                                    ImmutableList.Builder::add,
                                    (left, right) -> left.addAll(right.build()),
                                    ImmutableList.Builder::build));
                }

                @Override
                public String getName() {
                    return propertyDeclaration.getName();
                }

                @Override
                public IndividualTags getTags(RuntimeContext context) {
                    return IndividualTags.of(MemberDeclaration.TAG, propertyDeclaration);
                }
            };
        }

        private SourceMemberDescriptor<RuntimeContext> toTypeDescriptor(TypeDeclaration typeDeclaration) {
            Multiton multiton = typeDeclaration.getCompanion().map(Multiton.class::cast).getOrSupply(() -> (Multiton) typeDeclaration);

            return new SourceTypeDescriptor<RuntimeContext>() {
                @Override
                public SourceType<RuntimeContext> getType() {
                    return SourceType.of(TypeMixin.<RuntimeContext>rebind(multiton.getType()));
                }

                @Override
                public String getName() {
                    return multiton.getName();
                }

                @Override
                public IndividualTags getTags(RuntimeContext context) {
                    return IndividualTags.of(MemberDeclaration.TAG, multiton);
                }
            };
        }

        private Supplier<ObjectSingleton> createRootObjectSingletonSupplier(JsonObject json) {
            return MemoizingSupplier.of(this::createRootObjectSingleton);
        }

        private ObjectSingleton createRootObjectSingleton() {
            final ImmutableSet<Module> modules = Stream.concat(Stream.of(result.get()), dependencies.stream()).collect(set());

            final ImmutableSet<Fqn> glueObjectFqns = modules.stream()
                    .filter(m -> modules.stream().noneMatch(other -> other.getFqn().strictlyContains(m.getFqn())))
                    .flatMap(m -> m.getFqn().getParent().getAncestry().stream())
                    .distinct()
                    .collect(set());

            final AtomicReference<ImmutableMap<Fqn, ObjectSingleton>> hack = new AtomicReference<>();
            final ImmutableMap<Fqn, ObjectSingleton> glueTypes = glueObjectFqns.stream()
                    .map(fqn -> immutableEntry(fqn, AbstractTemplateTypeConstructor.<RuntimeContext>create(
                            TemplateTypeDeclaration.memoizing(
                                    (x, b) -> TypeParameterListImpl.empty(),
                                    (x, b) -> ImmutableList.of(),
                                    (x, b) -> {
                                        final Stream<Map.Entry<String, TypeDeclaration>> containedModules = modules.stream()
                                                .filter(m -> m.getFqn().getParent().equals(fqn))
                                                .map(m -> immutableEntry(m.getFqn().getLocalName(), m));

                                        final Stream<Map.Entry<String, TypeDeclaration>> containedGlueObjects = hack.get().entrySet().stream()
                                                .filter(e -> e.getKey().length() > 0)
                                                .filter(e -> e.getKey().getParent().equals(fqn))
                                                .map(e -> immutableEntry(e.getKey().getLocalName(), e.getValue()));

                                        return SourceMemberDescriptors.create(Stream.concat(containedModules, containedGlueObjects)
                                                .map(e -> new SourceTypeDescriptor<RuntimeContext>() {
                                                    @Override
                                                    public String getName() {
                                                        return e.getKey();
                                                    }

                                                    @Override
                                                    public SourceType<RuntimeContext> getType() {
                                                        return SourceType.of(TypeMixin.<RuntimeContext>rebind(e.getValue().getType()));
                                                    }

                                                    @Override
                                                    public IndividualTags getTags(RuntimeContext context) {
                                                        return IndividualTags.of(MemberDeclaration.TAG, e.getValue());
                                                    }
                                                })
                                                .collect(set()));
                                    },
                                    (x, b) -> IndividualTags.of(RuntimeTypeName.TAG, RuntimeTypeName.of(fqn.toCanonicalString())))
                    ).bind(runtimeContext)))
                    .map(e -> immutableEntry(e.getKey(), new ObjectSingleton(
                            Ancestry.empty(),
                            ImmutableSet.of(),
                            e.getKey().isRoot() ? "<root>" : e.getKey().getLocalName(),
                            x -> e.getValue().getParameters().unbind(),
                            x -> e.getValue().unbind(),
                            x -> new TypeBody(x, ImmutableList.of())
                    )))
                    .collect(map());
            hack.set(glueTypes);

            return glueTypes.get(Fqn.root());
        }

        private TypeParameterListReference createEmptyTypeParameters(JsonObject jsonObject, Optional<Ancestry> ancestry, ImmutableMap<String, Supplier<?>> otherProperties) {
            return x -> {
                final AbstractConstraints<RuntimeContext> parentConstraints = ancestry
                        .flatMap(a -> a.closest(MemberDeclaration.class))
                        .map(MemberDeclaration::getTypeParameters)
                        .map(TypeParameterListImpl::<RuntimeContext>rebind)
                        .map(TypeParameterListImpl::getConstraints)
                        .getOrReturn(AbstractConstraints.none());

                return TypeParameterListImpl.empty(runtimeContext, parentConstraints).unbind();
            };
        }

        private TypeReference<TemplateTypeConstructor> deserializeMethodSignature(JsonObject jsonObject, Optional<Ancestry> ancestry, ImmutableMap<String, Supplier<?>> otherProperties) {
            return x -> {
                final String signatureString = jsonObject.get("signature").getAsString();

                final TypeParameterListReference typeParametersReference = (TypeParameterListReference) otherProperties.get("typeParameters").get();
                final TypeParameterListImpl<RuntimeContext> typeParameters = TypeParameterListImpl.rebind(Resolution.resolve(ancestry.get(), typeParametersReference).get());

                final TypeMixin<RuntimeContext, ?> proper = typeParser.in(rebind(x)).parseType(signatureString).<RuntimeContext>bind(runtimeContext);
                final TemplateTypeConstructorMixin<RuntimeContext> constructor = AdHoc.templateTypeConstructor(runtimeContext, typeParameters, (TemplateTypeImpl<RuntimeContext>) proper);
                return constructor.unbind();
            };
        }

        private TypeReference<TemplateTypeConstructor> deserializePropertyType(JsonObject jsonObject) {
            return x -> {
                throw null;
            };
        }

        private TypeReference<TemplateType> deserializeRootSignatureType(JsonObject jsonObject) {
            return x -> result.get().getRoot().getType().applyTrivially();
        }

        private Constructor<?> deserializeConstructor(JsonElement json, Type type, JsonDeserializationContext context) {
            final Type constsructedType = ((ParameterizedType) type).getActualTypeArguments()[0];

            return ancestry -> deserializeModelNode(some(ancestry), json, constsructedType, context);
        }

        private Fqn deserializeFqn(JsonElement json, Type type, JsonDeserializationContext context) {
            return deserializeFqn(json.getAsString());
        }

        private Fqn deserializeFqn(String canonicalString) {
            final Iterable<String> ids = Splitter.on('.').omitEmptyStrings().split(canonicalString);
            return Fqn.from(Iterables.toArray(ids, String.class));
        }

        private Optional<?> deserializeOptional(JsonElement json, Type type, JsonDeserializationContext context) {
            final Type wrappedType = ((ParameterizedType) type).getActualTypeArguments()[0];

            return json == null ? none()
                    : some(context.deserialize(json, wrappedType));
        }

        private Supplier<?> deserializeSupplier(JsonElement json, Type type, JsonDeserializationContext context) {
            Type suppliedType = ((ParameterizedType) type).getActualTypeArguments()[0];

            return MemoizingSupplier.of(() -> context.deserialize(json, suppliedType));
        }

        private TypeParameterListReference deserializeTypeParameterListReference(JsonElement json, Type type, JsonDeserializationContext context) {
            final String typeParametersString = json.getAsString();

            return x -> typeParser.in(rebind(x)).parseTypeParameters(typeParametersString).bind(runtimeContext).unbind();
        }

        private TypeReference<?> deserializeTypeReference(JsonElement json, Type type, JsonDeserializationContext context) {
            final String typeString = json.getAsString();

            return x -> typeParser.in(rebind(x)).parseType(typeString).bind(runtimeContext).unbind();
        }

        private ImmutableList<?> deserializeList(JsonElement json, Type type, JsonDeserializationContext context) {
            final Type elementType = ((ParameterizedType) type).getActualTypeArguments()[0];
            final JsonArray jsonArray = json.getAsJsonArray();

            return deserializeArray(jsonArray)
                    .map(e -> context.deserialize(e, elementType))
                    .collect(Collector.of(
                            ImmutableList::<Object>builder,
                            ImmutableList.Builder::add,
                            (left, right) -> left.addAll(right.build()),
                            ImmutableList.Builder::build));
        }

        private ImmutableSet<?> deserializeSet(JsonElement json, Type type, JsonDeserializationContext context) {
            final Type elementType = ((ParameterizedType) type).getActualTypeArguments()[0];
            final JsonArray jsonArray = json.getAsJsonArray();

            return deserializeArray(jsonArray)
                    .map(e -> context.deserialize(e, elementType))
                    .collect(set());
        }

        private Stream<JsonElement> deserializeArray(JsonArray array) {
            return ContiguousSet.create(Range.closedOpen(0, array.size()), DiscreteDomain.integers()).stream()
                    .map(array::get);
        }

        private de.benshu.cofi.binary.deserialization.internal.TypeReferenceContext rebind(TypeReferenceContext typeReferenceContext) {
            return new de.benshu.cofi.binary.deserialization.internal.TypeReferenceContext() {
                @Override
                public <X extends BinaryModelContext<X>> Optional<AbstractConstraints<X>> getOuterConstraints(X context) {
                    return typeReferenceContext.getOuterConstraints().map(AbstractConstraints::rebind);
                }

                @Override
                public <X extends BinaryModelContext<X>> AbstractConstraints<X> getConstraints(X context) {
                    return AbstractConstraints.rebind(typeReferenceContext.getConstraints());
                }
            };
        }
    }

    private interface SpecialPropertyDeserializer {
        Object deserialize(JsonObject jsonObject, Optional<Ancestry> ancestry, ImmutableMap<String, Supplier<?>> otherProperties);
    }

    private interface SimpleSpecialPropertyDeserializer extends SpecialPropertyDeserializer {
        default Object deserialize(JsonObject jsonObject, Optional<Ancestry> ancestry, ImmutableMap<String, Supplier<?>> otherProperties) {
            return deserialize(jsonObject);
        }

        Object deserialize(JsonObject jsonObject);
    }
}
