package de.benshu.cofi.types.impl.declarations;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.declarations.source.SourceType;
import de.benshu.cofi.types.impl.unions.AbstractUnionTypeConstructor;
import de.benshu.cofi.types.tags.IndividualTags;

import java.util.function.BiFunction;

public interface UnionTypeDeclaration<X extends TypeSystemContext<X>>
        extends TypeDeclaration<X, AbstractUnionTypeConstructor<X>>,
                ParameterizedTypeDeclaration<X, AbstractUnionTypeConstructor<X>>,
                HierarchyDeclaration<X, AbstractUnionTypeConstructor<X>> {

    static <X extends TypeSystemContext<X>> UnionTypeDeclaration<X> lazy(
            BiFunction<X, AbstractUnionTypeConstructor<X>, TypeParameterListImpl<X>> parametersSupplier,
            BiFunction<X, AbstractUnionTypeConstructor<X>, ImmutableList<SourceType<X>>> elementsSupplier,
            BiFunction<X, AbstractUnionTypeConstructor<X>, IndividualTags> tagsSupplier
    ) {
        return new UnionTypeDeclaration<X>() {
            @Override
            public <O> O supplyParameters(X context, AbstractUnionTypeConstructor<X> bound, Interpreter<TypeParameterListImpl<X>, O> interpreter) {
                return interpreter.interpret(parametersSupplier.apply(context, bound), context.getChecker());
            }

            @Override
            public <O> O supplyHierarchy(X context, AbstractUnionTypeConstructor<X> bound, Interpreter<ImmutableList<SourceType<X>>, O> interpreter) {
                return interpreter.interpret(elementsSupplier.apply(context, bound), context.getChecker());
            }

            @Override
            public <O> O supplyTags(X context, AbstractUnionTypeConstructor<X> bound, Interpreter<IndividualTags, O> interpreter) {
                return interpreter.interpret(tagsSupplier.apply(context, bound), context.getChecker());
            }
        };
    }
}
