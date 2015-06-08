package de.benshu.cofi.binary.deserialization.internal;

import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import de.benshu.cofi.binary.internal.BinaryModuleMixin;
import de.benshu.cofi.binary.internal.BinaryTypeDeclarationMixin;
import de.benshu.cofi.types.impl.ProperTypeConstructorMixin;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.tags.IndividualTag;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import static de.benshu.commons.core.streams.Collectors.singleOrNone;

public abstract class AbstractBinaryModelContext<X extends AbstractBinaryModelContext<X>> implements BinaryModelContext<X> {
    public static IndividualTag<ProperTypeConstructorMixin<?, ?, ?>> ACCOMPANIED_TAG = IndividualTag.named("Accompanied").unambiguouslyDerivable();

    private Cache<BinaryModuleMixin, Cache<ImmutableList<String>, ProperTypeConstructorMixin<X, ?, ?>>> typeCache = CacheBuilder.newBuilder().build();

    protected abstract TypeMixin<X, ?> bind(BinaryTypeDeclarationMixin typeDeclaration);

    protected final Optional<ProperTypeConstructorMixin<X, ?, ?>> tryResolveTypeInModule(BinaryModuleMixin module, ImmutableList<String> relativeName) {
        final Cache<ImmutableList<String>, ProperTypeConstructorMixin<X, ?, ?>> moduleCache = computeIfAbsent(typeCache, module, () -> CacheBuilder.newBuilder().build());

        return Optional.ofNullable(moduleCache.getIfPresent(relativeName))
                .map(Optional::<ProperTypeConstructorMixin<X, ?, ?>>of)
                .orElseGet(() -> {
                    final Optional<ProperTypeConstructorMixin<X, ?, ?>> resolved = resolveIn(Optional.of(module), relativeName)
                            .map(this::bind)
                            .map(t -> t.getTags().tryGet(ACCOMPANIED_TAG).<TypeMixin<?, ?>>map(x -> x).getOrReturn(t))
                            .map(t -> (ProperTypeConstructorMixin<X, ?, ?>) t);

                    return resolved.map(t -> computeIfAbsent(moduleCache, relativeName, () -> t));
                });
    }

    private <K, V> V computeIfAbsent(Cache<K, V> cache, K key, Supplier<V> valueSupplier) {
        try {
            return cache.get(key, () -> valueSupplier.get());
        } catch (ExecutionException e) {
            throw Throwables.propagate(e);
        }
    }

    private Optional<BinaryTypeDeclarationMixin> resolveIn(Optional<BinaryTypeDeclarationMixin> typeDeclaration, ImmutableList<String> relativeName) {
        return typeDeclaration.flatMap(d -> relativeName.isEmpty()
                ? typeDeclaration
                : resolveIn(d.getMemberDeclarations()
                        .filter(m -> m.getName().equals(relativeName.get(0)))
                        .map(m -> ((BinaryTypeDeclarationMixin) m))
                        .collect(singleOrNone())
                        .asJavaOptional(),
                relativeName.subList(1, relativeName.size())));
    }
}
