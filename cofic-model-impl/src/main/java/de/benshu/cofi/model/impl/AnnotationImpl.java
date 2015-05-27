package de.benshu.cofi.model.impl;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.model.Annotation;
import de.benshu.cofi.parser.AstNodeConstructorMethod;
import de.benshu.cofi.parser.lexer.Token;

public abstract class AnnotationImpl<X extends ModelContext<X>> extends AbstractModelNode<X> implements Annotation<X> {
    @AstNodeConstructorMethod
    public static <X extends ModelContext<X>> AnnotationImpl<X> of(NamedTypeExpression<X> type, ExpressionNode<X> value, ImmutableList<PropertyAssignment<X>> propertyAssignments) {
        return value == null && propertyAssignments == null
                ? new Default<>(type, null, ImmutableList.<PropertyAssignment<X>>of())
                : new Default<>(type, value, propertyAssignments);
    }

    public final ExpressionNode<X> value;
    public final ImmutableList<PropertyAssignment<X>> propertyAssignments;

    AnnotationImpl(ExpressionNode<X> value, ImmutableList<PropertyAssignment<X>> propertyAssignments) {
        this.value = value;
        this.propertyAssignments = propertyAssignments;
    }

    public abstract NamedTypeExpression<X> getTypeExpression();

    static final class Default<X extends ModelContext<X>> extends AnnotationImpl<X> {
        private final NamedTypeExpression<X> type;

        Default(NamedTypeExpression<X> type, ExpressionNode<X> value, ImmutableList<PropertyAssignment<X>> propertyAssignments) {
            super(value, propertyAssignments);

            this.type = type;
        }

        @Override
        public NamedTypeExpression<X> getTypeExpression() {
            return type;
        }

        @Override
        public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
            return visitor.visitAnnotation(this, aggregate);
        }

        @Override
        public <N, L extends N, D extends L, S extends N, E extends N, T extends E> N accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
            return transformer.transformAnnotation(this);
        }
    }

    public static class PropertyAssignment<X extends ModelContext<X>> extends AbstractModelNode<X> {
        public static <X extends ModelContext<X>> PropertyAssignment<X> of(Token propertyName, ExpressionNode<X> value) {
            return new PropertyAssignment<>(propertyName, value);
        }

        public final Token propertyName;
        public final ExpressionNode<X> value;

        public PropertyAssignment(Token propertyName, ExpressionNode<X> value) {
            this.propertyName = propertyName;
            this.value = value;
        }

        @Override
        public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
            return visitor.visitAnnotationPropertyAssignment(this, aggregate);
        }

        @Override
        public <N, L extends N, D extends L, S extends N, E extends N, T extends E> N accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
            return transformer.transformAnnotationPropertyAssignment(this);
        }
    }
}
