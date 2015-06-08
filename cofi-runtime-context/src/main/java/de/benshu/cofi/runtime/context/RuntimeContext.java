package de.benshu.cofi.runtime.context;

import de.benshu.cofi.binary.deserialization.internal.AbstractBinaryModelContext;
import de.benshu.cofi.binary.internal.BinaryTypeDeclarationMixin;
import de.benshu.cofi.cofic.notes.Note;
import de.benshu.cofi.cofic.notes.async.Checker;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.runtime.Module;
import de.benshu.cofi.runtime.TypeDeclaration;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.TypeSystemImpl;
import de.benshu.cofi.types.impl.templates.AbstractTemplateTypeConstructor;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RuntimeContext extends AbstractBinaryModelContext<RuntimeContext> {
    private final Set<Module> modules = new HashSet<>();
    private final TypeSystemImpl<RuntimeContext> typeSystem;

    public RuntimeContext() {
        this.typeSystem = TypeSystemImpl.create(
                this::lookUpLangType,
                RuntimeTypeName.TAG,
                this::lookUpTopConstructor
        );
    }

    @Override
    protected TypeMixin<RuntimeContext, ?> bind(BinaryTypeDeclarationMixin typeDeclaration) {
        return TypeMixin.rebind(((TypeDeclaration) typeDeclaration).getType());
    }

    private TypeMixin<RuntimeContext, ?> lookUpLangType(String name) {
        return resolveQualifiedTypeName(Fqn.from("cofi", "lang", name));
    }

    private AbstractTemplateTypeConstructor<RuntimeContext> lookUpTopConstructor() {
        return (AbstractTemplateTypeConstructor<RuntimeContext>) lookUpLangType("Object");
    }

    public void load(Module module) {
        modules.add(module);
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

    @Override
    public TypeMixin<RuntimeContext, ?> resolveQualifiedTypeName(Fqn fqn) {
        final Module module = modules.stream()
                .filter(m -> m.getFqn().contains(fqn))
                .sorted((a, b) -> -a.getFqn().compareTo(b.getFqn()))
                .findFirst()
                .get();

        return tryResolveTypeInModule(module, module.getFqn().getRelativeNameOf(fqn)).get();
    }
}
