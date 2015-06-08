package de.benshu.cofi.types.impl.declarations;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.declarations.source.SourceType;
import de.benshu.cofi.types.impl.intersections.AbstractIntersectionTypeConstructor;
import de.benshu.cofi.types.tags.IndividualTags;

import java.util.function.BiFunction;

public interface IntersectionTypeDeclaration<X extends TypeSystemContext<X>>
        extends TypeDeclaration<X, AbstractIntersectionTypeConstructor<X>>,
                ParameterizedTypeDeclaration<X, AbstractIntersectionTypeConstructor<X>>,
                HierarchyDeclaration<X, AbstractIntersectionTypeConstructor<X>> {

    static <X extends TypeSystemContext<X>> IntersectionTypeDeclaration<X> lazy(
            BiFunction<X, AbstractIntersectionTypeConstructor<X>, TypeParameterListImpl<X>> parametersSupplier,
            BiFunction<X, AbstractIntersectionTypeConstructor<X>, ImmutableList<SourceType<X>>> elementsSupplier,
            BiFunction<X, AbstractIntersectionTypeConstructor<X>, IndividualTags> tagsSupplier
    ) {
        return new IntersectionTypeDeclaration<X>() {
            @Override
            public <O> O supplyParameters(X context, AbstractIntersectionTypeConstructor<X> bound, Interpreter<TypeParameterListImpl<X>, O> interpreter) {
                return interpreter.interpret(parametersSupplier.apply(context, bound), context.getChecker());
            }

            @Override
            public <O> O supplyHierarchy(X context, AbstractIntersectionTypeConstructor<X> bound, Interpreter<ImmutableList<SourceType<X>>, O> interpreter) {
                return interpreter.interpret(elementsSupplier.apply(context, bound), context.getChecker());
            }

            @Override
            public <O> O supplyTags(X context, AbstractIntersectionTypeConstructor<X> bound, Interpreter<IndividualTags, O> interpreter) {
                return interpreter.interpret(tagsSupplier.apply(context, bound), context.getChecker());
            }
        };
    }
}
