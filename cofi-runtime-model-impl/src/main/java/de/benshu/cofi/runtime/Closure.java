package de.benshu.cofi.runtime;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.binary.internal.Ancestry;
import de.benshu.cofi.binary.internal.Constructor;
import de.benshu.cofi.runtime.internal.TypeReference;
import de.benshu.cofi.types.ProperType;
import de.benshu.commons.core.Debuggable;
import de.benshu.jswizzle.data.Data;

import java.util.function.Supplier;

import static de.benshu.cofi.runtime.internal.Resolution.resolve;
import static java.util.stream.Collectors.joining;

public class Closure implements Expression, ClosureAccessors {
    @Data
    final ImmutableList<Case> cases;
    final Supplier<ProperType> type;

    public Closure(
            Ancestry ancestry,
            ImmutableList<Constructor<Case>> cases,
            TypeReference<ProperType> type) {

        final Ancestry ancestryIncludingMe = ancestry.append(this);

        this.cases = ancestryIncludingMe.constructAll(cases);
        this.type = resolve(ancestryIncludingMe, type);
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitClosure(this);
    }

    @Override
    public ProperType getType() {
        return type.get();
    }

    @Override
    public String debug() {
        return "{ " + type.get().debug() + " }";
    }

    @Data
    public static class Case implements ModelNode, ClosureCaseAccessors {
        final ImmutableList<Parameter> parameters;
        final ImmutableList<Statement> body;

        public Case(
                Ancestry ancestry,
                ImmutableList<Constructor<Parameter>> parameters,
                ImmutableList<Constructor<Statement>> body) {

            final Ancestry ancestryIncludingMe = ancestry.append(this);

            this.parameters = ancestryIncludingMe.constructAll(parameters);
            this.body = ancestryIncludingMe.constructAll(body);
        }

        @Override
        public String debug() {
            return parameters.isEmpty() ? "{ -> … }"
                    : "{ " + parameters.stream().map(Debuggable::debug).collect(joining(", ")) + " -> … }";
        }
    }
}
