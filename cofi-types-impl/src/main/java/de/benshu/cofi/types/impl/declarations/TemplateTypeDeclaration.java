package de.benshu.cofi.types.impl.declarations;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.declarations.source.SourceMemberDescriptors;
import de.benshu.cofi.types.impl.declarations.source.SourceType;
import de.benshu.cofi.types.tags.IndividualTags;

import java.util.function.Function;

public interface TemplateTypeDeclaration<X extends TypeSystemContext<X>>
        extends TypeDeclaration<X>,
                ParameterizedTypeDeclaration<X>,
                HierarchyDeclaration<X>,
                MemberDeclaration<X> {

    static <X extends TypeSystemContext<X>> TemplateTypeDeclaration<X> memoizing(
            Function<X, TypeParameterListImpl<X>> parametersSupplier,
            Function<X, ImmutableList<SourceType<X>>> supertypesSupplier,
            Function<X, SourceMemberDescriptors<X>> memberDescriptorsSupplier,
            Function<X, IndividualTags> tagsSupplier
    ) {
        return new TemplateTypeDeclaration<X>() {
            private X memoizationContext;
            private Object parameters;
            private Object hierarchy;
            private Object members;
            private Object tags;

            @Override
            public <O> O supplyParameters(X context, Interpreter<TypeParameterListImpl<X>, O> interpreter) {
                guard(context);
                if (parameters == null)
                    parameters = interpreter.interpret(parametersSupplier.apply(context), context.getChecker());
                return (O) parameters;
            }

            @Override
            public <O> O supplyHierarchy(X context, Interpreter<ImmutableList<SourceType<X>>, O> interpreter) {
                guard(context);
                if (hierarchy == null)
                    hierarchy = interpreter.interpret(supertypesSupplier.apply(context), context.getChecker());
                return (O) hierarchy;
            }

            @Override
            public <O> O supplyMembers(X context, Interpreter<SourceMemberDescriptors<X>, O> interpreter) {
                guard(context);
                if (members == null)
                    members = interpreter.interpret(memberDescriptorsSupplier.apply(context), context.getChecker());
                return (O) members;
            }

            @Override
            public <O> O supplyTags(X context, Interpreter<IndividualTags, O> interpreter) {
                guard(context);
                if (tags == null)
                    tags = interpreter.interpret(tagsSupplier.apply(context), context.getChecker());
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
