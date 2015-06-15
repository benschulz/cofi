package de.benshu.cofi.runtime;

import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.binary.internal.Ancestry;
import de.benshu.cofi.binary.internal.Constructor;
import de.benshu.cofi.runtime.internal.TypeParameterListReference;
import de.benshu.cofi.types.TemplateTypeConstructor;
import de.benshu.cofi.types.TypeParameterList;
import de.benshu.jswizzle.data.Data;

import java.util.function.Function;
import java.util.function.Supplier;

import static de.benshu.cofi.runtime.internal.Resolution.resolve;

@Data
public class Package extends AbstractModuleOrPackage<Package> implements Singleton, PackageAccessors {
    final String name;
    @Data.Exclude
    final Supplier<TypeParameterList> typeParameters;

    public Package(
            Ancestry ancestry,
            ImmutableSet<Constructor<Annotation>> annotations,
            String name,
            TypeParameterListReference typeParameters,
            Function<Package, TemplateTypeConstructor> type,
            Constructor<TypeBody> body,
            ImmutableSet<Constructor<AbstractTypeDeclaration<?>>> topLevelDeclarations,
            ImmutableSet<Constructor<Package>> subpackages) {
        super(ancestry, annotations, ancestry.closest(AbstractModuleOrPackage.class).get().getFqn().getChild(name), type, body, topLevelDeclarations, subpackages);

        final Ancestry ancestryIncludingMe = ancestry.append(this);

        this.name = getName();
        this.typeParameters = resolve(ancestryIncludingMe, typeParameters);
    }

    @Override
    protected Package self() {
        return this;
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
    public TypeParameterList getTypeParameters() {
        return typeParameters.get();
    }

    @Override
    public String debug() {
        return "package " + getFqn();
    }
}
