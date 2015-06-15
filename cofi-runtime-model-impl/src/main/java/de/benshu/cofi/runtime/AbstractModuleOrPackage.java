package de.benshu.cofi.runtime;

import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.binary.internal.Ancestry;
import de.benshu.cofi.binary.internal.Constructor;
import de.benshu.cofi.binary.internal.MemoizingSupplier;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.types.TemplateTypeConstructor;
import de.benshu.cofi.types.TypeList;
import de.benshu.jswizzle.data.Data;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Data
abstract class AbstractModuleOrPackage<S extends AbstractModuleOrPackage<S>> implements Multiton, AbstractModuleOrPackageAccessors<S> {
    final ImmutableSet<Annotation> annotations;
    final transient Fqn fqn;
    @Data.Exclude
    final transient Supplier<TemplateTypeConstructor> type;
    final Supplier<TypeList<TemplateTypeConstructor>> supertypes = MemoizingSupplier.of(() -> getType().getSupertypes());
    final TypeBody body;
    final ImmutableSet<AbstractTypeDeclaration<?>> topLevelDeclarations;
    final ImmutableSet<Package> subpackages;

    public AbstractModuleOrPackage(
            Ancestry ancestry,
            ImmutableSet<Constructor<Annotation>> annotations,
            Fqn fqn,
            Function<S, TemplateTypeConstructor> type,
            Constructor<TypeBody> body,
            ImmutableSet<Constructor<AbstractTypeDeclaration<?>>> topLevelDeclarations,
            ImmutableSet<Constructor<Package>> subpackages) {

        final Ancestry ancestryIncludingMe = ancestry.append(this);

        this.annotations = ancestryIncludingMe.constructAll(annotations);
        this.fqn = fqn;
        this.type = MemoizingSupplier.of(() -> type.apply(self()));
        this.body = ancestryIncludingMe.construct(body);
        this.topLevelDeclarations = ancestryIncludingMe.constructAll(topLevelDeclarations);
        this.subpackages = ancestryIncludingMe.constructAll(subpackages);
    }

    protected abstract S self();

    @Override
    public final TemplateTypeConstructor getType() {
        return type.get();
    }

    @Override
    public final TypeBody getBody() {
        return body;
    }

    public final Fqn getFqn() {
        return fqn;
    }

    @Override
    public final String getName() {
        return getFqn().getLocalName();
    }

    @Override
    public final Stream<? extends MemberDeclaration> getMemberDeclarations() {
        return Stream.of(
                subpackages.stream(),
                body.getMemberDeclarations(),
                topLevelDeclarations.stream()
                        .map(d -> d.getCompanion().<AbstractTypeDeclaration<?>>map(c -> c).getOrReturn(d))
        ).flatMap(s -> s);
    }
}
