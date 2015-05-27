package de.benshu.cofi.cofic.frontend.constraints;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.cofic.frontend.namespace.NamespaceTrackingVisitor;
import de.benshu.cofi.model.impl.AbstractTypeDeclaration;
import de.benshu.cofi.model.impl.AnnotationImpl;
import de.benshu.cofi.model.impl.CompilationUnit;
import de.benshu.cofi.model.impl.MethodDeclarationImpl;
import de.benshu.cofi.model.impl.PropertyDeclaration;
import de.benshu.cofi.model.impl.Statement;
import de.benshu.cofi.model.impl.TypeExpression;
import de.benshu.cofi.model.impl.TypeParameterized;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;

import java.util.ArrayDeque;
import java.util.Deque;

public class HierarchyAndConstraintEstablisher {
    public static ConstraintsData establish(Pass pass, ImmutableSet<CompilationUnit<Pass>> compilationUnits) {
        return compilationUnits
                .parallelStream()
                .map(u -> new Visitor(pass).visit(u, ConstraintsData.builder()))
                .collect(ConstraintsData::builder, ConstraintsDataBuilder::addAll, ConstraintsDataBuilder::addAll)
                .build();
    }

    private static final class Visitor extends NamespaceTrackingVisitor<ConstraintsDataBuilder> {
        private final Pass pass;

        private /*Stack*/ Deque<AbstractConstraints<Pass>> contextualConstraints = new ArrayDeque<>();

        public Visitor(Pass pass) {
            super(pass);

            this.pass = pass;

            contextualConstraints.push(AbstractConstraints.none());
        }

        @Override
        public ConstraintsDataBuilder visitAnnotation(AnnotationImpl<Pass> annotation, ConstraintsDataBuilder aggregate) {
            return aggregate;
        }

        @Override
        protected ConstraintsDataBuilder visitMethodBody(MethodDeclarationImpl<Pass> methodDeclaration, ConstraintsDataBuilder aggregate) {
            for (MethodDeclarationImpl.Piece<Pass> piece : methodDeclaration.pieces)
                push(piece, aggregate);
            for (MethodDeclarationImpl.Piece<Pass> piece : methodDeclaration.pieces)
                pop();
            return aggregate;
        }

        @Override
        public ConstraintsDataBuilder visitPropertyDeclaration(PropertyDeclaration<Pass> propertyDeclaration, ConstraintsDataBuilder aggregate) {
            return aggregate;
        }

        @Override
        protected ConstraintsDataBuilder visitStatements(ImmutableList<Statement<Pass>> statements, ConstraintsDataBuilder aggregate) {
            return aggregate;
        }

        @Override
        protected ConstraintsDataBuilder visitTypeBody(AbstractTypeDeclaration<Pass> typeDeclaration, ConstraintsDataBuilder aggregate) {
            push(typeDeclaration, aggregate);
            super.visitTypeBody(typeDeclaration, aggregate);
            pop();
            return aggregate;
        }

        private void push(TypeParameterized<Pass> parameterized, ConstraintsDataBuilder aggregate) {
            final TypeParameterListImpl<Pass> typeParameters = pass.lookUpTypeParametersOf(parameterized);

            AbstractConstraints<Pass> cs = AbstractConstraints.trivial(pass, contextualConstraints.peek(), typeParameters);
            for (ImmutableList<TypeExpression<Pass>> chain : parameterized.getTypeParameters().constraints) {
                ProperTypeMixin<Pass, ?> last = aggregate.lookUpProperTypeOf(chain.get(0));

                for (int i = 1; i < chain.size(); ++i) {
                    ProperTypeMixin<Pass, ?> current = aggregate.lookUpProperTypeOf(chain.get(i));
                    cs = cs.establishSubtype(last, current);
                    last = current;
                }
            }

            contextualConstraints.push(cs);
            aggregate.defineConstraintsOf(parameterized.getTypeParameters(), cs);
        }

        private void pop() {
            contextualConstraints.pop();
        }
    }
}
