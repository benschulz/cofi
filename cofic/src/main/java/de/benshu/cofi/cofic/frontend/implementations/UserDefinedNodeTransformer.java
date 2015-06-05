package de.benshu.cofi.cofic.frontend.implementations;

import com.google.common.collect.Sets;
import de.benshu.cofi.model.impl.Closure;
import de.benshu.cofi.model.impl.ExpressionNode;
import de.benshu.cofi.model.impl.ExpressionStatement;
import de.benshu.cofi.model.impl.FunctionInvocationExpression;
import de.benshu.cofi.model.impl.LiteralExpression;
import de.benshu.cofi.model.impl.LocalVariableDeclaration;
import de.benshu.cofi.model.impl.MemberAccessExpression;
import de.benshu.cofi.model.impl.ModelContext;
import de.benshu.cofi.model.impl.ModelTransformer;
import de.benshu.cofi.model.impl.NameExpression;
import de.benshu.cofi.model.impl.Statement;
import de.benshu.cofi.model.impl.ThisExpression;
import de.benshu.cofi.model.impl.TransformationContext;
import de.benshu.cofi.model.impl.TransformedUserDefinedNode;
import de.benshu.cofi.model.impl.TransformedUserDefinedNodes;
import de.benshu.cofi.model.impl.UserDefinedExpression;
import de.benshu.cofi.model.impl.UserDefinedStatement;

import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import static de.benshu.commons.core.streams.Collectors.list;
import static de.benshu.commons.core.streams.Collectors.set;

public class UserDefinedNodeTransformer<X extends ModelContext<X>> implements ModelTransformer<
        X,
        TransformedUserDefinedNodes<X, ?>,
        TransformedUserDefinedNodes<X, ?>,
        TransformedUserDefinedNodes<X, ?>,
        TransformedUserDefinedNodes<X, Statement<X>>,
        TransformedUserDefinedNodes<X, ExpressionNode<X>>,
        TransformedUserDefinedNodes<X, ?>> {

    private final TransformationContext<X> transformationContext;

    public UserDefinedNodeTransformer(TransformationContext<X> transformationContext) {
        this.transformationContext = transformationContext;
    }

    @Override
    public TransformedUserDefinedNodes<X, ExpressionNode<X>> transformClosure(Closure<X> closure) {
        return TransformedUserDefinedNodes.of(() -> Stream.of(new TransformedUserDefinedNode<>(closure, (x, t) -> true)));
    }

    @Override
    public TransformedUserDefinedNodes<X, Statement<X>> transformExpressionStatement(ExpressionStatement<X> expressionStatement) {
        return TransformedUserDefinedNodes.of(() -> transform(expressionStatement.expression).stream()
                .map(t -> {
                    ExpressionStatement<X> transformed = ExpressionStatement.of(
                            expressionStatement.annotations,
                            t.getTransformedNode()
                    );

                    return new TransformedUserDefinedNode<>(
                            transformed,
                            t.boundTest());
                }));
    }

    @Override
    public TransformedUserDefinedNodes<X, ExpressionNode<X>> transformFunctionInvocationExpression(FunctionInvocationExpression<X> functionInvocationExpression) {
        return TransformedUserDefinedNodes.of(() -> {
            final Set<List<TransformedUserDefinedNode<X, ExpressionNode<X>>>> product = Sets.cartesianProduct(
                    Stream.concat(
                            Stream.of(transform(functionInvocationExpression.primary).stream().collect(set())),
                            functionInvocationExpression.args.stream()
                                    .map(this::transform)
                                    .map(t -> t.stream().collect(set()))
                    ).collect(list()));

            return product.stream().map(c -> {
                FunctionInvocationExpression<X> transformed = FunctionInvocationExpression.of(
                        c.get(0).getTransformedNode(),
                        c.subList(1, c.size()).stream().map(TransformedUserDefinedNode::getTransformedNode).collect(list())
                );

                return new TransformedUserDefinedNode<>(transformed, c.stream()
                        .map(t -> t.<ExpressionNode<X>> boundTest())
                        .reduce((x, t) -> true, BiPredicate::and));
            });
        });
    }

    @Override
    public TransformedUserDefinedNodes<X, ExpressionNode<X>> transformLiteralExpression(LiteralExpression<X> literalExpression) {
        return TransformedUserDefinedNodes.of(() -> Stream.of(new TransformedUserDefinedNode<>(literalExpression, (x, t) -> true)));
    }

    @Override
    public TransformedUserDefinedNodes<X, Statement<X>> transformLocalVariableDeclaration(LocalVariableDeclaration<X> localVariableDeclaration) {
        return TransformedUserDefinedNodes.of(() -> transform(localVariableDeclaration.value).stream()
                .map(t -> {
                    LocalVariableDeclaration<X> transformed = LocalVariableDeclaration.of(
                            localVariableDeclaration.annotations,
                            localVariableDeclaration.modifiers,
                            localVariableDeclaration.name,
                            localVariableDeclaration.type,
                            t.getTransformedNode()
                    );

                    return new TransformedUserDefinedNode<>(
                            transformed,
                            t.boundTest());
                }));
    }

    @Override
    public TransformedUserDefinedNodes<X, ExpressionNode<X>> transformMemberAccessExpression(MemberAccessExpression<X> memberAccessExpression) {
        return TransformedUserDefinedNodes.of(() -> transform(memberAccessExpression.primary).stream()
                .map(t -> {
                    MemberAccessExpression<X> transformed = MemberAccessExpression.of(t.getTransformedNode(), memberAccessExpression.name);

                    return new TransformedUserDefinedNode<>(
                            transformed,
                            t.boundTest());
                }));
    }

    @Override
    public TransformedUserDefinedNodes<X, ExpressionNode<X>> transformNameExpression(NameExpression<X> nameExpression) {
        return TransformedUserDefinedNodes.of(() -> Stream.of(new TransformedUserDefinedNode<>(nameExpression, (x, t) -> true)));
    }

    @Override
    public TransformedUserDefinedNodes<X, ExpressionNode<X>> transformThisExpr(ThisExpression<X> thisExpression) {
        return TransformedUserDefinedNodes.of(() -> Stream.of(new TransformedUserDefinedNode<>(thisExpression, (x, t) -> true)));
    }

    @Override
    public TransformedUserDefinedNodes<X, ExpressionNode<X>> transformUserDefinedExpression(UserDefinedExpression<X> userDefinedExpression) {
        return TransformedUserDefinedNodes.of(() -> userDefinedExpression.transform(transformationContext)
                .flatMap(outer -> transform(outer.getTransformedNode()).stream()
                        .map(inner -> new TransformedUserDefinedNode<>(
                                inner.getTransformedNode(),
                                outer.unboundTest().and(inner.unboundTest())))));
    }

    @Override
    public TransformedUserDefinedNodes<X, Statement<X>> transformUserDefinedStatement(UserDefinedStatement<X> userDefinedStatement) {
        return TransformedUserDefinedNodes.of(() -> userDefinedStatement.transform(transformationContext)
                .flatMap(outer -> transform(outer.getTransformedNode()).stream()
                        .map(inner -> new TransformedUserDefinedNode<>(
                                inner.getTransformedNode(),
                                outer.unboundTest().and(inner.unboundTest())))));
    }
}
