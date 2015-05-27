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
public class LocalVariableDeclaration implements VariableDeclaration, Statement, LocalVariableDeclarationAccessors {
    final ImmutableSet<Annotation> annotations;
    final String name;
    @Data.Exclude
    final Supplier<ProperType> valueType;
    final Optional<Expression> initialValue;

    public LocalVariableDeclaration(
            Ancestry ancestry,
            ImmutableSet<Constructor<Annotation>> annotations,
            String name,
            TypeReference<ProperType> valueType,
            Optional<Constructor<Expression>> initialValue) {

        final Ancestry ancestryIncludingMe = ancestry.append(this);

        this.annotations = ancestryIncludingMe.constructAll(annotations);
        this.name = name;
        this.valueType = ancestryIncludingMe.resolve(valueType);
        this.initialValue = initialValue.map(ancestryIncludingMe::construct);
    }

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitLocalVariableDeclaration(this);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ProperType getValueType() {
        return valueType.get();
    }

    @Override
    public String debug() {
        return name + " : " + valueType.get().debug() + initialValue.map(v -> " := " + v.debug()).getOrReturn("");
    }
}
