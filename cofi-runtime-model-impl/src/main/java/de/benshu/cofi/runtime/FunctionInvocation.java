package de.benshu.cofi.runtime;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.binary.internal.Ancestry;
import de.benshu.cofi.binary.internal.Constructor;
import de.benshu.cofi.runtime.internal.TypeReference;
import de.benshu.cofi.types.ProperType;
import de.benshu.commons.core.Debuggable;
import de.benshu.jswizzle.data.Data;

import java.util.function.Supplier;
import java.util.stream.Collectors;

import static de.benshu.cofi.runtime.internal.Resolution.resolve;

public class FunctionInvocation implements Expression, FunctionInvocationAccessors {
    @Data
    final Expression primary;
    @Data
    final ImmutableList<Expression> arguments;
    final Supplier<ProperType> type;

    public FunctionInvocation(
            Ancestry ancestry,
            Constructor<Expression> primary,
            ImmutableList<Constructor<Expression>> arguments,
            TypeReference<ProperType> type) {

        final Ancestry ancestryIncludingMe = ancestry.append(this);

        this.primary = ancestryIncludingMe.construct(primary);
        this.arguments = ancestryIncludingMe.constructAll(arguments);
        this.type = resolve(ancestryIncludingMe, type);
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitFunctionInvocation(this);
    }

    @Override
    public ProperType getType() {
        return type.get();
    }

    @Override
    public String debug() {
        return primary.debug() + "(" + arguments.stream().map(Debuggable::debug).collect(Collectors.joining(", ")) + ")";
    }
}
