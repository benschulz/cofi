package de.benshu.cofi.runtime;

import de.benshu.cofi.runtime.internal.Ancestry;
import de.benshu.cofi.runtime.internal.TypeReference;
import de.benshu.cofi.types.ProperType;

import java.util.function.Supplier;

public class RootExpression implements Expression {
    final boolean root = true;

    private final transient Supplier<ProperType> type;

    public RootExpression(
            Ancestry ancestry,
            TypeReference<ProperType> type) {

        final Ancestry ancestryIncludingMe = ancestry.append(this);

        this.type = ancestryIncludingMe.resolve(type);
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitRootExpression(this);
    }

    @Override
    public ProperType getType() {
        return type.get();
    }

    @Override
    public String debug() {
        return "<root>";
    }
}
