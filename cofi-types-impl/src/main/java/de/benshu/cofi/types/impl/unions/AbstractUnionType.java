package de.benshu.cofi.types.impl.unions;

import com.google.common.base.Joiner;
import de.benshu.cofi.types.ProperTypeSort;
import de.benshu.cofi.types.bound.Type;
import de.benshu.cofi.types.impl.AbstractProperType;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.impl.constraints.Monosemous;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.tags.Tagger;
import de.benshu.cofi.types.tags.Tags;
import de.benshu.cofi.types.bound.ProperTypeVisitor;
import de.benshu.cofi.types.bound.UnionType;

public abstract class AbstractUnionType<X extends TypeSystemContext<X>, S extends AbstractUnionType<X, S>>
        extends AbstractProperType<X, S>
        implements UnionType<X, S> {

    private final Tags tags;

    protected AbstractUnionType(X context, Tagger tagger) {
        super(context);

        this.tags = tagger.tag(this);
    }

    @Override
    public Tags getTags() {
        return tags;
    }

    @Override
    public <T> T accept(ProperTypeVisitor<X, T> visitor) {
        return visitor.visitUnionType(this);
    }

    @Override
    public ProperTypeSort getSort() {
        return ProperTypeSort.UNION;
    }

    @Override
    public abstract AbstractTypeList<X, ProperTypeMixin<X, ?>> getElements();

    @Override
    public final AbstractConstraints<X> establishSubtype(TypeMixin<X, ?> other, Monosemous<X> cs) {
        return other.establishSupertype(this, cs);
    }

    @Override
    public final AbstractConstraints<X> establishSubtypeGeneric(TypeMixin<X, ?> other, Monosemous<X> cs) {
        return getElements().stream()
                .map(e -> e.establishSubtype(other, cs))
                .reduce(cs, AbstractConstraints::and);
    }

    @Override
    public final AbstractConstraints<X> establishSupertype(AbstractUnionType<X, ?> other, Monosemous<X> cs) {
        return other.establishSubtype(this, cs);
    }

    @Override
    public final AbstractConstraints<X> establishSupertype(TypeMixin<X, ?> other, Monosemous<X> cs) {
        return other.establishSubtype(this, cs);
    }

    @Override
    public final AbstractConstraints<X> establishSupertypeGeneric(TypeMixin<X, ?> other, Monosemous<X> cs) {
        return getElements().stream()
                .map(e -> e.establishSubtype(other, cs))
                .reduce(AbstractConstraints.all(), AbstractConstraints::or);
    }

    @Override
    public String debug() {
        return "(" + Joiner.on(" | ").join(getElements().mapAny(Type::debug)) + ")";
    }
}