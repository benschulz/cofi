package de.benshu.cofi.types.impl.intersections;

import com.google.common.collect.ImmutableMap;
import de.benshu.cofi.types.ProperTypeSort;
import de.benshu.cofi.types.bound.IntersectionType;
import de.benshu.cofi.types.bound.ProperTypeVisitor;
import de.benshu.cofi.types.impl.AbstractProperType;
import de.benshu.cofi.types.impl.Bottom;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.Substitutable;
import de.benshu.cofi.types.impl.Substitutions;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.impl.constraints.Monosemous;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.members.AbstractMember;
import de.benshu.cofi.types.impl.tags.Tagger;
import de.benshu.cofi.types.impl.unions.AbstractUnionType;
import de.benshu.cofi.types.tags.Tags;
import de.benshu.commons.core.Optional;

public abstract class AbstractIntersectionType<X extends TypeSystemContext<X>, S extends AbstractIntersectionType<X, S>>
        extends AbstractProperType<X, S>
        implements IntersectionType<X, S>,
                   Substitutable<X, S> {

    private final Tags tags;

    public AbstractIntersectionType(X context, Tagger tagger) {
        super(context);

        this.tags = tagger.tag(this);
    }

    @Override
    public Tags getTags() {
        return tags;
    }

    @Override
    public abstract AbstractTypeList<X, ProperTypeMixin<X, ?>> getElements();

    @Override
    public <T> T accept(ProperTypeVisitor<X, T> visitor) {
        return visitor.visitIntersectionType(this);
    }

    @Override
    public ProperTypeSort getSort() {
        return ProperTypeSort.INTERSECTION;
    }

    @Override
    public final AbstractConstraints<X> establishSubtypeGeneric(TypeMixin<X, ?> other, Monosemous<X> cs) {
        return getElements().stream()
                .map(e -> e.establishSubtype(other, cs))
                .reduce(AbstractConstraints.all(), AbstractConstraints::or);
    }

    @Override
    public AbstractConstraints<X> establishSupertype(Bottom<X> other, Monosemous<X> cs) {
        return cs;
    }

    @Override
    public AbstractConstraints<X> establishSupertype(AbstractUnionType<X, ?> other, Monosemous<X> cs) {
        return other.establishSubtype(this, cs);
    }

    @Override
    public final AbstractConstraints<X> establishSupertypeGeneric(TypeMixin<X, ?> other, Monosemous<X> cs) {
        return getElements().stream()
                .map(e -> e.establishSupertype(other, cs))
                .reduce(cs, AbstractConstraints::and);
    }

    @Override
    public final Optional<AbstractMember<X>> lookupMember(String name) {
        AbstractTypeList<X, ProperTypeMixin<X, ?>> elements = getElements();
        if (elements.size() == 1)
            return elements.get(0).lookupMember(name);

        throw null;
    }

    @Override
    public final ImmutableMap<String, AbstractMember<X>> getMembers() {
        throw null;
    }

    @Override
    public abstract S substitute(Substitutions<X> substitutions);
}
