package de.benshu.cofi.runtime;

import de.benshu.cofi.binary.internal.Ancestry;
import de.benshu.cofi.runtime.internal.TypeReference;
import de.benshu.cofi.types.ProperType;
import de.benshu.jswizzle.data.Data;

import java.util.function.Supplier;

import static de.benshu.cofi.runtime.internal.Resolution.resolve;

public class NameExpression implements Expression, NameExpressionAccessors {
    @Data
    final String name;
    final Supplier<ProperType> type;

    public NameExpression(
            Ancestry ancestry,
            String name,
            TypeReference<ProperType> type) {

        final Ancestry ancestryIncludingMe = ancestry.append(this);

        this.name = name;
        this.type = resolve(ancestryIncludingMe, type);
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitNameExpression(this);
    }

    public ProperType getType() {
        return type.get();
    }

    @Override
    public String debug() {
        return name;
    }
}
