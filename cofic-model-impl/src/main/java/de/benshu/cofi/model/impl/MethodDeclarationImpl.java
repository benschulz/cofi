package de.benshu.cofi.model.impl;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.model.MethodDeclaration;
import de.benshu.cofi.parser.AstNodeConstructorMethod;
import de.benshu.cofi.parser.lexer.Token;
import de.benshu.cofi.types.MemberSort;

import static de.benshu.commons.core.streams.Collectors.list;

public class MethodDeclarationImpl<X extends ModelContext<X>> extends MemberDeclarationImpl<X> implements AnnotatedNodeMixin<X>, MethodDeclaration<X> {
    @AstNodeConstructorMethod
    public static <X extends ModelContext<X>> MethodDeclarationImpl<X> of(ImmutableList<AnnotationImpl<X>> annotations,
                                                                          ImmutableList<ModifierImpl<X>> modifiers, ImmutableList<Piece<X>> pieces,
                                                                          TypeExpression<X> returnType, ImmutableList<Statement<X>> body) {
        return new MethodDeclarationImpl<>(annotations, modifiers, pieces, returnType, body);
    }

    public final ImmutableList<Piece<X>> pieces;
    public final TypeExpression<X> returnType;
    public final ImmutableList<Statement<X>> body;
    private final String name;

    private MethodDeclarationImpl(ImmutableList<AnnotationImpl<X>> annotations,
                                  ImmutableList<ModifierImpl<X>> modifiers, ImmutableList<Piece<X>> pieces,
                                  TypeExpression<X> returnType, ImmutableList<Statement<X>> body) {
        super(annotations, modifiers);

        this.returnType = returnType;
        this.pieces = pieces;
        this.body = body;

        final StringBuilder name = new StringBuilder();
        for (Piece<X> piece : this.pieces) {
            name.append(piece.name.getLexeme());
        }
        this.name = name.toString();
    }

    @Override
    public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
        return visitor.visitMethodDeclaration(this, aggregate);
    }

    @Override
    public <N, L extends N, D extends L, S extends N, E extends N, T extends N> L accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
        return transformer.transformMethodDeclaration(this);
    }

    @Override
    public MemberSort getSort() {
        return MemberSort.METHOD;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ImmutableList<ImmutableList<ParameterImpl<X>>> getParameters() {
        return pieces.stream().map(p -> p.params).collect(list());
    }

    @Override
    boolean isMember() {
        return true;
    }

    public static class Piece<X extends ModelContext<X>> extends AbstractModelNode<X> implements TypeParameterized<X> {
        @AstNodeConstructorMethod
        public static <X extends ModelContext<X>> Piece<X> of(Token name, TypeParameters<X> typeParameters, ImmutableList<ParameterImpl<X>> params) {
            return new Piece<>(name, typeParameters, params);
        }

        public final Token name;
        public final TypeParameters<X> typeParameters;
        public final ImmutableList<ParameterImpl<X>> params;

        private Piece(Token name, TypeParameters<X> typeParameters,
                      ImmutableList<ParameterImpl<X>> params) {
            this.name = name;
            this.typeParameters = typeParameters;
            this.params = params;
        }

        @Override
        public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
            return visitor.visitMethodDeclarationPiece(this, aggregate);
        }

        @Override
        public <N, L extends N, D extends L, S extends N, E extends N, T extends N> N accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
            return transformer.transformMethodDeclarationPiece(this);
        }

        @Override
        public TypeParameters<X> getTypeParameters() {
            return typeParameters;
        }
    }
}
