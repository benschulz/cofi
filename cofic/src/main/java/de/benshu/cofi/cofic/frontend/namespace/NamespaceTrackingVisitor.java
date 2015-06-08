package de.benshu.cofi.cofic.frontend.namespace;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.cofic.frontend.GenericModelDataBuilder;
import de.benshu.cofi.cofic.notes.Source;
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
import de.benshu.cofi.parser.lexer.Token;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.TypeConstructorMixin;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.impl.templates.AbstractTemplateTypeConstructor;

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
        namespaces.push(RootNs.create());

        aggregate = visit(compilationUnit.moduleDeclaration, aggregate);
        namespaces.push(ModuleNs.wrap(getNs(), compilationUnit.moduleDeclaration));

        aggregate = visit(compilationUnit.packageDeclaration, aggregate);
        final Fqn packageFqn = compilationUnit.packageDeclaration.name.fqn;
        namespaces.push(PackageNs.wrap(getNs(), packageFqn, pass.lookUpPackageObjectDeclarationOf(packageFqn)));

        aggregate = visitAll(compilationUnit.imports, aggregate);

        aggregate = visitAll(compilationUnit.declarations, aggregate);

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
        aggregate = visit(importStatement.name, aggregate);
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

        return aggregate.defineTypeOf(literalTypeExpression, type);
    }

    protected T visitMethodBody(MethodDeclarationImpl<Pass> methodDeclaration, T aggregate) {
        return methodDeclaration.body == null ? aggregate
                : visitStatements(methodDeclaration.body, aggregate);
    }

    @Override
    public T visitMethodDeclaration(MethodDeclarationImpl<Pass> methodDeclaration, T aggregate) {
        namespaces.push(MethodDeclarationNs.wrap(getNs(), methodDeclaration));

        aggregate = visitAll(methodDeclaration.annotations, aggregate);
        aggregate = visitAll(methodDeclaration.modifiers, aggregate);

        for (MethodDeclarationImpl.Piece<Pass> piece : methodDeclaration.pieces) {
            namespaces.push(TypeParametersNs.wrap(getNs(), piece));
            visit(piece, aggregate);
        }

        aggregate = visit(methodDeclaration.returnType, aggregate);

        aggregate = visitMethodBody(methodDeclaration, aggregate);

        for (int i = 0; i < methodDeclaration.pieces.size(); ++i) {
            namespaces.pop();
        }
        namespaces.pop();
        return aggregate;
    }

    @Override
    public T visitMethodDeclarationPiece(MethodDeclarationImpl.Piece<Pass> piece, T aggregate) {
        aggregate = visit(piece.typeParameters, aggregate);
        aggregate = visitAll(piece.params, aggregate);
        return aggregate;
    }

    @Override
    public T visitModifier(ModifierImpl<Pass> modifier, T aggregate) {
        return visitAnnotation(modifier, aggregate);
    }

    @Override
    public T visitNamedType(NamedTypeExpression<Pass> namedType, T aggregate) {
        visit(namedType.name, aggregate);

        return aggregate.defineTypeOf(namedType, resolveAndConstructTypeName(namedType.name, aggregate));
    }

    protected final TypeMixin<Pass, ?> resolveAndConstructTypeName(NameImpl<Pass> name, T aggregate) {
        final TypeMixin<Pass, ?> resolvedType = resolveTypeName(name, aggregate);

        if (resolvedType instanceof TypeConstructorMixin<?, ?, ?>) {
            final TypeConstructorMixin<Pass, ?, ?> typeConstructor = (TypeConstructorMixin<Pass, ?, ?>) resolvedType;

            if (typeConstructor.getParameters().isEmpty())
                return typeConstructor.applyTrivially();
            else if (name.typeArgs != null)
                return typeConstructor.apply(name.typeArgs.stream().map(aggregate::lookUpTypeOf).collect(typeList()));
        }

        return resolvedType;
    }

    @Override
    public T visitNameExpression(NameExpression<Pass> nameExpression, T aggregate) {
        Preconditions.checkState(nameExpression.name.ids.size() == 1);

        return visit(nameExpression.name, aggregate);
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
            aggregate = visitStatement(statement, aggregate);

            // ugly
            if (statement instanceof LocalVariableDeclaration) {
                final LocalVariableDeclaration<Pass> lvd = (LocalVariableDeclaration<Pass>) statement;
                namespaces.push(LocalVariableDeclarationNs.wrap(getNs(), lvd));
                aggregate = visitStatements(statements.subList(i + 1, statements.size()), aggregate);
                namespaces.pop();
                return aggregate;
            }
        }

        return aggregate;
    }

    protected T visitStatement(Statement<Pass> statement, T aggregate) {
        return visit(statement, aggregate);
    }

    @Override
    public T visitTraitDeclaration(TraitDeclaration<Pass> traitDeclaration, T aggregate) {
        return visitTypeDeclaration(traitDeclaration, aggregate);
    }

    @Override
    public T visitTupleType(TupleTypeExpression<Pass> tupleType, T aggregate) {
        visitAll(tupleType.types, aggregate);

        return aggregate.defineTypeOf(tupleType, pass.getTypeSystem().getTuple(tupleType.types.size())
                .apply(tupleType.types.stream().map(aggregate::lookUpTypeOf).collect(typeList())));
    }

    protected T visitTypeBody(AbstractTypeDeclaration<Pass> typeDeclaration, T aggregate) {
        return visit(typeDeclaration.body, aggregate);
    }

    protected T visitTypeDeclaration(AbstractTypeDeclaration<Pass> typeDeclaration, T aggregate) {
        aggregate = visitAll(typeDeclaration.annotations, aggregate);
        aggregate = visitAll(typeDeclaration.modifiers, aggregate);

        namespaces.push(typeDeclaration instanceof PackageObjectDeclaration<?>
                ? PackageObjectNs.wrap(getNs(), getNs().getPackageFqn(), (PackageObjectDeclaration<Pass>) typeDeclaration)
                : TypeDeclarationNs.within(getNs(), typeDeclaration));

        if (typeDeclaration instanceof ObjectDeclaration<?>)
            for (AbstractTypeDeclaration<Pass> accompanied : pass.tryLookUpAccompaniedBy(typeDeclaration))
                namespaces.push(TypeParametersNs.wrap(getNs(), accompanied));
        else
            namespaces.push(TypeParametersNs.wrap(getNs(), typeDeclaration));

        aggregate = visit(typeDeclaration.getTypeParameters(), aggregate);

        aggregate = visitAll(typeDeclaration.getParameters(), aggregate);
        aggregate = visitAll(typeDeclaration.extending, aggregate);

        namespaces.push(ParametersNs.wrap(getNs(), typeDeclaration.getParameters()));

        aggregate = visitTypeBody(typeDeclaration, aggregate);

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

    protected final TypeMixin<Pass, ?> resolveTypeName(NameImpl<Pass> name, T aggregate) {
        final LookUp lookUp = lookUp(aggregate);

        final Source.Snippet src = name.ids.get(0).getTokenString(name.ids.get(name.ids.size() - 1));

        return name instanceof FullyQualifiedName
                ? getNs().resolveQualifiedTypeName(lookUp, ((FullyQualifiedName) name).fqn, src)
                : getNs().resolveTypeName(lookUp, ImmutableList.copyOf(name.ids.stream().map(Token::getLexeme).iterator()), src);
    }

    protected final TypeMixin<Pass, ?> resolveFullyQualifiedType(Fqn name, Source.Snippet src, T aggregate) {
        return getNs().resolveQualifiedTypeName(lookUp(aggregate), name, src);
    }

    protected final AbstractResolution resolve(NameImpl<Pass> name, T aggregate) {
        return resolve(Iterables.getOnlyElement(name.ids).getLexeme(), aggregate);
    }

    protected final AbstractResolution resolve(String name, T aggregate) {
        return getNs().resolve(lookUp(aggregate), name);
    }

    protected AbstractConstraints<Pass> getContextualConstraints(T aggregate) {
        return getNs().getContextualConstraints(lookUp(aggregate));
    }

    private LookUp lookUp(T aggregate) {
        return new LookUp(pass, aggregate);
    }
}
