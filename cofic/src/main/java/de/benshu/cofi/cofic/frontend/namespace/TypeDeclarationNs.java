package de.benshu.cofi.cofic.frontend.namespace;

import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.cofic.notes.Source;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.model.impl.AbstractTypeDeclaration;
import de.benshu.cofi.model.impl.ExpressionNode;
import de.benshu.cofi.model.impl.ObjectDeclaration;
import de.benshu.cofi.model.impl.ThisExpression;
import de.benshu.cofi.parser.lexer.ArtificialToken;
import de.benshu.cofi.parser.lexer.Token;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;

import java.util.Objects;
import java.util.Optional;

import static de.benshu.commons.core.Optional.from;

class TypeDeclarationNs extends AbstractNamespace {
    public static AbstractNamespace within(AbstractNamespace parent,
                                           AbstractTypeDeclaration<Pass> typeDeclaration) {
        return new TypeDeclarationNs(parent, typeDeclaration);
    }

    public static TypeDeclarationNs outOfScope(AbstractNamespace parent,
                                               AbstractTypeDeclaration<Pass> typeDeclaration) {
        return new TypeDeclarationNs(parent, typeDeclaration);
    }

    private final AbstractTypeDeclaration<Pass> typeDeclaration;

    private TypeDeclarationNs(AbstractNamespace parent, AbstractTypeDeclaration<Pass> typeDeclaration) {
        super(parent);

        this.typeDeclaration = typeDeclaration;
    }

    @Override
    protected TypeMixin<Pass, ?> asType(LookUp lookUp) {
        return lookUp.lookUpTypeOf(typeDeclaration);
    }

    @Override
    ExpressionNode<Pass> getAccessor(LookUp lookUp) {
        return getAccessor(lookUp.lookUpFqnOf(typeDeclaration));
    }

    @Override
    public AbstractTypeDeclaration<Pass> getContainingTypeDeclaration() {
        return typeDeclaration;
    }

    @Override
    public AbstractConstraints<Pass> getContextualConstraints(LookUp lookUp) {
        return lookUp.lookUpTypeParametersOf(typeDeclaration).getConstraints();
    }

    @Override
    public Fqn getContainingEntityFqn() {
        return getParent().getContainingEntityFqn().getChild(typeDeclaration.getName());
    }

    @Override
    protected de.benshu.commons.core.Optional<AbstractNamespace> tryResolveNamespaceLocally(LookUp lookUp, String name, Source.Snippet src) {
        final Optional<ObjectDeclaration<Pass>> memberTypeDeclaration = typeDeclaration.body.elements.stream()
                .filter(e -> e instanceof ObjectDeclaration<?>)
                .map(e -> (ObjectDeclaration<Pass>) e)
                .filter(d -> d.getName().equals(name))
                .findFirst();

        return from(memberTypeDeclaration.map(d -> TypeDeclarationNs.outOfScope(this, d)));
    }

    @Override
    protected de.benshu.commons.core.Optional<AbstractResolution> tryResolveLocally(LookUp lookUp, AbstractNamespace fromNamespace, String name) {
        return lookUp.lookUpTypeOf(typeDeclaration).applyTrivially().lookupMember(name)
                .map(m -> {
                    boolean thisAccessible = Objects.equals(fromNamespace.getContainingTypeDeclaration(), typeDeclaration);

                    final ExpressionNode<Pass> implicitPrimary = thisAccessible
                            ? ThisExpression.of(ArtificialToken.create(Token.Kind.THIS, "this"))
                            : getAccessor(lookUp);

                    return new DefaultResolution(m.getType(), implicitPrimary, m);
                });
    }
}
