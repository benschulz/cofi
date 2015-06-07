package de.benshu.cofi.cofic.model.binary;

import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.benshu.cofi.binary.deserialization.internal.TypeParser;
import de.benshu.cofi.binary.internal.Ancestry;
import de.benshu.cofi.binary.internal.Constructor;
import de.benshu.cofi.binary.internal.MemoizingSupplier;
import de.benshu.cofi.cofic.model.binary.internal.TypeParameterListReference;
import de.benshu.cofi.cofic.model.binary.internal.TypeReference;
import de.benshu.cofi.cofic.model.binary.internal.UnboundType;
import de.benshu.cofi.cofic.model.binary.internal.UnboundTypeParameterList;
import de.benshu.cofi.cofic.model.common.LocalTypeName;
import de.benshu.cofi.cofic.model.common.TypeTags;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.TypeParameterImpl;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.commons.core.Optional;

import java.io.Reader;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.Maps.immutableEntry;
import static de.benshu.commons.core.Optional.none;
import static de.benshu.commons.core.Optional.some;
import static de.benshu.commons.core.streams.Collectors.map;
import static de.benshu.commons.core.streams.Collectors.set;
import static java.util.Arrays.asList;

public class BinaryDeserializer {
    public BinaryModule deserialize(Reader reader) {
        return new Deserialization(reader).getResult();
    }

    private class Deserialization {
        private final Gson gson;
        private final TypeParser typeParser;
        private final BinaryModule result;

        public Deserialization(Reader reader) {
            this.gson = new GsonBuilder().setPrettyPrinting()
                    .registerTypeHierarchyAdapter(BinaryModelNode.class, (JsonDeserializer<BinaryModelNode>) this::deserializeModelNode)
                    .registerTypeAdapter(Constructor.class, (JsonDeserializer<Constructor<?>>) this::deserializeConstructor)
                    .registerTypeAdapter(Fqn.class, (JsonDeserializer<Fqn>) this::deserializeFqn)
//                    .registerTypeAdapter(Optional.class, (JsonDeserializer<Optional<?>>) this::deserializeOptional)
//                    .registerTypeAdapter(Supplier.class, (JsonDeserializer<Supplier<?>>) this::deserializeSupplier)
                    .registerTypeAdapter(ImmutableList.class, (JsonDeserializer<ImmutableList<?>>) this::deserializeList)
                    .registerTypeAdapter(ImmutableSet.class, (JsonDeserializer<ImmutableSet<?>>) this::deserializeSet)
                    .registerTypeAdapter(TypeParameterListReference.class, (JsonDeserializer<TypeParameterListReference>) this::deserializeTypeParameterListReference)
                    .registerTypeAdapter(TypeReference.class, (JsonDeserializer<TypeReference>) this::deserializeTypeReference)
                    .create();

            this.typeParser = new TypeParser(
                    new TypeParser.FqnResolver() {
                        @Override
                        public <X extends TypeSystemContext<X>> TypeMixin<X, ?> resolve(X context, Fqn fqn) {
                            throw null;
                        }
                    },
                    new TypeParser.Namer() {
                        @Override
                        public IndividualTags name(String name) {
                            return IndividualTags.of(TypeTags.NAME, LocalTypeName.create(name));
                        }

                        @Override
                        public String getNameOf(TypeParameterImpl<?> typeParameter) {
                            return typeParameter.getTags().get(TypeTags.NAME).toDescriptor();
                        }
                    });

            this.result = gson.fromJson(reader, BinaryModule.class);
        }

        public BinaryModule getResult() {
            return result;
        }

        private BinaryModelNode deserializeModelNode(JsonElement json, Type requiredType, JsonDeserializationContext context) {
            return deserializeModelNode(none(), json, requiredType, context);
        }

        private BinaryModelNode deserializeModelNode(Optional<Ancestry> ancestry, JsonElement json, Type requiredType, JsonDeserializationContext context) {
            final JsonObject jsonObject = json.getAsJsonObject();

            final Class<? extends BinaryModelNode> type = determineTypeOf(jsonObject, requiredType).asSubclass(BinaryModelNode.class);
            checkArgument(TypeToken.of(requiredType).getRawType().isAssignableFrom(type));

            return deserializeModelNode(ancestry, context, type, jsonObject);
        }

        private Class<?> determineTypeOf(JsonObject jsonObject, Type requiredType) {
            final Class<?> rawRequiredType = TypeToken.of(requiredType).getRawType();

            if ((rawRequiredType.getModifiers() & Modifier.ABSTRACT) == 0) {
                return rawRequiredType;
            }

            // TODO Come up with an ingenious way of doing this. (Past you had some ideas, but didn't bother writing them down. Sorry for that.)
            if (jsonObject.has("companion")) {
                if (jsonObject.get("companion").isJsonPrimitive())
                    return jsonObject.has("singleton") ? BinarySingletonCompation.class : BinaryMultitonCompanion.class;

                if (jsonObject.has("trait"))
                    return BinaryTrait.class;

                if (jsonObject.has("union"))
                    return BinaryUnion.class;

                return BinaryClass.class;
            } else if(jsonObject.has("supertypes"))
                return BinaryObjectSingleton.class;

            if (jsonObject.has("pieces"))
                return BinaryMethodDeclaration.class;

            if (jsonObject.has("traits"))
                return BinaryPropertyDeclaration.class;

            throw new AssertionError("TODO: " + jsonObject.entrySet().stream().map(Map.Entry::getKey).collect(set()));
        }

        private <N extends BinaryModelNode> N deserializeModelNode(Optional<Ancestry> ancestry, JsonDeserializationContext context, Class<N> requiredType, JsonObject jsonObject) {
            final java.lang.reflect.Constructor<N> constructor = getConstructorOf(requiredType);

            class NodeDeserialization {
                private final ImmutableMap<String, Supplier<?>> arguments = Stream.of(constructor.getParameters())
                        .map(this::deserializeArgument)
                        .collect(map());

                private Map.Entry<String, Supplier<?>> deserializeArgument(java.lang.reflect.Parameter parameter) {
                    return immutableEntry(parameter.getName(), MemoizingSupplier.of(() ->
                                    ancestry.map(a -> (Object) a)
                                            .filter(a -> parameter.getType() == Ancestry.class)
                                            .getOrSupply(() -> {
                                                final String parameterName = parameter.getName();
                                                final JsonObject debuggerOhDebugger = jsonObject;
                                                final JsonElement jsonArgument = checkNotNull(debuggerOhDebugger.get(parameterName));
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
        private <N extends BinaryModelNode> java.lang.reflect.Constructor<N> getConstructorOf(Class<N> modelNodeType) {
            return (java.lang.reflect.Constructor<N>) getOnlyElement(asList(modelNodeType.getDeclaredConstructors()));
        }

        private Constructor<?> deserializeConstructor(JsonElement json, Type requiredType, JsonDeserializationContext context) {
            final Type constsructedType = ((ParameterizedType) requiredType).getActualTypeArguments()[0];

            return ancestry -> deserializeModelNode(some(ancestry), json, constsructedType, context);
        }

        private Fqn deserializeFqn(JsonElement json, Type requiredType, JsonDeserializationContext context) {
            return deserializeFqn(json.getAsString());
        }

        private Fqn deserializeFqn(String canonicalString) {
            final Iterable<String> ids = Splitter.on('.').omitEmptyStrings().split(canonicalString);
            return Fqn.from(Iterables.toArray(ids, String.class));
        }

        private TypeParameterListReference deserializeTypeParameterListReference(JsonElement json, Type type, JsonDeserializationContext context) {
            final String typeParametersString = json.getAsString();

            return x -> new UnboundTypeParameterList() {
                @Override
                public <X extends TypeSystemContext<X>> TypeParameterListImpl<X> bind(X context) {
                    return typeParser.in(context, x).parseTypeParameters(typeParametersString);
                }
            };
        }

        private TypeReference deserializeTypeReference(JsonElement json, Type type, JsonDeserializationContext context) {
            final String typeString = json.getAsString();

            return x -> new UnboundType() {
                @Override
                public <X extends TypeSystemContext<X>> TypeMixin<X, ?> bind(X context) {
                    return typeParser.in(context, x).parseType(typeString);
                }
            };
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
    }
}
