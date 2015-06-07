package de.benshu.cofi.runtime;

import de.benshu.cofi.binary.internal.Ancestry;
import de.benshu.cofi.runtime.internal.TypeReference;
import de.benshu.cofi.types.ProperType;
import de.benshu.jswizzle.data.Data;

import java.util.function.Supplier;

import static de.benshu.cofi.runtime.internal.Resolution.resolve;

public class LiteralValue implements Expression, LiteralValueAccessors {
    @Data
    final String literal;
    final Supplier<ProperType> type;

    public LiteralValue(
            Ancestry ancestry,
            String literal,
            TypeReference<ProperType> type) {

        final Ancestry ancestryIncludingMe = ancestry.append(this);

        this.literal = literal;
        this.type = resolve(ancestryIncludingMe, type);
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitLiteralValue(this);
    }

    @Override
    public ProperType getType() {
        return type.get();
    }

    @Override
    public String debug() {
        return literal;
    }
}
