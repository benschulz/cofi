package de.benshu.cofi.runtime.context.tags;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import de.benshu.cofi.runtime.MemberDeclaration;
import de.benshu.cofi.runtime.MethodDeclaration;
import de.benshu.cofi.types.tags.IndividualTag;
import de.benshu.cofi.types.tags.Tags;
import de.benshu.commons.core.Optional;

import java.util.stream.Stream;

import static de.benshu.commons.core.streams.Collectors.set;
import static java.util.stream.Collectors.collectingAndThen;

public class Implementations {
    public static final IndividualTag<Implementations> TAG = IndividualTag.named("Implementations")
            .semigroup(Implementations::combineWith, Implementations::deriveFrom);

    private static Optional<Implementations> deriveFrom(Tags tags) {
        return tags.tryGet(MemberDeclaration.TAG)
                .flatMap(d -> Optional.cast(MethodDeclaration.class, d))
                .flatMap(Implementations::from);
    }

    private static Optional<Implementations> from(MethodDeclaration methodDeclaration) {
        return methodDeclaration.getBody()
                .map(d -> new Implementations(ImmutableSet.of(methodDeclaration)));
    }

    private final ImmutableSet<MethodDeclaration> declarations;

    public Implementations(ImmutableSet<MethodDeclaration> declarations) {
        this.declarations = declarations;
    }

    private Implementations combineWith(Implementations other) {
        return Stream.concat(declarations.stream(), other.declarations.stream()).collect(collectingAndThen(set(), Implementations::new));
    }

    public ImmutableSet<MethodDeclaration> getDeclarations() {
        return declarations;
    }

    public MethodDeclaration getEffectiveImplementation() {
        return Iterables.getOnlyElement(declarations);
    }
}
