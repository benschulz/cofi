package de.benshu.cofi.types.impl.members;

import de.benshu.cofi.types.MemberSort;
import de.benshu.cofi.types.bound.Member;
import de.benshu.cofi.types.impl.ProperTypeConstructorMixin;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.Substitutions;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.impl.declarations.InterpretedMemberDescriptor;
import de.benshu.cofi.types.impl.declarations.InterpretedMethodDescriptor;

import java.util.function.Supplier;

public class LazyMethod<X extends TypeSystemContext<X>> extends AbstractLazyMember<X, MethodImpl<X>> implements Member<X> {
    public static <X extends TypeSystemContext<X>> LazyMethod<X> createMethod(X context, ProperTypeMixin<X, ?> owner, InterpretedMethodDescriptor<X> descriptor) {
        return new LazyMethod<>(context, descriptor.getName(), () -> MethodImpl.createMethod(context, owner, descriptor));
    }

    public LazyMethod(X context, String name, Supplier<MethodImpl<X>> supplier) {
        super(context, name, supplier);
    }

    @Override
    public AbstractMember<X> intersectWith(AbstractConstraints<X> contextualConstraints, AbstractMember<X> otherMember) {
        return new LazyMethod<>(context, getName(), () -> get().intersectWith(contextualConstraints, get(otherMember)));
    }

    @Override
    public AbstractMember<X> refine(AbstractConstraints<X> contextualConstraints, InterpretedMemberDescriptor<X> descriptors) {
        return new LazyMethod<>(context, getName(), () -> get().refine(contextualConstraints, descriptors));
    }

    @Override
    public AbstractMember<X> bequest(ProperTypeConstructorMixin<X, ?, ?> child) {
        return new LazyMethod<>(context, getName(), () -> get().bequest(child));
    }

    @Override
    public AbstractMember<X> substitute(ProperTypeMixin<X, ?> newOwner, Substitutions<X> substitutions) {
        return new LazyMethod<>(context, getName(), () -> get().substitute(newOwner, substitutions));
    }

    @Override
    public MemberSort getSort() {
        return MemberSort.METHOD;
    }
}
