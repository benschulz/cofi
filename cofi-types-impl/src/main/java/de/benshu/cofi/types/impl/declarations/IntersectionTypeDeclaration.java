package de.benshu.cofi.types.impl.declarations;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.tags.IndividualTags;

import java.util.function.Function;

public interface IntersectionTypeDeclaration<X extends TypeSystemContext<X>>
        extends TypeDeclaration<X>,
                ParameterizedTypeDeclaration<X>,
                HierarchyDeclaration<X> {

    static <X extends TypeSystemContext<X>> IntersectionTypeDeclaration<X> lazy(
            Function<X, TypeParameterListImpl<X>> parametersSupplier,
            Function<X, ImmutableList<SourceType<X>>> elementsSupplier,
            Function<X, IndividualTags> tagsSupplier
    ) {
        return new IntersectionTypeDeclaration<X>() {
            @Override
            public <O> O supplyTags(X context, Interpreter<IndividualTags, O> interpreter) {
                return interpreter.interpret(tagsSupplier.apply(context), context.getChecker());
            }

            @Override
            public <O> O supplyParameters(X context, Interpreter<TypeParameterListImpl<X>, O> interpreter) {
                return interpreter.interpret(parametersSupplier.apply(context), context.getChecker());
            }

            @Override
            public <O> O supplyHierarchy(X context, Interpreter<ImmutableList<SourceType<X>>, O> interpreter) {
                return interpreter.interpret(elementsSupplier.apply(context), context.getChecker());
            }
        };
    }
}
