package de.benshu.cofi.runtime.context;

import de.benshu.cofi.binary.deserialization.internal.BinaryModelContext;
import de.benshu.cofi.cofic.notes.Note;
import de.benshu.cofi.cofic.notes.async.Checker;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.runtime.Module;
import de.benshu.cofi.runtime.NamedEntity;
import de.benshu.cofi.runtime.NamedEntityVisitor;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.TypeSystemImpl;
import de.benshu.cofi.types.impl.templates.AbstractTemplateTypeConstructor;

import java.util.Map;
import java.util.function.Supplier;

public class RuntimeContext implements BinaryModelContext<RuntimeContext> {
    private final Supplier<Module> module;
    private final FqnResolver fqnResolver;
    private final TypeSystemImpl<RuntimeContext> typeSystem;

    public RuntimeContext(Supplier<Module> module) {
        this.module = module;
        this.fqnResolver = new FqnResolver(module);

        this.typeSystem = TypeSystemImpl.create(
                this::lookUpLangType,
                RuntimeTypeName.TAG,
                this::lookUpTopConstructor
        );
    }

    private AbstractTemplateTypeConstructor<RuntimeContext> lookUpTopConstructor() {
        return (AbstractTemplateTypeConstructor<RuntimeContext>) lookUpLangType("Object");
    }

    private TypeMixin<RuntimeContext, ?> lookUpLangType(String name) {
        return TypeMixin.<RuntimeContext>rebind(fqnResolver.resolve("cofi", "lang", name).getType());
    }

    @Override
    public TypeSystemImpl<RuntimeContext> getTypeSystem() {
        return typeSystem;
    }

    @Override
    public Checker getChecker() {
        return check -> check.check().entrySet().stream()
                .map(Map.Entry::getValue)
                .map(Note::toString)
                .forEach(System.out::println);
    }

    public Module getModule() {
        return module.get();
    }

    @Override
    public TypeMixin<RuntimeContext, ?> resolveQualifiedTypeName(Fqn fqn) {
        return fqnResolver.resolve(fqn).accept(new NamedEntityVisitor<TypeMixin<RuntimeContext, ?>>() {
            @Override
            public TypeMixin<RuntimeContext, ?> defaultAction(NamedEntity namedEntity) {
                return TypeMixin.rebind(namedEntity.getType());
            }
        });
    }
}
