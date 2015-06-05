package de.benshu.cofi.cofic.frontend.infer;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.inference.Inferencer;
import de.benshu.cofi.inference.Parametrization;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.Substitutions;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

public class ExpressionTreeInferencer<T> {
    private final Pass pass;
    private final/* Stack */ Deque<OverloadedExpressionInferencer<T>> expressions = new ArrayDeque<>();
    private final/* Stack */ Deque<OverloadedInvocationInferencer> invocations = new ArrayDeque<>();

    public ExpressionTreeInferencer(Pass pass) {
        this.pass = pass;
    }

    public void beginInvocation(InferFunctionInvocation<T> functionInvocation) {
        OverloadedExpressionInferencer<T> primary = expressions.pop();
        OverloadedInvocationInferencer inferencer = new OverloadedInvocationInferencer(pass, primary, functionInvocation);
        expressions.push(inferencer);
        invocations.push(inferencer);
    }

    /**
     * The end of an invocation can not be determined automagically (example: {@code a(b.c)} vs
     * {@code a(b).c}).
     */
    public void endInvocation() {
        OverloadedInvocationInferencer inferencer = invocations.pop();

        ImmutableList.Builder<OverloadedExpressionInferencer<T>> args = ImmutableList.builder();
        while (expressions.peek() != inferencer) {
            args.add(expressions.pop());
        }

        inferencer.setArgs(args.build().reverse());
    }

    public void accessMember(InferMemberAccess<T> memberAccess) {
        OverloadedExpressionInferencer<T> primary = expressions.pop();
        OverloadedMemberAccessInferencer<T> inferencer = new OverloadedMemberAccessInferencer<T>(pass, primary, memberAccess);
        expressions.push(inferencer);
    }

    public void pushValue(ProperTypeMixin<Pass, ?> type) {
        expressions.push(new OverloadedSimpleValueInferencer<T>(type));
    }

    public void pushClosure(InferClosure<T> closure) {
        expressions.push(new OverloadedClosureInferencer<T>(closure));
    }

    public Optional<T> infer(Pass pass, AbstractConstraints<Pass> contextConstraints, ProperTypeMixin<Pass, ?> context, T aggregate) {
        Preconditions.checkState(expressions.size() == 1);

        OverloadedExpressionInferencer<T> exprInferencer = expressions.pop();
        return infer(pass, contextConstraints, context, exprInferencer, aggregate);
    }

    private Optional<T> infer(Pass pass, AbstractConstraints<Pass> contextConstraints, ProperTypeMixin<Pass, ?> context, OverloadedExpressionInferencer<T> exprInferencer, T aggregate) {
        for (ExpressionInferencer<T> inferencer : exprInferencer.unoverload()) {
            TypeParameterListImpl<Pass> parameters = TypeParameterListImpl.createTrivial(inferencer.getTypeArgCount(), pass);
            AbstractConstraints<Pass> initialConstraints = AbstractConstraints.trivial(pass, contextConstraints, parameters);

            for (Parametrization<Pass, T> parametrization : inferencer.inferGeneric(pass, parameters, 0, initialConstraints, context)) {
                Iterable<AbstractTypeList<Pass, ?>> typeArgOptions = new Inferencer<>(pass.getTypeSystem(), parametrization.getConstraints()).infer(pass);

                for (AbstractTypeList<Pass, ?> typeArgs : typeArgOptions) {
                    return Optional.of(parametrization.apply(Substitutions.ofThrough(parameters, typeArgs), aggregate));
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public String toString() {
        return "Invocations=" + invocations + " Expressions=" + expressions;
    }
}
