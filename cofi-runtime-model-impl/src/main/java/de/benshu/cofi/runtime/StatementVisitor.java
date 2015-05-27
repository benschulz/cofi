package de.benshu.cofi.runtime;

public interface StatementVisitor<R> {
    default R visit(Statement statement) {
        return statement.accept(this);
    }

    R visitAssignment(Assignment assignment);

    R visitExpressionStatement(ExpressionStatement expressionStatement);

    R visitLocalVariableDeclaration(LocalVariableDeclaration localVariableDeclaration);
}
