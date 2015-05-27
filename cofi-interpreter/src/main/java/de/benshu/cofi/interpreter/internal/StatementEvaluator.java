package de.benshu.cofi.interpreter.internal;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.runtime.Assignment;
import de.benshu.cofi.runtime.Expression;
import de.benshu.cofi.runtime.ExpressionStatement;
import de.benshu.cofi.runtime.LocalVariableDeclaration;
import de.benshu.cofi.runtime.MemberDeclaration;
import de.benshu.cofi.runtime.MethodDeclaration;
import de.benshu.cofi.runtime.Statement;
import de.benshu.cofi.runtime.StatementVisitor;
import de.benshu.cofi.types.Member;

public class StatementEvaluator {
    private final ModuleInterpretation moduleInterpretation;

    public StatementEvaluator(ModuleInterpretation moduleInterpretation) {
        this.moduleInterpretation = moduleInterpretation;
    }

    public void evaluate(EvaluationContext context, Statement statement) {
        new Evaluation(context, statement).perform();
    }

    private class Evaluation implements StatementVisitor<Void> {
        private final EvaluationContext context;
        private final Statement statement;

        public Evaluation(EvaluationContext context, Statement statement) {
            this.context = context;
            this.statement = statement;
        }

        public void perform() {
            visit(statement);
        }

        @Override
        public Void visitAssignment(Assignment assignment) {
            final CofiObject lhs = evaluateExpression(assignment.getLhs());
            final CofiObject rhs = evaluateExpression(assignment.getRhs());

            final Member set = lhs.getType().lookupMember("set").get();
            final MemberDeclaration setDeclaration = set.getTags().get(MemberDeclaration.TAG);

            FunctionEvaluator.forMethod(moduleInterpretation, lhs, (MethodDeclaration) setDeclaration)
                    .evaluate(ImmutableList.of(rhs));

            return null;
        }

        @Override
        public Void visitExpressionStatement(ExpressionStatement expressionStatement) {
            evaluateExpression(expressionStatement.getExpression());

            return null;
        }

        @Override
        public Void visitLocalVariableDeclaration(LocalVariableDeclaration localVariableDeclaration) {
            for (Expression expression : localVariableDeclaration.getInitialValue())
                context.put(localVariableDeclaration, evaluateExpression(expression));
            return null;
        }

        public CofiObject evaluateExpression(Expression expression) {
            return moduleInterpretation.getExpressionEvaluator().evaluate(context, expression);
        }
    }
}
