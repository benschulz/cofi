package de.benshu.cofi.runtime;

import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.runtime.internal.Ancestry;
import de.benshu.cofi.runtime.internal.Constructor;
import de.benshu.cofi.runtime.internal.TypeReference;
import de.benshu.cofi.types.ProperType;
import de.benshu.commons.core.Debuggable;
import de.benshu.commons.core.Optional;
import de.benshu.jswizzle.data.Data;

import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Annotation implements ModelNode, AnnotationAccessors {
    final Supplier<ProperType> type;
    @Data
    final Optional<Expression> value;
    @Data
    final ImmutableSet<PropertyAssignment> propertyAssignments;

    public Annotation(
            Ancestry ancestry,
            TypeReference<ProperType> type,
            Optional<Constructor<Expression>> value,
            ImmutableSet<Constructor<PropertyAssignment>> propertyAssignments) {

        final Ancestry ancestryIncludingMe = ancestry.append(this);

        this.type = ancestryIncludingMe.resolve(type);
        this.value = value.map(ancestryIncludingMe::construct);
        this.propertyAssignments = ancestryIncludingMe.constructAll(propertyAssignments);
    }

    @Override
    public String debug() {
        return "@" + type.get().debug() + value.map((t) -> Stream.of((Debuggable) t))
                .or(() -> Optional.from(propertyAssignments).map(as -> as.stream().map(Debuggable.class::cast)))
                .map(ds -> ds.map(Debuggable::debug).collect(Collectors.joining(", ")))
                .map(p -> "(" + p + ")")
                .getOrReturn("");
    }

    public ProperType getType() {
        return type.get();
    }

    public static abstract class PropertyAssignment implements ModelNode {}
}
