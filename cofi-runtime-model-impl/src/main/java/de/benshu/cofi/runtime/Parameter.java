package de.benshu.cofi.runtime;

import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.binary.internal.Ancestry;
import de.benshu.cofi.binary.internal.Constructor;
import de.benshu.cofi.runtime.internal.TypeReference;
import de.benshu.cofi.types.ProperType;
import de.benshu.commons.core.Optional;
import de.benshu.jswizzle.data.Data;

import java.util.function.Supplier;

import static de.benshu.cofi.runtime.internal.Resolution.resolve;

@Data
public class Parameter implements VariableDeclaration, ParameterAccessors {
    final ImmutableSet<Annotation> annotations;
    final String name;
    @Data.Exclude
    final Supplier<ProperType> valueType;
    final boolean variableArity;
    final Optional<Expression> defaultValue;

    public Parameter(Ancestry ancestry,
                     ImmutableSet<Constructor<Annotation>> annotations,
                     String name,
                     TypeReference<ProperType> valueType,
                     boolean variableArity,
                     Optional<Constructor<Expression>> defaultValue) {

        final Ancestry ancestryIncludingMe = ancestry.append(this);

        this.annotations = ancestryIncludingMe.constructAll(annotations);
        this.name = name;
        this.valueType = resolve(ancestryIncludingMe, valueType);
        this.variableArity = variableArity;
        this.defaultValue = defaultValue.map(ancestryIncludingMe::construct);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String debug() {
        return name + " : " + valueType.get().debug();
    }

    public ProperType getValueType() {
        return valueType.get();
    }
}
