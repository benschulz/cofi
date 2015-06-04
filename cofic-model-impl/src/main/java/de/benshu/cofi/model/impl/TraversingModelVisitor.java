package de.benshu.cofi.model.impl;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.parser.lexer.Token;

public class TraversingModelVisitor<X extends ModelContext<X>, T> implements ModelVisitor<X, T> {

    @Override
    public T visitAnnotation(AnnotationImpl<X> annotation, T aggregate) {
        aggregate = visit(annotation.getTypeExpression(), aggregate);
        aggregate = visitAllNonNull(annotation.propertyAssignments, aggregate);
        aggregate = visitNonNull(annotation.value, aggregate);
        return aggregate;
    }

    @Override
    public T visitAnnotationPropertyAssignment(AnnotationImpl.PropertyAssignment<X> propertyAssignment, T aggregate) {
        aggregate = visit(propertyAssignment.value, aggregate);
        return aggregate;
    }

    @Override
    public T visitAssignment(Assignment<X> assignment, T aggregate) {
        aggregate = visit(assignment.lhs, aggregate);
        aggregate = visit(assignment.rhs, aggregate);
        return aggregate;
    }

    @Override
    public T visitClassDeclaration(ClassDeclaration<X> classDeclaration, T aggregate) {
        aggregate = visitAll(classDeclaration.annotations, aggregate);
        aggregate = visitAll(classDeclaration.modifiers, aggregate);
        aggregate = visitToken(classDeclaration.id, aggregate);
        aggregate = visit(classDeclaration.typeParameters, aggregate);
        aggregate = visitAll(classDeclaration.parameters, aggregate);
        aggregate = visitAll(classDeclaration.extending, aggregate);
        aggregate = visit(classDeclaration.body, aggregate);
        return aggregate;
    }

    @Override
    public T visitClosure(Closure<X> closure, T aggregate) {
        aggregate = visitAll(closure.cases, aggregate);
        return aggregate;
    }

    @Override
    public T visitClosureCase(Closure.Case<X> closureCase, T aggregate) {
        aggregate = visitAll(closureCase.params, aggregate);
        aggregate = visitAll(closureCase.body, aggregate);
        return aggregate;
    }

    @Override
    public T visitCompilationUnit(CompilationUnit<X> compilationUnit, T aggregate) {
        aggregate = visit(compilationUnit.moduleDeclaration, aggregate);
        aggregate = visit(compilationUnit.packageDeclaration, aggregate);
        aggregate = visitAll(compilationUnit.imports, aggregate);
        aggregate = visitAll(compilationUnit.declarations, aggregate);
        return aggregate;
    }

    @Override
    public T visitCompilationUnitModuleDeclaration(CompilationUnit.ModuleDeclaration<X> moduleDeclaration, T aggregate) {
        aggregate = visit(moduleDeclaration.name, aggregate);
        return aggregate;
    }

    @Override
    public T visitCompilationUnitPackageDeclaration(CompilationUnit.PackageDeclaration<X> packageDeclaration, T aggregate) {
        aggregate = visit(packageDeclaration.name, aggregate);
        return aggregate;
    }

    @Override
    public T visitExpressionStatement(ExpressionStatement<X> expressionStatement, T aggregate) {
        aggregate = visitAll(expressionStatement.annotations, aggregate);
        aggregate = visit(expressionStatement.expression, aggregate);
        return aggregate;
    }

    @Override
    public T visitFullyQualifiedName(FullyQualifiedName<X> fullyQualifiedName, T aggregate) {
        for (Token id : fullyQualifiedName.ids)
            aggregate = visitToken(id, aggregate);
        aggregate = visitAllNonNull(fullyQualifiedName.typeArgs, aggregate);
        return aggregate;
    }

    @Override
    public T visitFunctionInvocationExpression(FunctionInvocationExpression<X> functionInvocationExpression, T aggregate) {
        aggregate = visit(functionInvocationExpression.primary, aggregate);
        aggregate = visitAll(functionInvocationExpression.args, aggregate);
        return aggregate;
    }

    @Override
    public T visitFunctionType(FunctionTypeExpression<X> functionType, T aggregate) {
        aggregate = functionType.in.accept(this, aggregate);
        aggregate = functionType.out.accept(this, aggregate);
        return aggregate;
    }

    @Override
    public T visitImportStatement(ImportStatement<X> importStatement, T aggregate) {
        aggregate = visit(importStatement.name, aggregate);
        return aggregate;
    }

    @Override
    public T visitInitializerStatement(InitializerStatement<X> initializer, T aggregate) {
        aggregate = visit(initializer.statement, aggregate);
        return aggregate;
    }

    @Override
    public T visitLiteralExpression(LiteralExpression<X> literalExpr, T aggregate) {
        aggregate = visitToken(literalExpr.literal, aggregate);
        return aggregate;
    }

    @Override
    public T visitLiteralType(LiteralTypeExpression<X> literalTypeExpression, T aggregate) {
        aggregate = visitToken(literalTypeExpression.literal, aggregate);
        return aggregate;
    }

    @Override
    public T visitLocalVariableDeclaration(LocalVariableDeclaration<X> localVariableDeclaration, T aggregate) {
        aggregate = visitAll(localVariableDeclaration.annotations, aggregate);
        aggregate = visitAll(localVariableDeclaration.modifiers, aggregate);
        aggregate = visitToken(localVariableDeclaration.name, aggregate);
        aggregate = visit(localVariableDeclaration.type, aggregate);
        aggregate = visit(localVariableDeclaration.value, aggregate);
        return aggregate;
    }

    @Override
    public T visitMemberAccessExpression(MemberAccessExpression<X> memberAccessExpression, T aggregate) {
        aggregate = visit(memberAccessExpression.primary, aggregate);
        aggregate = visit(memberAccessExpression.name, aggregate);
        return aggregate;
    }

    @Override
    public T visitMethodDeclaration(MethodDeclarationImpl<X> methodDeclaration, T aggregate) {
        aggregate = visitAll(methodDeclaration.annotations, aggregate);
        aggregate = visitAll(methodDeclaration.modifiers, aggregate);
        aggregate = visitAll(methodDeclaration.pieces, aggregate);
        aggregate = visit(methodDeclaration.returnType, aggregate);
        aggregate = visitAllNonNull(methodDeclaration.body, aggregate);
        return aggregate;
    }

    @Override
    public T visitMethodDeclarationPiece(MethodDeclarationImpl.Piece<X> piece, T aggregate) {
        aggregate = visit(piece.typeParameters, aggregate);
        aggregate = visitAll(piece.params, aggregate);
        return aggregate;
    }

    @Override
    public T visitModifier(ModifierImpl<X> modifier, T aggregate) {
        aggregate = visitToken(modifier.token, aggregate);
        return aggregate;
    }

    @Override
    public T visitNamedType(NamedTypeExpression<X> namedType, T aggregate) {
        aggregate = visit(namedType.name, aggregate);
        return aggregate;
    }

    @Override
    public T visitNameExpression(NameExpression<X> nameExpression, T aggregate) {
        aggregate = visit(nameExpression.name, aggregate);
        return aggregate;
    }

    @Override
    public T visitPackageObjectDeclaration(PackageObjectDeclaration<X> packageObjectDeclaration, T aggregate) {
        aggregate = visitAll(packageObjectDeclaration.annotations, aggregate);
        aggregate = visitAll(packageObjectDeclaration.modifiers, aggregate);
        aggregate = visitAll(packageObjectDeclaration.extending, aggregate);
        aggregate = visit(packageObjectDeclaration.body, aggregate);
        return aggregate;
    }

    @Override
    public T visitParameter(ParameterImpl<X> parameter, T aggregate) {
        aggregate = visitAll(parameter.annotations, aggregate);
        aggregate = visitAll(parameter.modifiers, aggregate);
        aggregate = visit(parameter.type, aggregate);
        aggregate = visitNonNull(parameter.defaultValue, aggregate);
        return aggregate;
    }

    @Override
    public T visitPropertyDeclaration(PropertyDeclaration<X> propertyDeclaration, T aggregate) {
        aggregate = visitAll(propertyDeclaration.annotations, aggregate);
        aggregate = visitAll(propertyDeclaration.modifiers, aggregate);
        aggregate = visitToken(propertyDeclaration.id, aggregate);
        aggregate = visitAll(propertyDeclaration.traits, aggregate);
        aggregate = visit(propertyDeclaration.type, aggregate);
        aggregate = visit(propertyDeclaration.initialValue, aggregate);
        return aggregate;
    }

    @Override
    public T visitRelativeName(RelativeNameImpl<X> relativeName, T aggregate) {
        for (Token id : relativeName.ids)
            aggregate = visitToken(id, aggregate);
        aggregate = visitAllNonNull(relativeName.typeArgs, aggregate);
        return aggregate;
    }

    @Override
    public T visitThisExpr(ThisExpression<X> thisExpression, T aggregate) {
        aggregate = visitToken(thisExpression.token, aggregate);
        return aggregate;
    }

    @Override
    public T visitTraitDeclaration(TraitDeclaration<X> traitDeclaration, T aggregate) {
        aggregate = visitAll(traitDeclaration.annotations, aggregate);
        aggregate = visitAll(traitDeclaration.modifiers, aggregate);
        aggregate = visitToken(traitDeclaration.id, aggregate);
        aggregate = visit(traitDeclaration.typeParameters, aggregate);
        aggregate = visitAll(traitDeclaration.extending, aggregate);
        aggregate = visit(traitDeclaration.body, aggregate);
        return aggregate;
    }

    @Override
    public T visitTupleType(TupleTypeExpression<X> tupleType, T aggregate) {
        return visitAll(tupleType.types, aggregate);
    }

    @Override
    public T visitTypeBody(TypeBody<X> typeBody, T aggregate) {
        return visitAll(typeBody.elements, aggregate);
    }

    @Override
    public T visitTypeParameterDeclaration(TypeParamDecl<X> typeParameterDeclaration, T aggregate) {
        return visitToken(typeParameterDeclaration.name, aggregate);
    }

    @Override
    public T visitTypeParameters(TypeParameters<X> typeParameters, T aggregate) {
        aggregate = visitAll(typeParameters.declarations, aggregate);
        for (ImmutableList<TypeExpression<X>> cs : typeParameters.constraints)
            aggregate = visitAll(cs, aggregate);
        return aggregate;
    }

    @Override
    public T visitUnionDeclaration(UnionDeclaration<X> unionDeclaration, T aggregate) {
        aggregate = visitAll(unionDeclaration.annotations, aggregate);
        aggregate = visitAll(unionDeclaration.modifiers, aggregate);
        aggregate = visitToken(unionDeclaration.id, aggregate);
        aggregate = visit(unionDeclaration.typeParameters, aggregate);
        aggregate = visitAll(unionDeclaration.extending, aggregate);
        aggregate = visit(unionDeclaration.body, aggregate);
        return aggregate;
    }

    @Override
    public T visitObjectDeclaration(ObjectDeclaration<X> objectDecl, T aggregate) {
        aggregate = visitAll(objectDecl.annotations, aggregate);
        aggregate = visitAll(objectDecl.modifiers, aggregate);
        aggregate = visitToken(objectDecl.id, aggregate);
        aggregate = visitAll(objectDecl.extending, aggregate);
        aggregate = visit(objectDecl.body, aggregate);
        return aggregate;
    }

    public T visitToken(Token token, T aggregate) {
        return aggregate;
    }
}
