package de.benshu.cofi.model.impl;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.cofic.model.common.LocalTypeName;
import de.benshu.cofi.cofic.model.common.TypeTags;
import de.benshu.cofi.parser.AstNodeConstructorMethod;
import de.benshu.cofi.types.Variance;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.UnboundTypeParameterList;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.impl.declarations.Interpreter;
import de.benshu.cofi.types.impl.declarations.TypeParameterListDeclaration;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.commons.core.Pair;

public class TypeParameters<X extends ModelContext<X>> extends AbstractModelNode<X> {
    public static <X extends ModelContext<X>> TypeParameters<X> none() {
        return of(ImmutableList.of(), ImmutableList.of());
    }

    @AstNodeConstructorMethod
    public static <X extends ModelContext<X>> TypeParameters<X> of(ImmutableList<TypeParamDecl<X>> declarations, ImmutableList<ImmutableList<TypeExpression<X>>> constraints) {
        return new TypeParameters<>(declarations, constraints);
    }

    public final ImmutableList<TypeParamDecl<X>> declarations;
    public final ImmutableList<ImmutableList<TypeExpression<X>>> constraints;
    private final UnboundTypeParameterList<X> params;

    private TypeParameters(ImmutableList<TypeParamDecl<X>> declarations,
                           ImmutableList<ImmutableList<TypeExpression<X>>> constraints) {
        this.declarations = declarations == null ? ImmutableList.<TypeParamDecl<X>>of() : declarations;
        this.constraints = constraints == null ? ImmutableList.<ImmutableList<TypeExpression<X>>>of() : constraints;

        final ImmutableList.Builder<Pair<Variance, IndividualTags>> builder = ImmutableList.builder();
        for (TypeParamDecl<X> decl : this.declarations) {
            builder.add(Pair.of(decl.variance, IndividualTags.of(TypeTags.NAME, LocalTypeName.create(decl.name.getLexeme()))));
        }
        final ImmutableList<Pair<Variance, IndividualTags>> parameterDeclarations = builder.build();

        this.params = TypeParameterListImpl.create(new TypeParameterListDeclaration<X>() {
            @Override
            public <O> O supplyParameters(X context, Interpreter<ImmutableList<Pair<Variance, IndividualTags>>, O> interpreter) {
                return interpreter.interpret(parameterDeclarations, context.getChecker());
            }

            @Override
            public <O> O supplyConstraints(X context, TypeParameterListImpl<X> bound, Interpreter<AbstractConstraints<X>, O> interpreter) {
                return interpreter.interpret(context.lookUpConstraintsOf(TypeParameters.this), context.getChecker());
            }
        });
    }

    @Override
    public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
        return visitor.visitTypeParameters(this, aggregate);
    }

    @Override
    public <N, L extends N, D extends L, S extends N, E extends N, T extends N> N accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
        return transformer.transformTypeParameters(this);
    }

    public TypeParameterListImpl<X> bind(X context) {
        return params.bind(context);
    }
}
