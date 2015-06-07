package de.benshu.cofi.runtime;

import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.binary.internal.Ancestry;
import de.benshu.cofi.binary.internal.Constructor;
import de.benshu.cofi.binary.internal.MemoizingSupplier;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.runtime.internal.TypeParameterListReference;
import de.benshu.cofi.types.TemplateTypeConstructor;
import de.benshu.cofi.types.TypeList;
import de.benshu.cofi.types.TypeParameterList;
import de.benshu.jswizzle.data.Data;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static de.benshu.cofi.runtime.internal.Resolution.resolve;

@Data
public class Package implements NamedEntity, Singleton, PackageAccessors, TypeDeclaration, MemberDeclaration {
    final ImmutableSet<Annotation> annotations;
    final transient Fqn fqn;
    final String name;
    @Data.Exclude
    final transient Supplier<TemplateTypeConstructor> type;
    @Data.Exclude
    final Supplier<TypeParameterList> typeParameters;
    @Data.Exclude
    final Supplier<TypeList<TemplateTypeConstructor>> supertypes = MemoizingSupplier.of(() -> getType().getSupertypes());
    final TypeBody body;
    final ImmutableSet<AbstractTypeDeclaration<?>> topLevelDeclarations;
    final ImmutableSet<Package> subpackages;

    public Package(Ancestry ancestry,
                   ImmutableSet<Constructor<Annotation>> annotations,
                   String name,
                   TypeParameterListReference typeParameters,
                   Function<Package, TemplateTypeConstructor> type,
                   Constructor<TypeBody> body,
                   ImmutableSet<Constructor<AbstractTypeDeclaration<?>>> topLevelDeclarations,
                   ImmutableSet<Constructor<Package>> subpackages) {

        final Ancestry ancestryIncludingMe = ancestry.append(this);

        this.annotations = ancestryIncludingMe.constructAll(annotations);
        this.fqn = ancestry.closest(Package.class).map(Package::getFqn)
                // TODO eliminate the getParent() when modules become a real thing
                .getOrSupply(() -> ancestry.closest(Module.class).get().getFqn().getParent())
                .getChild(name);
        this.name = name;
        this.typeParameters = resolve(ancestryIncludingMe, typeParameters);
        this.type = MemoizingSupplier.of(() -> type.apply(this));
        this.body = ancestryIncludingMe.construct(body);
        this.topLevelDeclarations = ancestryIncludingMe.constructAll(topLevelDeclarations);
        this.subpackages = ancestryIncludingMe.constructAll(subpackages);
    }

    @Override
    public <R> R accept(NamedEntityVisitor<R> visitor) {
        return visitor.visitPackage(this);
    }

    @Override
    public <R> R accept(MemberDeclarationVisitor<R> visitor) {
        return visitor.visitTypeDeclaration(this);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public TemplateTypeConstructor getType() {
        return type.get();
    }

    @Override
    public TypeParameterList getTypeParameters() {
        return typeParameters.get();
    }

    @Override
    public Stream<? extends MemberDeclaration> getMemberDeclarations() {
        return Stream.of(
                subpackages.stream(),
                body.getMemberDeclarations(),
                topLevelDeclarations.stream()
                        .map(d -> d.getCompanion().<AbstractTypeDeclaration<?>>map(c -> c).getOrReturn(d))
        ).flatMap(s -> s);
    }

    @Override
    public TypeBody getBody() {
        throw null;
    }

    @Override
    public String debug() {
        return "package " + fqn;
    }
}
