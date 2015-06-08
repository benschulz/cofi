package de.benshu.cofi.runtime;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.binary.internal.Ancestry;
import de.benshu.cofi.binary.internal.Constructor;
import de.benshu.commons.core.Debuggable;
import de.benshu.jswizzle.data.Data;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class TypeBody implements ModelNode, TypeBodyAccessors {
    final ImmutableList<Containable> elements;

    public TypeBody(
            Ancestry ancestry,
            ImmutableList<Constructor<Containable>> elements) {

        final Ancestry ancestryIncludingMe = ancestry.append(this);

        this.elements = ancestryIncludingMe.constructAll(elements);
    }

    Stream<MemberDeclaration> getMemberDeclarations() {
        return elements.stream()
                .filter(MemberDeclaration.class::isInstance)
                .map(MemberDeclaration.class::cast);
    }

    public static abstract class Containable implements ModelNode {}

    @Override
    public String debug() {
        return "{ " + elements.stream()
                .filter(e -> e instanceof PropertyDeclaration || e instanceof MethodDeclaration || e instanceof TypeDeclaration)
                .map(Debuggable::debug)
                .collect(Collectors.joining("; ")) + " }";
    }
}
