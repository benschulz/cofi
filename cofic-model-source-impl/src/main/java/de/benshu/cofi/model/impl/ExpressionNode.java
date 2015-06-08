package de.benshu.cofi.model.impl;

import de.benshu.cofi.model.ModelNode;

public abstract class ExpressionNode<X extends ModelContext<X>>
        extends AbstractModelNode<X>
        implements ExpressionMixin<X>, ModelNodeMixin<X>, ModelNode<X> {
}
