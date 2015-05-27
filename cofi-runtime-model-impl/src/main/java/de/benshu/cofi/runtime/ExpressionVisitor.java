package de.benshu.cofi.runtime;

public interface ExpressionVisitor<R> {
    default R visit(Expression expression) {
        return expression.accept(this);
    }

    R visitClosure(Closure closure);

    R visitFunctionInvocation(FunctionInvocation functionInvocation);

    R visitLiteralValue(LiteralValue literalValue);

    R visitMemberAccess(MemberAccess memberAccess);

    R visitNameExpression(NameExpression nameExpression);

    R visitRootExpression(RootExpression rootExpression);

    R visitThisExpression(ThisExpression thisExpression);
}
