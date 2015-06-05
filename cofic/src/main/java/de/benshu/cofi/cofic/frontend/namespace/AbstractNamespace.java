package de.benshu.cofi.cofic.frontend.namespace;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.cofic.notes.CofiNote;
import de.benshu.cofi.cofic.notes.ImmutableNote;
import de.benshu.cofi.cofic.notes.PrintStreamNotes;
import de.benshu.cofi.cofic.notes.Source.Snippet;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.model.Namespace;
import de.benshu.cofi.model.impl.AbstractTypeDeclaration;
import de.benshu.cofi.model.impl.ExpressionNode;
import de.benshu.cofi.model.impl.MemberAccessExpression;
import de.benshu.cofi.model.impl.RelativeNameImpl;
import de.benshu.cofi.model.impl.RootExpression;
import de.benshu.cofi.parser.lexer.ArtificialToken;
import de.benshu.cofi.parser.lexer.Token;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.commons.core.Optional;

import static de.benshu.commons.core.Optional.none;
import static de.benshu.commons.core.Optional.some;

public abstract class AbstractNamespace implements Namespace<Pass> {
    final AbstractNamespace parent;

    AbstractNamespace() {
        this.parent = null;
    }

    AbstractNamespace(AbstractNamespace parent) {
        this.parent = parent;
    }

    public AbstractTypeDeclaration<Pass> getContainingTypeDeclaration() {
        return getParent().getContainingTypeDeclaration();
    }

    public AbstractConstraints<Pass> getContextualConstraints(LookUp lookUp) {
        return getParent().getContextualConstraints(lookUp);
    }

    public Fqn getPackageFqn() {
        return getParent().getPackageFqn();
    }

    public Fqn getContainingEntityFqn() {
        return getParent().getContainingEntityFqn();
    }

    public AbstractNamespace getParent() {
        if (parent == null)
            throw new AssertionError();
        return parent;
    }

    public AbstractNamespace getRoot() {
        return parent == null ? this : parent.getRoot();
    }

    ExpressionNode<Pass> getAccessor(LookUp lookUp) {
        throw new UnsupportedOperationException();
    }

    ExpressionNode<Pass> getAccessor(Fqn fqn) {
        return getAccessor(fqn, fqn.length() - 1);
    }

    private ExpressionNode<Pass> getAccessor(Fqn fqn, int index) {
        final ArtificialToken id = ArtificialToken.create(Token.Kind.IDENTIFIER, fqn.get(index));

        return MemberAccessExpression.of(index > 0 ? getAccessor(fqn, index - 1) : RootExpression.of(), RelativeNameImpl.of(id));
    }

    public TypeMixin<Pass, ?> resolveType(LookUp lookUp, ImmutableList<String> ids, Snippet src) {
        return resolveNamespace(lookUp, ids, src);
    }

    public TypeMixin<Pass, ?> resolveFullyQualifiedType(LookUp lookUp, Fqn ids, Snippet src) {
        return getRoot().resolveNamespace(lookUp, ImmutableList.copyOf(ids), src);
    }

    private TypeMixin<Pass, ?> resolveNamespace(LookUp lookUp, ImmutableList<String> ids, Snippet src) {
        for (AbstractNamespace resolvedInThisScope : tryResolveNamespace(lookUp, ids, src))
            return resolvedInThisScope.asType(lookUp);
        return fail(ids, src).getType();
    }

    final Optional<AbstractNamespace> tryResolveNamespace(LookUp lookUp, ImmutableList<String> ids, Snippet src) {
        Optional<AbstractNamespace> current = some(this);
        for (String id : ids)
            current = current.flatMap(c -> c.tryResolveNamespaceLocally(lookUp, id, src));
        return current.or(() -> parent == null ? none() : parent.tryResolveNamespace(lookUp, ids, src));
    }

    Optional<AbstractNamespace> tryResolveNamespaceLocally(LookUp lookUp, String name, Snippet src) {
        return none();
    }

    TypeMixin<Pass, ?> asType(LookUp lookUp) {
        throw new UnsupportedOperationException(this.getClass().toString());
    }


    public AbstractResolution resolve(LookUp lookUp, String name) {
        return resolve(lookUp, this, name);
    }

    public AbstractResolution resolve(LookUp lookUp, AbstractNamespace fromNamespace, String name) {
        for (AbstractResolution resolvedInThisScope : tryResolveLocally(lookUp, fromNamespace, name))
            return resolvedInThisScope;

        return parent == null
                ? new ErrorResolution()
                : parent.resolve(lookUp, fromNamespace, name);
    }

    Optional<AbstractResolution> tryResolveLocally(LookUp lookUp, AbstractNamespace fromNamespace, String name) {
        return tryResolveNamespaceLocally(lookUp, name, ArtificialToken.create(Token.Kind.IDENTIFIER, name))
                .map(ns -> ns.asType(lookUp))
                .map(DefaultResolution::new);
    }

    AbstractResolution fail(ImmutableList<String> relativeName, Snippet src) {
        String msg = "Can't resolve " + Joiner.on(".").join(relativeName) + " in scope " + this + ".";
        PrintStreamNotes.err().attach(src, ImmutableNote.create(CofiNote.UNDEFINED_NAME, msg));
        return new ErrorResolution();
    }

    @Override
    public String toString() {
        return "Namespace: " + getClass().toString();
    }
}
