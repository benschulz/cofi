package de.benshu.cofi.runtime;

import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.binary.internal.Ancestry;
import de.benshu.cofi.binary.internal.Constructor;
import de.benshu.jswizzle.data.Data;

@Data
public class ExpressionStatement implements Statement, ExpressionStatementAccessors {
    final ImmutableSet<Annotation> annotations;
    final Expression expression;

    public ExpressionStatement(
            Ancestry ancestry,
            ImmutableSet<Constructor<Annotation>> annotations,
            Constructor<Expression> expression) {

        final Ancestry ancestryIncludingMe = ancestry.append(this);

        this.annotations = ancestryIncludingMe.constructAll(annotations);
        this.expression = ancestry.construct(expression);
    }

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitExpressionStatement(this);
    }

    @Override
    public String debug() {
        return expression.debug();
    }
}
