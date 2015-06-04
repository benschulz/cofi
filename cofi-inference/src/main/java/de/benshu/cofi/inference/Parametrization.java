package de.benshu.cofi.inference;

import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.Substitutions;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;

/**
 * Parametrization<Pass>of an expression (tree).
 */
public interface Parametrization<X extends TypeSystemContext<X>, T> {
    /**
     * Inserts the type arguments at the corresponding member accesses.
     *
     * @param substitutions
     * @param aggregate
     */
    T apply(Substitutions<X> substitutions, T aggregate);

    /**
     * @return the constraints the parametrization is meeting
     */
    AbstractConstraints<X> getConstraints();

    /**
     * @return type of the expression
     */
    default ProperTypeMixin<X, ?> getImplicitType() {
        return getExplicitType();
    }

    /**
     * @return type of the expression
     */
    ProperTypeMixin<X, ?> getExplicitType();
}
