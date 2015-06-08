package de.benshu.cofi.types.impl.declarations;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.declarations.source.SourceMemberDescriptors;
import de.benshu.cofi.types.impl.declarations.source.SourceType;
import de.benshu.cofi.types.impl.templates.AbstractTemplateTypeConstructor;
import de.benshu.cofi.types.tags.IndividualTags;

import java.util.function.BiFunction;

public interface TemplateTypeDeclaration<X extends TypeSystemContext<X>>
        extends TypeDeclaration<X, AbstractTemplateTypeConstructor<X>>,
                ParameterizedTypeDeclaration<X, AbstractTemplateTypeConstructor<X>>,
                HierarchyDeclaration<X, AbstractTemplateTypeConstructor<X>>,
                MemberDeclaration<X, AbstractTemplateTypeConstructor<X>> {

    static <X extends TypeSystemContext<X>> TemplateTypeDeclaration<X> memoizing(
            BiFunction<X, AbstractTemplateTypeConstructor<X>, TypeParameterListImpl<X>> parametersSupplier,
            BiFunction<X, AbstractTemplateTypeConstructor<X>, ImmutableList<SourceType<X>>> supertypesSupplier,
            BiFunction<X, AbstractTemplateTypeConstructor<X>, SourceMemberDescriptors<X>> memberDescriptorsSupplier,
            BiFunction<X, AbstractTemplateTypeConstructor<X>, IndividualTags> tagsSupplier
    ) {
        return new TemplateTypeDeclaration<X>() {
            private X memoizationContext;
            private Object parameters;
            private Object hierarchy;
            private Object members;
            private Object tags;

            @Override
            public <O> O supplyParameters(X context, AbstractTemplateTypeConstructor<X> bound, Interpreter<TypeParameterListImpl<X>, O> interpreter) {
                guard(context);
                if (parameters == null)
                    parameters = interpreter.interpret(parametersSupplier.apply(context, bound), context.getChecker());
                return (O) parameters;
            }

            @Override
            public <O> O supplyHierarchy(X context, AbstractTemplateTypeConstructor<X> bound, Interpreter<ImmutableList<SourceType<X>>, O> interpreter) {
                guard(context);
                if (hierarchy == null)
                    hierarchy = interpreter.interpret(supertypesSupplier.apply(context, bound), context.getChecker());
                return (O) hierarchy;
            }

            @Override
            public <O> O supplyMembers(X context, AbstractTemplateTypeConstructor<X> bound, Interpreter<SourceMemberDescriptors<X>, O> interpreter) {
                guard(context);
                if (members == null)
                    members = interpreter.interpret(memberDescriptorsSupplier.apply(context, bound), context.getChecker());
                return (O) members;
            }

            @Override
            public <O> O supplyTags(X context, AbstractTemplateTypeConstructor<X> bound, Interpreter<IndividualTags, O> interpreter) {
                guard(context);
                if (tags == null)
                    tags = interpreter.interpret(tagsSupplier.apply(context, bound), context.getChecker());
                return (O) tags;
            }

            private void guard(X context) {
                if (memoizationContext != context) {
                    parameters = null;
                    hierarchy = null;
                    members = null;
                    tags = null;
                    memoizationContext = context;
                }
            }
        };
    }
}
