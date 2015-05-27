package de.benshu.cofi.cofic.frontend.namespace;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.cofic.frontend.GenericModelDataBuilder;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.model.impl.AbstractTypeDeclaration;
import de.benshu.cofi.model.impl.ClassDeclaration;
import de.benshu.cofi.model.impl.CompilationUnit;
import de.benshu.cofi.model.impl.FullyQualifiedName;
import de.benshu.cofi.model.impl.ImportStatement;
import de.benshu.cofi.model.impl.LiteralTypeExpression;
import de.benshu.cofi.model.impl.LocalVariableDeclaration;
import de.benshu.cofi.model.impl.MethodDeclarationImpl;
import de.benshu.cofi.model.impl.ModifierImpl;
import de.benshu.cofi.model.impl.NameExpression;
import de.benshu.cofi.model.impl.NameImpl;
import de.benshu.cofi.model.impl.NamedTypeExpression;
import de.benshu.cofi.model.impl.ObjectDeclaration;
import de.benshu.cofi.model.impl.PackageObjectDeclaration;
import de.benshu.cofi.model.impl.RelativeNameImpl;
import de.benshu.cofi.model.impl.Statement;
import de.benshu.cofi.model.impl.TraitDeclaration;
import de.benshu.cofi.model.impl.TraversingModelVisitor;
import de.benshu.cofi.model.impl.TupleTypeExpression;
import de.benshu.cofi.model.impl.UnionDeclaration;
import de.benshu.cofi.types.impl.templates.AbstractTemplateTypeConstructor;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.TypeConstructorMixin;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;

import java.util.ArrayDeque;
import java.util.Deque;

import static de.benshu.cofi.types.impl.lists.AbstractTypeList.typeList;

public abstract class NamespaceTrackingVisitor<T extends GenericModelDataBuilder<T, ?>> extends TraversingModelVisitor<Pass, T> {
    private final Pass pass;
    private final /* Stack */ Deque<AbstractNamespace> namespaces = new ArrayDeque<>();

    public NamespaceTrackingVisitor(Pass pass) {
        this.pass = pass;
    }

    protected AbstractNamespace getNs() {
        return namespaces.peek();
    }

    protected AbstractNamespace popNs() {
        return namespaces.pop();
    }

    protected void pushNs(AbstractNamespace ns) {
        namespaces.push(ns);
    }

    protected AbstractTypeDeclaration<Pass> getContainingTypeDeclaration() {
        return getNs().getContainingTypeDeclaration();
    }

    @Override
    public T visitClassDeclaration(ClassDeclaration<Pass> classDeclaration, T aggregate) {
        return visitTypeDeclaration(classDeclaration, aggregate);
    }

    @Override
    public T visitCompilationUnit(CompilationUnit<Pass> compilationUnit, T aggregate) {
        namespaces.push(RootNs.create(pass, aggregate));

        visit(compilationUnit.moduleDeclaration, aggregate);
        namespaces.push(ModuleNs.wrap(getNs(), compilationUnit.moduleDeclaration));

        visit(compilationUnit.packageDeclaration, aggregate);
        final Fqn packageFqn = compilationUnit.packageDeclaration.name.fqn;
        namespaces.push(PackageNs.wrap(getNs(), packageFqn, pass.lookUpPackageObjectDeclarationOf(packageFqn)));

        visitAll(compilationUnit.imports, aggregate);

        visitAll(compilationUnit.declarations, aggregate);

        namespaces.pop();
        namespaces.pop();
        namespaces.pop();
        return aggregate;
    }

    @Override
    public T visitFullyQualifiedName(FullyQualifiedName<Pass> fullyQualifiedName, T aggregate) {
        return visitAllNonNull(fullyQualifiedName.typeArgs, aggregate);
    }

    @Override
    public T visitImportStatement(ImportStatement<Pass> importStatement, T aggregate) {
        visit(importStatement.name, aggregate);
        namespaces.push(ImportNs.wrap(namespaces.peek(), importStatement));
        return aggregate;
    }

    @Override
    public T visitLiteralType(LiteralTypeExpression<Pass> literalTypeExpression, T aggregate) {
        final ProperTypeMixin<Pass, ?> type;
        switch (literalTypeExpression.literal.getKind()) {
            case NIL:
                type = ((AbstractTemplateTypeConstructor<Pass>) pass.getTypeSystem().lookUp("Nil")).applyTrivially();
                break;
            default:
                throw new UnsupportedOperationException();
        }

        aggregate.defineTypeOf(literalTypeExpression, type);
        return aggregate;
    }

    protected T visitMethodBody(MethodDeclarationImpl<Pass> methodDeclaration, T aggregate) {
        return methodDeclaration.body == null ? aggregate
                : visitStatements(methodDeclaration.body, aggregate);
    }

    @Override
    public T visitMethodDeclaration(MethodDeclarationImpl<Pass> methodDeclaration, T aggregate) {
        namespaces.push(MethodDeclarationNs.wrap(getNs(), methodDeclaration));

        visitAll(methodDeclaration.annotations, aggregate);
        visitAll(methodDeclaration.modifiers, aggregate);

        for (MethodDeclarationImpl.Piece<Pass> piece : methodDeclaration.pieces) {
            namespaces.push(TypeParametersNs.wrap(getNs(), piece));
            visit(piece, aggregate);
        }

        methodDeclaration.returnType.accept(this, aggregate);

        visitMethodBody(methodDeclaration, aggregate);

        for (int i = 0; i < methodDeclaration.pieces.size(); ++i) {
            namespaces.pop();
        }
        namespaces.pop();
        return aggregate;
    }

    @Override
    public T visitMethodDeclarationPiece(MethodDeclarationImpl.Piece<Pass> piece, T aggregate) {
        visit(piece.typeParameters, aggregate);
        visitAll(piece.params, aggregate);
        return aggregate;
    }

    @Override
    public T visitModifier(ModifierImpl<Pass> modifier, T aggregate) {
        return visitAnnotation(modifier, aggregate);
    }

    @Override
    public T visitNamedType(NamedTypeExpression<Pass> namedType, T aggregate) {
        visit(namedType.name, aggregate);

        aggregate.defineTypeOf(namedType, resolveAndConstructTypeName(namedType.name, aggregate));
        return aggregate;
    }

    protected final TypeMixin<Pass, ?> resolveAndConstructTypeName(NameImpl<Pass> name, T aggregate) {
        final TypeMixin<Pass, ?> resolvedType = resolveTypeName(name);

        if (resolvedType instanceof TypeConstructorMixin<?, ?, ?>) {
            final TypeConstructorMixin<Pass, ?, ?> typeConstructor = (TypeConstructorMixin<Pass, ?, ?>) resolvedType;

            if (typeConstructor.getParameters().isEmpty())
                return typeConstructor.applyTrivially();
            else if (name.typeArgs != null)
                return typeConstructor.apply(name.typeArgs.stream().map(aggregate::lookUpTypeOf).collect(typeList()));
        }

        return resolvedType;
    }

    protected final TypeMixin<Pass, ?> resolveTypeName(NameImpl<Pass> name) {
        return getNs().resolveType(name);
    }

    @Override
    public T visitNameExpression(NameExpression<Pass> nameExpression, T aggregate) {
        Preconditions.checkState(nameExpression.name.ids.size() == 1);

        visit(nameExpression.name, aggregate);

        return aggregate;
    }

    @Override
    public T visitObjectDeclaration(ObjectDeclaration<Pass> objectDecl, T aggregate) {
        return visitTypeDeclaration(objectDecl, aggregate);
    }

    @Override
    public T visitPackageObjectDeclaration(PackageObjectDeclaration<Pass> packageObjectDeclaration, T aggregate) {
        return visitTypeDeclaration(packageObjectDeclaration, aggregate);
    }

    @Override
    public T visitRelativeName(RelativeNameImpl<Pass> relativeName, T aggregate) {
        return visitAllNonNull(relativeName.typeArgs, aggregate);
    }

    protected T visitStatements(ImmutableList<Statement<Pass>> statements, T aggregate) {
        for (int i = 0; i < statements.size(); ++i) {
            final Statement<Pass> statement = statements.get(i);
            statement.accept(this, aggregate);

            // ugly
            if (statement instanceof LocalVariableDeclaration) {
                final LocalVariableDeclaration<Pass> lvd = (LocalVariableDeclaration<Pass>) statement;
                namespaces.push(LocalVariableDeclarationNs.wrap(getNs(), lvd));
                visitStatements(statements.subList(i + 1, statements.size()), aggregate);
                namespaces.pop();
                return aggregate;
            }
        }

        return aggregate;
    }

    @Override
    public T visitTraitDeclaration(TraitDeclaration<Pass> traitDeclaration, T aggregate) {
        return visitTypeDeclaration(traitDeclaration, aggregate);
    }

    @Override
    public T visitTupleType(TupleTypeExpression<Pass> tupleType, T aggregate) {
        visitAll(tupleType.types, aggregate);

        aggregate.defineTypeOf(tupleType, pass.getTypeSystem().getTuple(tupleType.types.size())
                .apply(tupleType.types.stream().map(aggregate::lookUpTypeOf).collect(typeList())));
        return aggregate;
    }

    protected T visitTypeBody(AbstractTypeDeclaration<Pass> typeDeclaration, T aggregate) {
        return visit(typeDeclaration.body, aggregate);
    }

    protected T visitTypeDeclaration(AbstractTypeDeclaration<Pass> typeDeclaration, T aggregate) {
        visitAll(typeDeclaration.annotations, aggregate);
        visitAll(typeDeclaration.modifiers, aggregate);

        namespaces.push(typeDeclaration instanceof PackageObjectDeclaration<?>
                ? PackageObjectNs.wrap(getNs(), getNs().getPackageFqn(), (PackageObjectDeclaration<Pass>) typeDeclaration)
                : TypeDeclarationNs.within(getNs(), typeDeclaration));

        if (typeDeclaration instanceof ObjectDeclaration<?>)
            for (AbstractTypeDeclaration<Pass> accompanied : pass.tryLookUpAccompaniedBy(typeDeclaration))
                namespaces.push(TypeParametersNs.wrap(getNs(), accompanied));
        else
            namespaces.push(TypeParametersNs.wrap(getNs(), typeDeclaration));

        visit(typeDeclaration.getTypeParameters(), aggregate);

        visitAll(typeDeclaration.getParameters(), aggregate);
        visitAll(typeDeclaration.extending, aggregate);

        namespaces.push(ParametersNs.wrap(getNs(), typeDeclaration.getParameters()));

        visitTypeBody(typeDeclaration, aggregate);

        popNs();
        if (typeDeclaration instanceof ObjectDeclaration<?>)
            for (AbstractTypeDeclaration<Pass> accompanied : pass.tryLookUpAccompaniedBy(typeDeclaration))
                popNs();
        else
            popNs();
        popNs();

        return aggregate;
    }

    @Override
    public T visitUnionDeclaration(UnionDeclaration<Pass> unionDeclaration, T aggregate) {
        return visitTypeDeclaration(unionDeclaration, aggregate);
    }

    protected AbstractConstraints<Pass> getContextualConstraints() {
        return getNs().getContextualConstraints();
    }
}
