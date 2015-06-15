package de.benshu.cofi.runtime;

import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.binary.internal.Ancestry;
import de.benshu.cofi.binary.internal.BinaryModuleMixin;
import de.benshu.cofi.binary.internal.Constructor;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.runtime.internal.TypeParameterListReference;
import de.benshu.cofi.types.TemplateTypeConstructor;
import de.benshu.cofi.types.TypeParameterList;
import de.benshu.jswizzle.data.Data;

import java.util.function.Function;
import java.util.function.Supplier;

import static de.benshu.cofi.runtime.internal.Resolution.resolve;

public class Module extends AbstractModuleOrPackage<Module> implements ModuleAccessors, BinaryModuleMixin {
    @Data
    final Fqn fqn;
    final transient Supplier<ObjectSingleton> root;
    final Supplier<TypeParameterList> typeParameters;

    public Module(
            ImmutableSet<Constructor<Annotation>> annotations,
            Fqn fqn,
            TypeParameterListReference typeParameters,
            Function<Module, TemplateTypeConstructor> type,
            Constructor<TypeBody> body,
            ImmutableSet<Constructor<AbstractTypeDeclaration<?>>> topLevelDeclarations,
            ImmutableSet<Constructor<Package>> subpackages,
            Supplier<ObjectSingleton> root) {
        super(Ancestry.empty(), annotations, fqn, type, body, topLevelDeclarations, subpackages);

        final Ancestry ancestryIncludingMe = Ancestry.first(this);

        this.fqn = fqn;
        this.typeParameters = resolve(ancestryIncludingMe, typeParameters);
        this.root = root;
    }

    @Override
    protected Module self() {
        return this;
    }

    @Override
    public <R> R accept(NamedEntityVisitor<R> visitor) {
        return visitor.visitModule(this);
    }

    @Override
    public TypeParameterList getTypeParameters() {
        return typeParameters.get();
    }

    public ObjectSingleton getRoot() {
        return root.get();
    }

    @Override
    public String debug() {
        return "module " + getFqn();
    }
}
