package de.benshu.cofi.cofic.frontend.namespace;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.model.impl.ParameterImpl;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.commons.core.Optional;

import static de.benshu.commons.core.Optional.none;
import static de.benshu.commons.core.Optional.some;

// TODO merge with LocalVariable
public class ParametersNs extends AbstractNamespace {
    public static AbstractNamespace wrap(AbstractNamespace parent, ImmutableList<ParameterImpl<Pass>> parameters) {
        return new de.benshu.cofi.cofic.frontend.namespace.ParametersNs(parent, parameters);
    }

    private final ImmutableList<ParameterImpl<Pass>> parameters;

    private ParametersNs(AbstractNamespace parent, ImmutableList<ParameterImpl<Pass>> parameters) {
        super(parent);

        this.parameters = parameters;
    }

    @Override
    protected Optional<AbstractResolution> tryResolveLocally(AbstractNamespace fromNamespace, String name) {
        for (ParameterImpl<Pass> param : parameters)
            if (param.name.getLexeme().equals(name)) {
                final ProperTypeMixin<Pass, ?> valueType = aggregate.lookUpProperTypeOf(param.type);
                final ProperTypeMixin<Pass, ?> variableType = pass.getTypeSystem().lookUp("Field").apply(AbstractTypeList.of(valueType));
                return some(new DefaultResolution(variableType));
            }

        return none();
    }
}
