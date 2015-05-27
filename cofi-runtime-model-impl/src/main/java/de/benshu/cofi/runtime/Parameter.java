package de.benshu.cofi.runtime;

import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.runtime.internal.Ancestry;
import de.benshu.cofi.runtime.internal.Constructor;
import de.benshu.cofi.runtime.internal.TypeReference;
import de.benshu.cofi.types.ProperType;
import de.benshu.commons.core.Optional;
import de.benshu.jswizzle.data.Data;

import java.util.function.Supplier;

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
        this.valueType = ancestryIncludingMe.resolve(valueType);
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
