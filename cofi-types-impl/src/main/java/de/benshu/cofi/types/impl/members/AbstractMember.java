package de.benshu.cofi.types.impl.members;

import de.benshu.cofi.types.bound.Member;
import de.benshu.cofi.types.impl.ProperTypeConstructorMixin;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.Substitutions;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.impl.declarations.InterpretedMemberDescriptor;
import de.benshu.cofi.types.impl.declarations.InterpretedMethodDescriptor;
import de.benshu.cofi.types.impl.declarations.InterpretedPropertyDescriptor;
import de.benshu.cofi.types.impl.declarations.InterpretedTypeDescriptor;
import de.benshu.cofi.types.impl.tags.TaggedMixin;
import de.benshu.cofi.types.tags.Tags;

public abstract class AbstractMember<X extends TypeSystemContext<X>> implements Member<X>, TaggedMixin {
    public static <X extends TypeSystemContext<X>> AbstractMember<X> create(X context, ProperTypeMixin<X, ?> owner, InterpretedMemberDescriptor<X> descriptor) {
        switch (descriptor.getSort()) {
            case METHOD:
                return LazyMethod.createMethod(context, owner, (InterpretedMethodDescriptor<X>) descriptor);
            case PROPERTY:
                return PropertyImpl.createProperty(context, owner, (InterpretedPropertyDescriptor<X>) descriptor);
            case TYPE:
                InterpretedTypeDescriptor<X> typeDescriptor = (InterpretedTypeDescriptor<X>) descriptor;
                return TypeMemberImpl.createTypeMember(context, owner, typeDescriptor);
            default:
                throw new AssertionError();
        }
    }

    final X context;

    AbstractMember(X context) {
        this.context = context;
    }

    public abstract ProperTypeMixin<X, ?> getOwner();

    @Override
    public abstract Tags getTags();

    @Override
    public abstract ProperTypeConstructorMixin<X, ?, ?> getType();

    public abstract AbstractMember<X> intersectWith(AbstractConstraints<X> contextualConstraints, AbstractMember<X> otherMember);

    public abstract AbstractMember<X> refine(AbstractConstraints<X> contextualConstraints, InterpretedMemberDescriptor<X> descriptors);

    public abstract AbstractMember<X> bequest(ProperTypeConstructorMixin<X, ?, ?> child);

    public abstract AbstractMember<X> substitute(ProperTypeMixin<X, ?> newOwner, Substitutions<X> substitutions);

    @Override
    public String toString() {
        return getOwner() + "." + getName();
    }

    public abstract de.benshu.cofi.types.Member unbind();
}
