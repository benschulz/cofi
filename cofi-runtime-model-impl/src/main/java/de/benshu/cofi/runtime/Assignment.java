package de.benshu.cofi.runtime;

import de.benshu.cofi.runtime.internal.Ancestry;
import de.benshu.cofi.runtime.internal.Constructor;
import de.benshu.jswizzle.data.Data;

@Data
public class Assignment implements Statement, AssignmentAccessors {
    final Expression lhs;
    final Expression rhs;

    public Assignment(
            Ancestry ancestry,
            Constructor<Expression> lhs,
            Constructor<Expression> rhs) {

        final Ancestry ancestryIncludingMe = ancestry.append(this);

        this.lhs = ancestryIncludingMe.construct(lhs);
        this.rhs = ancestryIncludingMe.construct(rhs);
    }

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitAssignment(this);
    }

    @Override
    public String debug() {
        return lhs.debug() + " := " + rhs.debug();
    }
}
