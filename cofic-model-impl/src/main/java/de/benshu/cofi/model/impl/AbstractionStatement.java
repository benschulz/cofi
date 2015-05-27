package de.benshu.cofi.model.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import de.benshu.cofi.parser.AstNodeConstructorMethod;
import de.benshu.cofi.parser.lexer.Token;

// FIXME abstraction statements with 2 or more pieces are completely broken
// Methods of Invocation should delegate to the last Piece and getPrimary()
// of a Piece should return the previous piece. The first Piece would have
// to return a MemberAccessExpression or something that shares an interface
// with MAE.
public class AbstractionStatement<X extends ModelContext<X>> extends Statement<X> implements FunctionInvocation<X> {
    public static class Parguments<X extends ModelContext<X>> extends AbstractModelNode<X> {
        @AstNodeConstructorMethod
        public static <X extends ModelContext<X>> Parguments<X> of(ImmutableList<ParameterImpl<X>> parameters, ImmutableList<ExpressionNode<X>> arguments) {
            return new Parguments<>(parameters, arguments);
        }

        public final ImmutableList<ParameterImpl<X>> parameters;
        public final ImmutableList<ExpressionNode<X>> arguments;

        private Parguments(ImmutableList<ParameterImpl<X>> parameters, ImmutableList<ExpressionNode<X>> arguments) {
            this.parameters = parameters;
            this.arguments = arguments;
        }

        @Override
        public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
            throw null;
        }

        @Override
        public <N, L extends N, D extends L, S extends N, E extends N, T extends E> N accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
            throw null;
        }
    }

    public static class Piece<X extends ModelContext<X>> extends AbstractModelNode<X> {
        @AstNodeConstructorMethod
        public static <X extends ModelContext<X>> Piece<X> of(Token name, Parguments<X> parguments, ImmutableList<Statement<X>> statements) {
            return new Piece<>(name, parguments, statements);
        }

        public final Token name;
        public final ImmutableList<ExpressionNode<X>> arguments;
        public final Closure<X> closure;

        public Piece(Token name, Parguments<X> parguments, ImmutableList<Statement<X>> statements) {
            this.name = name;
            this.arguments = parguments.arguments;
            this.closure = Closure.of(ImmutableList.of(Closure.Case.of(parguments.parameters, statements)));
        }

        @Override
        public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
            return visitor.visitAbstractionStatementPiece(this, aggregate);
        }

        @Override
        public <N, L extends N, D extends L, S extends N, E extends N, T extends E> N accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
            return transformer.transformAbstractionStatementPiece(this);
        }
    }

    @AstNodeConstructorMethod
    public static <X extends ModelContext<X>> AbstractionStatement<X> of(ImmutableList<AnnotationImpl<X>> annotations, NameImpl<X> qualifier, ImmutableList<Piece<X>> pieces) {
        return new AbstractionStatement<>(annotations, qualifier, pieces);
    }

    public final ImmutableList<AnnotationImpl<X>> annotations;
    public final NameImpl<X> qualifier;
    public final ImmutableList<Piece<X>> pieces;

    public AbstractionStatement(ImmutableList<AnnotationImpl<X>> annotations, NameImpl<X> qualifier, ImmutableList<Piece<X>> pieces) {
        this.annotations = annotations;
        this.qualifier = qualifier == null ? null : qualifier;
        this.pieces = pieces;
    }

    @Override
    public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
        return visitor.visitAbstractionStatement(this, aggregate);
    }

    @Override
    public <N, L extends N, D extends L, S extends N, E extends N, T extends E> S accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
        return transformer.transformAbstractionStatement(this);
    }

    public ImmutableList<Token> getName() {
        final ImmutableList.Builder<Token> builder = ImmutableList.builder();

        for (Piece<X> piece : pieces) {
            builder.add(piece.name);
        }

        return builder.build();
    }

    @Override
    public ImmutableList<ExpressionNode<X>> getArgs() {
        ImmutableList<ExpressionNode<X>> args = pieces.get(0).arguments;
        Iterable<ExpressionNode<X>> includingClosure = Iterables.concat(args.subList(1, args.size()),
                ImmutableList.of(pieces.get(0).closure));
        return ImmutableList.copyOf(includingClosure);
    }

}
