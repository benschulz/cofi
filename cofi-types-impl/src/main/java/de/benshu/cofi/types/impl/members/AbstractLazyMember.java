package de.benshu.cofi.types.impl.members;

import de.benshu.cofi.types.Member;
import de.benshu.cofi.types.impl.ProperTypeConstructorMixin;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.tags.Tags;

import java.util.function.Supplier;

public abstract class AbstractLazyMember<X extends TypeSystemContext<X>, M extends AbstractMember<X>> extends AbstractMember<X> {
    static <X extends TypeSystemContext<X>> AbstractMember<X> get(AbstractMember<X> member) {
        return member instanceof AbstractLazyMember
                ? ((AbstractLazyMember<X, ?>) member).get()
                : member;
    }

    private final String name;

    private Supplier<M> supplier;
    private volatile M member;

    protected AbstractLazyMember(X context, String name, Supplier<M> supplier) {
        super(context);

        this.name = name;
        this.supplier = supplier;
    }

    @Override
    public String getName() {
        return name;
    }

    protected final M get() {
        if (member == null) {
            synchronized (this) {
                if (member == null) {
                    member = supplier.get();
                    supplier = null;
                }
            }
        }

        return member;
    }

    @Override
    public ProperTypeMixin<X, ?> getOwner() {
        return get().getOwner();
    }

    @Override
    public Tags getTags() {
        return get().getTags();
    }

    @Override
    public ProperTypeConstructorMixin<X, ?, ?> getType() {
        return get().getType();
    }

    @Override
    public Member unbind() {
        return get().unbind();
    }
}
