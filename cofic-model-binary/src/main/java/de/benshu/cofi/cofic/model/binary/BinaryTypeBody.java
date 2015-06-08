package de.benshu.cofi.cofic.model.binary;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.binary.internal.Ancestry;
import de.benshu.cofi.binary.internal.Constructor;

import java.util.stream.Stream;

public class BinaryTypeBody implements BinaryModelNode {
    final ImmutableList<Containable> elements;

    public BinaryTypeBody(
            Ancestry ancestry,
            ImmutableList<Constructor<Containable>> elements) {

        final Ancestry ancestryIncludingMe = ancestry.append(this);

        this.elements = ancestryIncludingMe.constructAll(elements);
    }

    Stream<BinaryMemberDeclaration> getMemberDeclarations() {
        return elements.stream()
                .filter(BinaryMemberDeclaration.class::isInstance)
                .map(BinaryMemberDeclaration.class::cast);
    }

    public static abstract class Containable implements BinaryModelNode {}

}
