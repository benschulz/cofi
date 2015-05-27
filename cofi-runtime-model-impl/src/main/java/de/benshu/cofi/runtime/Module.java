package de.benshu.cofi.runtime;

import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.runtime.internal.Ancestry;
import de.benshu.cofi.runtime.internal.Constructor;
import de.benshu.cofi.runtime.internal.TypeParameterListReference;
import de.benshu.cofi.types.TemplateTypeConstructor;
import de.benshu.cofi.types.TypeParameterList;
import de.benshu.jswizzle.data.Data;

import java.util.function.Supplier;
import java.util.stream.Stream;

public class Module implements Singleton, ModuleAccessors {
    @Data
    final Fqn fqn;
    @Data
    final Package pakkage;

    final transient Supplier<ObjectSingleton> root;
    final transient Supplier<TypeParameterList> typeParameters;

    public Module(
            Fqn fqn,
            Constructor<Package> pakkage,
            Supplier<ObjectSingleton> root,
            TypeParameterListReference typeParameters) {

        final Ancestry ancestryIncludingMe = Ancestry.first(this);

        this.fqn = fqn;
        this.typeParameters = ancestryIncludingMe.resolve(typeParameters);
        this.pakkage = ancestryIncludingMe.construct(pakkage);
        this.root = root;
    }

    @Override
    public <R> R accept(NamedEntityVisitor<R> visitor) {
        return visitor.visitModule(this);
    }

    @Override
    public TypeBody getBody() {
        throw null;
    }

    @Override
    public TemplateTypeConstructor getType() {
        throw null;
    }

    @Override
    public String getName() {
        throw null;
    }

    @Override
    public TypeParameterList getTypeParameters() {
        return typeParameters.get();
    }

    @Override
    public Stream<? extends MemberDeclaration> getMemberDeclarations() {
        throw null;
    }

    public ObjectSingleton getRoot() {
        return root.get();
    }

    @Override
    public String debug() {
        return "module " + fqn;
    }
}
