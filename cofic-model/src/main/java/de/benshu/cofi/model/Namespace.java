package de.benshu.cofi.model;

import de.benshu.cofi.types.bound.Member;
import de.benshu.cofi.types.bound.Type;

public interface Namespace<X> {
    interface Resolution<X> {
        Type<X, ?> getType();

        boolean isMember();

        Member<X> getMember();
    }
}
