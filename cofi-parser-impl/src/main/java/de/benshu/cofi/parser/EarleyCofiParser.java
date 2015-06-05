package de.benshu.cofi.parser;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.model.impl.AnnotationImpl;
import de.benshu.cofi.model.impl.ClassDeclaration;
import de.benshu.cofi.model.impl.Closure;
import de.benshu.cofi.model.impl.CompilationUnit;
import de.benshu.cofi.model.impl.ExpressionNode;
import de.benshu.cofi.model.impl.ExpressionStatement;
import de.benshu.cofi.model.impl.FullyQualifiedName;
import de.benshu.cofi.model.impl.FunctionInvocationExpression;
import de.benshu.cofi.model.impl.ImportStatement;
import de.benshu.cofi.model.impl.InitializerStatement;
import de.benshu.cofi.model.impl.LiteralExpression;
import de.benshu.cofi.model.impl.LiteralTypeExpression;
import de.benshu.cofi.model.impl.LocalVariableDeclaration;
import de.benshu.cofi.model.impl.MemberAccessExpression;
import de.benshu.cofi.model.impl.MethodDeclarationImpl;
import de.benshu.cofi.model.impl.ModelContext;
import de.benshu.cofi.model.impl.ModifierImpl;
import de.benshu.cofi.model.impl.NameExpression;
import de.benshu.cofi.model.impl.NamedTypeExpression;
import de.benshu.cofi.model.impl.ObjectDeclaration;
import de.benshu.cofi.model.impl.PackageObjectDeclaration;
import de.benshu.cofi.model.impl.ParameterImpl;
import de.benshu.cofi.model.impl.PropertyDeclaration;
import de.benshu.cofi.model.impl.RelativeNameImpl;
import de.benshu.cofi.model.impl.RootExpression;
import de.benshu.cofi.model.impl.Statement;
import de.benshu.cofi.model.impl.ThisExpression;
import de.benshu.cofi.model.impl.TraitDeclaration;
import de.benshu.cofi.model.impl.TransformationContext;
import de.benshu.cofi.model.impl.TypeBody;
import de.benshu.cofi.model.impl.TypeExpression;
import de.benshu.cofi.model.impl.TypeParamDecl;
import de.benshu.cofi.model.impl.TypeParameters;
import de.benshu.cofi.model.impl.UnionDeclaration;
import de.benshu.cofi.model.impl.UserDefinedExpression;
import de.benshu.cofi.model.impl.UserDefinedNodeTransformation;
import de.benshu.cofi.model.impl.UserDefinedStatement;
import de.benshu.cofi.parser.lexer.ArtificialToken;
import de.benshu.cofi.parser.lexer.Token;
import de.benshu.cofi.parser.lexer.impl.TokenStreamImpl;
import de.benshu.cofi.types.impl.ProperTypeConstructorMixin;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.TypeConstructorInvocation;
import de.benshu.cofi.types.impl.TypeConstructorMixin;
import de.benshu.cofi.types.impl.TypeMixin;

import java.io.Reader;
import java.lang.reflect.Field;
import java.util.Optional;

import static de.benshu.cofi.parser.AstNode.factory;
import static de.benshu.cofi.parser.AstNode.listFactory;

@SuppressWarnings("unused")
public enum EarleyCofiParser {
    INSTANCE;

    // ------------------- TERMINALS -------------------------------------------------------------------------------------

    private static final Terminal AMPERSAND = Terminal.create(Token.Kind.SYMBOL, "&");
    private static final Terminal AT = Terminal.create(Token.Kind.SYMBOL, "@");
    private static final Terminal CLASS = Terminal.create(Token.Kind.CLASS, null);
    private static final Terminal COLON = Terminal.create(Token.Kind.SYMBOL, ":");
    private static final Terminal COLON_EQ = Terminal.create(Token.Kind.SYMBOL, ":=");
    private static final Terminal COMMA = Terminal.create(Token.Kind.SYMBOL, ",");
    private static final Terminal DOT = Terminal.create(Token.Kind.SYMBOL, ".");
    private static final Terminal EXTENDS = Terminal.create(Token.Kind.IDENTIFIER, "extends");
    private static final Terminal GT = Terminal.create(Token.Kind.SYMBOL, ">");
    private static final Terminal IDENTIFIER = Terminal.create(Token.Kind.IDENTIFIER, null);
    private static final Terminal IMPLICIT = Terminal.create(Token.Kind.IMPLICIT, null);
    private static final Terminal IMPORT = Terminal.create(Token.Kind.IDENTIFIER, "import");
    private static final Terminal LBRACE = Terminal.create(Token.Kind.SYMBOL, "{");
    private static final Terminal LITERAL_TOKEN = Terminal.create(Token.Kind.LITERAL, null);
    private static final Terminal LPAREN = Terminal.create(Token.Kind.SYMBOL, "(");
    private static final Terminal LT = Terminal.create(Token.Kind.SYMBOL, "<");
    private static final Terminal LT_COLON = Terminal.create(Token.Kind.SYMBOL, "<:");
    private static final Terminal LT_MINUS = Terminal.create(Token.Kind.SYMBOL, "<-");
    private static final Terminal MINUS = Terminal.create(Token.Kind.SYMBOL, "-");
    private static final Terminal MINUS_GT = Terminal.create(Token.Kind.SYMBOL, "->");
    private static final Terminal MODIFIER_TOKEN = Terminal.create(Token.Kind.MODIFIER, null);
    private static final Terminal MODULE = Terminal.create(Token.Kind.MODULE, null);
    private static final Terminal NEW_LINE = Terminal.NEW_LINE;
    private static final Terminal OBJECT = Terminal.create(Token.Kind.OBJECT, null);
    private static final Terminal OF = Terminal.create(Token.Kind.IDENTIFIER, "of");
    private static final Terminal PACKAGE = Terminal.create(Token.Kind.PACKAGE, null);
    private static final Terminal PIPE = Terminal.create(Token.Kind.SYMBOL, "|");
    private static final Terminal PLUS = Terminal.create(Token.Kind.SYMBOL, "+");
    private static final Terminal RBRACE = Terminal.create(Token.Kind.SYMBOL, "}");
    private static final Terminal RPAREN = Terminal.create(Token.Kind.SYMBOL, ")");
    private static final Terminal SEMICOLON = Terminal.create(Token.Kind.SYMBOL, ";");
    private static final Terminal THIS = Terminal.create(Token.Kind.THIS, null);
    private static final Terminal TRAIT = Terminal.create(Token.Kind.TRAIT, null);
    private static final Terminal UNION = Terminal.create(Token.Kind.UNION, null);

    // ------------------- NON TERMINALS ---------------------------------------------------------------------------------

    private static final NonTerminal ANNOTATION = NonTerminal.create("Annotation", factory(AnnotationImpl.class));
    private static final NonTerminal ANNOTATION_PROPERTY_ASSIGNMENT = NonTerminal.create("AnnotationPropertyAssignment", factory(AnnotationImpl.PropertyAssignment.class));
    private static final NonTerminal ANNOTATION_PROPERTY_ASSIGNMENTS = NonTerminal.createReturnPairA("AnnotationPropertyAssignments");
    private static final NonTerminal ABSTRACTION_STATEMENT_PIECES = NonTerminal.create("AbstractionStatementPieces", listFactory());
    private static final NonTerminal ANNOTATIONS = NonTerminal.create("Annotations", listFactory());
    private static final NonTerminal ARGUMENT_LIST = NonTerminal.createReturnPairA("ArgumentList", listFactory());
    private static final NonTerminal ARGUMENTS = NonTerminal.createPassThrough("Arguments");
    private static final NonTerminal ASSIGNMENT = NonTerminal.create("Assignment", EarleyCofiParser::userDefinedAssignment);
    private static final NonTerminal CLASS_DECLARATION = NonTerminal.create("ClassDeclaration", factory(ClassDeclaration.class));
    private static final NonTerminal CLOSURE = NonTerminal.create("Closure", factory(Closure.class));
    private static final NonTerminal CLOSURE_CASE = NonTerminal.create("ClosureCase", factory(Closure.Case.class));
    private static final NonTerminal CLOSURE_CASES = NonTerminal.create("ClosureCases", listFactory());
    private static final NonTerminal COMPILATION_UNIT = NonTerminal.create("CompilationUnit", factory(CompilationUnit.class));
    private static final NonTerminal EMPTY_PARAMETER_LIST = NonTerminal.createEmptyList("EmptyParameterList", listFactory());
    private static final NonTerminal EMPTY_TYPE_BOUNDS = NonTerminal.createEmptyList("EmptyTypeBounds", listFactory());
    private static final NonTerminal EMPTY_TYPE_LIST = NonTerminal.createEmptyList("EmptyTypeList", listFactory());
    private static final NonTerminal EMPTY_TYPE_PARAM_DECLS = NonTerminal.createEmptyList("EmptyTypeParamDecls", listFactory());
    private static final NonTerminal EXPRESSION = NonTerminal.createPassThrough("Expression");
    private static final NonTerminal EXPRESSIONS = NonTerminal.createReturnPairA("Expression");
    private static final NonTerminal EXPRESSION_STATEMENT = NonTerminal.create("ExpressionStatement", factory(ExpressionStatement.class));
    private static final NonTerminal EXTENDS_LIST = NonTerminal.createPassThrough("ExtendsList");
    private static final NonTerminal FULLY_QUALIFIED_NAME = NonTerminal.create("FullyQualifiedName", factory(FullyQualifiedName.class));
    private static final NonTerminal FULLY_QUALIFIED_NAME_ELEMENTS = NonTerminal.createPassThrough("FullyQualifiedNameElements");
    private static final NonTerminal FULLY_QUALIFIED_NAME_ELEMENT = NonTerminal.createPassThrough("FullyQualifiedNameElement");
    private static final NonTerminal FUNCTION_INVOCATION_EXPRESSION = NonTerminal.create("FunctionInvocationExpression", factory(FunctionInvocationExpression.class));
    private static final NonTerminal IDENTIFIERS = NonTerminal.createReturnPairA("Identifiers");
    private static final NonTerminal IMPORT_STATEMENT = NonTerminal.create("ImportStatement", factory(ImportStatement.class));
    private static final NonTerminal IMPORTS = NonTerminal.create("Imports", listFactory());
    private static final NonTerminal INITIALIZER_STATEMENT = NonTerminal.create("InitializerStatement", factory(InitializerStatement.class));
    private static final NonTerminal LITERAL_EXPRESSION = NonTerminal.create("LiteralExpression", factory(LiteralExpression.class));
    private static final NonTerminal LITERAL_TYPE = NonTerminal.create("LiteralType", factory(LiteralTypeExpression.class));
    private static final NonTerminal LOCAL_VARIABLE_DECLARATION = NonTerminal.create("LocalVariableDeclaration", factory(LocalVariableDeclaration.class));
    private static final NonTerminal MEMBER_ACCESS_EXPRESSION = NonTerminal.create("MemberAccessExpression", EarleyCofiParser::userDefinedMemberAccessExpression);
    private static final NonTerminal METHOD_BODY = NonTerminal.createPassThrough("MethodBody");
    private static final NonTerminal METHOD_DECLARATION = NonTerminal.create("MethodDeclaration", factory(MethodDeclarationImpl.class));
    private static final NonTerminal METHOD_DECLARATION_PIECE = NonTerminal.create("MethodDeclarationPiece", factory(MethodDeclarationImpl.Piece.class));
    private static final NonTerminal METHOD_DECLARATION_PIECES = NonTerminal.create("MethodDeclarationPieces", listFactory());
    private static final NonTerminal MODIFIER = NonTerminal.create("Modifier", factory(ModifierImpl.class));
    private static final NonTerminal MODULUE_DECLARATION = NonTerminal.create("CompilationUnitModuleDeclaration", factory(CompilationUnit.ModuleDeclaration.class));
    private static final NonTerminal PACKAGE_DECLARATION = NonTerminal.create("CompilationUnitPackageDeclaration", factory(CompilationUnit.PackageDeclaration.class));
    private static final NonTerminal MODIFIERS = NonTerminal.create("Modifiers", listFactory());
    private static final NonTerminal NAME = NonTerminal.createPassThrough("Name");
    private static final NonTerminal NAME_EXPRESSION = NonTerminal.create("NameExpression", EarleyCofiParser::userDefinedNameExpression);
    private static final NonTerminal NAMED_TYPE = NonTerminal.create("NamedType", factory(NamedTypeExpression.class));
    private static final NonTerminal OBJECT_DECL = NonTerminal.create("AbstractObjectDeclaration", factory(ObjectDeclaration.class));
    private static final NonTerminal PACKAGE_OBJECT_DECL = NonTerminal.create("PackageObjectDeclaration", factory(PackageObjectDeclaration.class));
    private static final NonTerminal PARAMETER = NonTerminal.create("Parameter", factory(ParameterImpl.class));
    private static final NonTerminal PARAMETER_LIST = NonTerminal.createReturnPairA("ParameterList", listFactory());
    private static final NonTerminal PARAMETERS_OPT = NonTerminal.createPassThrough("ParametersOpt");
    private static final NonTerminal PARAMETERS = NonTerminal.createPassThrough("Parameters");
    private static final NonTerminal PAREN_TYPE = NonTerminal.createPassThrough("ParenType");
    private static final NonTerminal PROPERTY_DECLARATION = NonTerminal.create("PropertyDeclaration", factory(PropertyDeclaration.class));
    private static final NonTerminal RELATIVE_NAME = NonTerminal.create("RelativeName", factory(RelativeNameImpl.class));
    private static final NonTerminal ROOT_EXPRESSION = NonTerminal.create("RootExpression", factory(RootExpression.class));
    private static final NonTerminal SINGLE_COMPONENT_NAME = NonTerminal.create("SingleComponentName", factory(RelativeNameImpl.class));
    private static final NonTerminal STATEMENT = NonTerminal.createPassThrough("Statement");
    private static final NonTerminal STATEMENTS = NonTerminal.create("Statements", listFactory());
    private static final NonTerminal THIS_EXPRESSION = NonTerminal.create("ThisExpression", factory(ThisExpression.class));
    private static final NonTerminal TRAIT_DECLARATION = NonTerminal.create("TraitDeclaration", factory(TraitDeclaration.class));
    private static final NonTerminal TYPE_DECLARATION = NonTerminal.createPassThrough("AbstractTypeDeclaration");
    private static final NonTerminal TUPLE_TYPE = NonTerminal.create("TupleType", factory(TypeExpression.class));
    private static final NonTerminal TYPE_ARGUMENTS = NonTerminal.createPassThrough("TypeArguments");
    private static final NonTerminal TYPE_EXPRESSION = NonTerminal.createPassThrough("Type");
    private static final NonTerminal TYPE_BODY = NonTerminal.create("TypeBody", factory(TypeBody.class));
    private static final NonTerminal TYPE_BODY_ELEMENT = NonTerminal.createPassThrough("TypeBodyElement");
    private static final NonTerminal TYPE_BODY_ELEMENTS = NonTerminal.create("TypeBodyElements", listFactory());
    private static final NonTerminal TYPE_BOUNDS = NonTerminal.createReturnPairA("TypeBounds", listFactory());
    private static final NonTerminal TYPE_BOUNDS_CHAIN = NonTerminal.createReturnPairA("TYPE_BOUNDS_CHAIN");
    private static final NonTerminal TYPE_LIST = NonTerminal.createReturnPairA("TypeList", listFactory());
    private static final NonTerminal TYPE_DECLARATIONS = NonTerminal.create("TypeDeclarations", listFactory());
    private static final NonTerminal TYPE_PARAMETERS = NonTerminal.create("TypeParameters", factory(TypeParameters.class));
    private static final NonTerminal TYPE_PARAMETER_DECLARATION = NonTerminal.create("TypeParameterDeclaration", factory(TypeParamDecl.class));
    private static final NonTerminal TYPE_PARAMETER_DECLARATIONS = NonTerminal.createReturnPairA("TypeParameterDeclarations", listFactory());
    private static final NonTerminal UNION_DECLARATION = NonTerminal.create("UnionDeclaration", factory(UnionDeclaration.class));
    private static final NonTerminal VARIABLE_TRAITS = NonTerminal.createReturnPairA("VariableTraits", listFactory());

    // ------------------- RULES -----------------------------------------------------------------------------------------

    static final Rule ANNOTATION_____AT__NAMED_TYPE = Rule.create(ANNOTATION, production(AT, NAMED_TYPE), 2, 0, 0);
    static final Rule ANNOTATION_____AT__NAMED_TYPE__LPAREN__EXPRESSION__RPAREN = Rule.create(ANNOTATION,
            production(AT, NAMED_TYPE, LPAREN, EXPRESSION, RPAREN), 2, 4, 0);
    static final Rule ANNOTATION_____AT__NAMED_TYPE__LPAREN__ANNOTATION_PROPERTY_ASSIGNMENTS__RPAREN = Rule.create(
            ANNOTATION, production(AT, NAMED_TYPE, LPAREN, ANNOTATION_PROPERTY_ASSIGNMENTS, RPAREN), 2, 0, 4);

    static final Rule ANNOTATION_PROPERTY_ASSIGNMENT_____IDENTIFIER__COLON_EQ__EXPRESSION = Rule.create(
            ANNOTATION_PROPERTY_ASSIGNMENT, production(IDENTIFIER, COLON_EQ, EXPRESSION), 1, 3);

    static final Rule ARGUMENTS_____LPAREN__ARGUMENT_LIST__RPAREN = Rule.create(ARGUMENTS,
            production(LPAREN, ARGUMENT_LIST, RPAREN), 2);

    static final Rule ASSIGNMENT_____IDENTIFIER__COLON_EQ__EXPRESSION = Rule.create(ASSIGNMENT, production(IDENTIFIER, COLON_EQ, EXPRESSION, SEMICOLON), 1, 3);
    static final Rule ASSIGNMENT_____EXPRESSION__DOT__IDENTIFIER__COLON_EQ__EXPRESSION = Rule.create(ASSIGNMENT, production(EXPRESSION, DOT, IDENTIFIER, COLON_EQ, EXPRESSION, SEMICOLON), 1, 3, 5);

    static final Rule CLASS_DECLARATION_____ANNOTATIONS__MODIFIERS__CLASS__IDENTIFIER__TYPE_PARAMETERS__PARAMETERS_OPT__EXTENDS_LIST__TYPE_BODY = Rule
            .create(
                    CLASS_DECLARATION,
                    production(ANNOTATIONS, MODIFIERS, CLASS, IDENTIFIER, TYPE_PARAMETERS, PARAMETERS_OPT, EXTENDS_LIST,
                            TYPE_BODY), 1, 2, 4, 5, 6, 7, 8);

    static final Rule CLOSURE_____LBRACE__CLOSURE_CASES__RBRACE = Rule.create(CLOSURE,
            production(LBRACE, CLOSURE_CASES, RBRACE), 2);

    static final Rule CLOSURE_CASE_____PARAMETER_LIST__MINUS_GT__STATEMENTS = Rule.create(CLOSURE_CASE,
            production(PARAMETER_LIST, MINUS_GT, STATEMENTS), 1, 3);

    static final Rule COMPILATION_UNIT_____MODULE_DECLARATION__PACKAGE_DECLARATION__IMPORTS__TYPE_DECLARATIONS = Rule
            .create(COMPILATION_UNIT, production(MODULUE_DECLARATION, PACKAGE_DECLARATION, IMPORTS, TYPE_DECLARATIONS), 1, 2,
                    3, 4);

    static final Rule MODULE_DECLARATION_____MODULE__FULLY_QUALIFIED_NAME__SEMICOLON = Rule.create(MODULUE_DECLARATION,
            production(MODULE, FULLY_QUALIFIED_NAME, SEMICOLON), 2);

    static final Rule PACKAGE_DECLARATION_____PACKAGE__FULLY_QUALIFIED_NAME__SEMICOLON = Rule.create(PACKAGE_DECLARATION,
            production(PACKAGE, FULLY_QUALIFIED_NAME, SEMICOLON), 2);

    static final Rule EMPTY_PARAMETER_LIST_____ = Rule.create(EMPTY_PARAMETER_LIST, production());

    static final Rule EMPTY_TYPE_BOUNDS_____ = Rule.create(EMPTY_TYPE_BOUNDS, production());

    static final Rule EMPTY_TYPE_LIST_____ = Rule.create(EMPTY_TYPE_LIST, production());

    static final Rule EMPTY_TYPE_PARAM_DECLS_____ = Rule.create(EMPTY_TYPE_PARAM_DECLS, production());

    static final Rule EXPRESSION_____FUNCTION_INVOCATION_EXPRESSION = Rule.createPassThrough(EXPRESSION, FUNCTION_INVOCATION_EXPRESSION);
    static final Rule EXPRESSION_____CLOSURE = Rule.createPassThrough(EXPRESSION, CLOSURE);
    static final Rule EXPRESSION_____LITERAL_EXPRESSION = Rule.createPassThrough(EXPRESSION, LITERAL_EXPRESSION);
    static final Rule EXPRESSION_____MEMBER_ACCESS_EXPRESSION = Rule.createPassThrough(EXPRESSION, MEMBER_ACCESS_EXPRESSION);
    static final Rule EXPRESSION_____NAME_EXPRESSION = Rule.createPassThrough(EXPRESSION, NAME_EXPRESSION);
    static final Rule EXPRESSION_____THIS_EXPRESSION = Rule.createPassThrough(EXPRESSION, THIS_EXPRESSION);

    static final Rule EXPRESSION_STATEMENT_____ANNOTATIONS__METHOD_INVOCATION_EXPRESSION__SEMICOLON = Rule.create(
            EXPRESSION_STATEMENT, production(ANNOTATIONS, FUNCTION_INVOCATION_EXPRESSION, SEMICOLON), 1, 2);

    static final Rule EXTENDS_LIST_____EMPTY_TYPE_LIST = Rule.createPassThrough(EXTENDS_LIST, EMPTY_TYPE_LIST);
    static final Rule EXTENDS_LIST_____EXTENDS__TYPE_LIST = Rule.create(EXTENDS_LIST, production(EXTENDS, TYPE_LIST), 2);

    static final Rule IMPORT_STATEMENT_____IMPORT__FULLY_QUALIFIED_NAME__SEMICOLON = Rule.create(IMPORT_STATEMENT,
            production(IMPORT, FULLY_QUALIFIED_NAME, SEMICOLON), 2);

    static final Rule INITIALIZER_STATEMENT_____STATEMENT = Rule.create(INITIALIZER_STATEMENT, production(STATEMENT), 1);

    static final Rule LITERAL_EXPRESSION_____LITERAL_TOKEN = Rule
            .create(LITERAL_EXPRESSION, production(LITERAL_TOKEN), 1);

    static final Rule LITERAL_TYPE_____LITERAL_TOKEN = Rule.create(LITERAL_TYPE, production(LITERAL_TOKEN), 1);

    static final Rule LOCAL_VARIABLE_DECLARATION_____ANNOTATIONS__TYPE_PARAMETERS__IDENTIFIER__COLON__TYPE_EXPRESSION__COLON_EQ__EXPRESSION__SEMICOLON = Rule
            .create(LOCAL_VARIABLE_DECLARATION,
                    production(ANNOTATIONS, MODIFIERS, IDENTIFIER, COLON, TYPE_EXPRESSION, COLON_EQ, EXPRESSION, SEMICOLON), 1,
                    2, 3, 5, 7);
    static final Rule LOCAL_VARIABLE_DECLARATION_____ANNOTATIONS__TYPE_PARAMETERS__IDENTIFIER__COLON__TYPE_EXPRESSION__SEMICOLON = Rule
            .create(LOCAL_VARIABLE_DECLARATION,
                    production(ANNOTATIONS, MODIFIERS, IDENTIFIER, COLON, TYPE_EXPRESSION, SEMICOLON), 1, 2, 3, 5, 0);

    static final Rule MEMBER_ACCESS_EXPRESSION_____EXPRESSION__DOT__SINGLE_COMPONENT_NAME = Rule.create(MEMBER_ACCESS_EXPRESSION,
            production(EXPRESSION, DOT, SINGLE_COMPONENT_NAME), 1, 3);
    static final Rule MEMBER_ACCESS_EXPRESSION_____ROOT_EXPRESSION__DOT__SINGLE_COMPONENT_NAME = Rule.create(MEMBER_ACCESS_EXPRESSION,
            production(ROOT_EXPRESSION, DOT, SINGLE_COMPONENT_NAME), 1, 3);

    static final Rule METHOD_DECLARATION_____ANNOTATIONS__MODIFIERS__METHOD_DECLARATION_PIECES__COLON__TYPE_EXPRESSION__METHOD_BODY = Rule
            .create(METHOD_DECLARATION,
                    production(ANNOTATIONS, MODIFIERS, METHOD_DECLARATION_PIECES, COLON, TYPE_EXPRESSION, METHOD_BODY), 1, 2, 3,
                    5, 6);

    static final Rule METHOD_DECLARATION_PIECE_____IDENTIFIER__TYPE_PARAMETERS__PARAMETERS = Rule.create(
            METHOD_DECLARATION_PIECE, production(IDENTIFIER, TYPE_PARAMETERS, PARAMETERS), 1, 2, 3);

    static final Rule NAME_____FULLY_QUALIFIED_NAME = Rule.create(NAME, production(FULLY_QUALIFIED_NAME), 1);
    static final Rule NAME_____RELATIVE_NAME = Rule.create(NAME, production(RELATIVE_NAME), 1);

    static final Rule NAME_EXPRESSION_____SINGLE_COMPONENT_NAME = Rule.create(NAME_EXPRESSION, production(SINGLE_COMPONENT_NAME), 1);

    static final Rule FULLY_QUALIFIED_NAME_____FULLY_QUALIFIED_NAME_ELEMENTS__TYPE_ARGUMENTS = Rule.create(FULLY_QUALIFIED_NAME,
            production(FULLY_QUALIFIED_NAME_ELEMENTS, TYPE_ARGUMENTS), 1, 2);

    static final Rule FULLY_QUALIFIED_NAME_ELEMENT_____DOT__IDENTIFIER = Rule.create(FULLY_QUALIFIED_NAME_ELEMENT,
            production(DOT, IDENTIFIER), 2);

    static final Rule FUNCTION_INVOCATION_EXPRESSION_____PRIMARY_EXPRESSION__ARGUMENTS = Rule.create(
            FUNCTION_INVOCATION_EXPRESSION, production(EXPRESSION, ARGUMENTS), 1, 2);

    static final Rule METHOD_BODY_____ = Rule.create(METHOD_BODY, production(SEMICOLON), 0);
    static final Rule METHOD_BODY_____LBRACE__STATEMENTS___RBRACE = Rule.create(METHOD_BODY,
            production(LBRACE, STATEMENTS, RBRACE), 2);

    static final Rule MODIFIER_____MODIFIER_TOKEN = Rule.create(MODIFIER, production(MODIFIER_TOKEN), 1);

    static final Rule NAMED_TYPE_____NAME = Rule.create(NAMED_TYPE, production(NAME), 1);

    static final Rule OBJECT_DECL_____ANNOTATIONS__MODIFIERS__OBJECT__IDENTIFIER__EXTENDS_LIST__TYPE_BODY = Rule.create(
            OBJECT_DECL, production(ANNOTATIONS, MODIFIERS, OBJECT, IDENTIFIER, EXTENDS_LIST, TYPE_BODY), 1, 2, 4, 5, 6);

    static final Rule PACKAGE_OBJECT_DECLARATION_____ANNOTATIONS__MODIFIERS__PACKAGE__EXTENDS_LIST__TYPE_BODY = Rule
            .create(PACKAGE_OBJECT_DECL, production(ANNOTATIONS, MODIFIERS, PACKAGE, EXTENDS_LIST, TYPE_BODY), 1, 2, 4, 5);

    static final Rule PARAMETER_____ANNOTATIONS__IDENTIFIER__COLON__TYPE_EXPRESSION = Rule.create(PARAMETER,
            production(ANNOTATIONS, MODIFIERS, IDENTIFIER, COLON, TYPE_EXPRESSION), 1, 2, 3, 5, 0, 0);

    static final Rule PARAMETERS_____LPAREN__PARAMETER_LIST__RPAREN = Rule.create(PARAMETERS,
            production(LPAREN, PARAMETER_LIST, RPAREN), 2);

    static final Rule PARAMETERS_OPT_____ = Rule.create(PARAMETERS_OPT, production(EMPTY_PARAMETER_LIST), 1);
    static final Rule PARAMETERS_OPT_____PARAMETERS = Rule.create(PARAMETERS_OPT, production(PARAMETERS), 1);

    static final Rule PAREN_TYPE_____TUPLE_TYPE = Rule.createPassThrough(PAREN_TYPE, TUPLE_TYPE);

    static final Rule PROPERTY_DECLARATION_____ = Rule.create(
            PROPERTY_DECLARATION,
            production(ANNOTATIONS, MODIFIERS, VARIABLE_TRAITS, IDENTIFIER, COLON, TYPE_EXPRESSION, COLON_EQ, EXPRESSION,
                    SEMICOLON), 1, 2, 4, 3, 6, 8);
    static final Rule PROPERTY_DECLARATION_____A = Rule.create(PROPERTY_DECLARATION,
            production(ANNOTATIONS, MODIFIERS, IDENTIFIER, COLON, TYPE_EXPRESSION, COLON_EQ, EXPRESSION, SEMICOLON), 1, 2, 3,
            0, 5, 7);
    static final Rule PROPERTY_DECLARATION_____AB = Rule
            .create(PROPERTY_DECLARATION,
                    production(ANNOTATIONS, MODIFIERS, VARIABLE_TRAITS, IDENTIFIER, COLON, TYPE_EXPRESSION, SEMICOLON), 1, 2, 4,
                    3, 6);
    static final Rule PROPERTY_DECLARATION_____ABC = Rule.create(PROPERTY_DECLARATION,
            production(ANNOTATIONS, MODIFIERS, IDENTIFIER, COLON, TYPE_EXPRESSION, SEMICOLON), 1, 2, 3, 0, 5);

    static final Rule RELATIVE_NAME_____IDENTIFIER__TYPE_ARGUMENTS = Rule.create(RELATIVE_NAME, production(IDENTIFIER, TYPE_ARGUMENTS), 1, 2);

    static final Rule ROOT_EXPRESSION_____ = Rule.create(ROOT_EXPRESSION, production());

    static final Rule SINGLE_COMPONENT_NAME_____IDENTIFIER__TYPE_ARGUMENTS = Rule.create(SINGLE_COMPONENT_NAME, production(IDENTIFIER, TYPE_ARGUMENTS), 1, 2);

    static final Rule STATEMENT_____ASSIGNMENT = Rule.createPassThrough(STATEMENT, ASSIGNMENT);
    static final Rule STATEMENT_____EXPRESSION_STATEMENT = Rule.createPassThrough(STATEMENT, EXPRESSION_STATEMENT);
    static final Rule STATEMENT_____LOCAL_VARIABLE_DECLARATION = Rule.createPassThrough(STATEMENT, LOCAL_VARIABLE_DECLARATION);

    static final Rule THIS_EXPRESSION_____THIS = Rule.create(THIS_EXPRESSION, production(THIS), 1);

    static final Rule TRAIT_DECLARATION_____ANNOTATIONS__MODIFIERS__TRAIT__IDENTIFIER__TYPE_PARAMETERS__EXTENDS_LIST__TYPE_BODY = Rule
            .create(TRAIT_DECLARATION,
                    production(ANNOTATIONS, MODIFIERS, TRAIT, IDENTIFIER, TYPE_PARAMETERS, EXTENDS_LIST, TYPE_BODY), 1, 2, 4, 5,
                    6, 7);

    static final Rule TUPLE_TYPE_____LPAREN__TYPE_LIST__RPAREN = Rule.create(TUPLE_TYPE,
            production(LPAREN, TYPE_LIST, RPAREN), 2);
    static final Rule TUPLE_TYPE_____LPAREN__EMPTY_TYPE_LIST__RPAREN = Rule.create(TUPLE_TYPE,
            production(LPAREN, EMPTY_TYPE_LIST, RPAREN), 2);

    static final Rule TYPE_ARGUMENTS_____ = Rule.create(TYPE_ARGUMENTS, production(), 0);
    static final Rule TYPE_ARGUMENTS_____LT__TYPE_LIST__GT = Rule.create(TYPE_ARGUMENTS, production(LT, TYPE_LIST, GT), 2);

    static final Rule TYPE_BODY_____LBRACE__TYPE_BODY_ELEMENTS__RBRACE = Rule.create(TYPE_BODY,
            production(LBRACE, TYPE_BODY_ELEMENTS, RBRACE), 2);

    static final Rule TYPE_BODY_ELEMENT_____INITIALIZER_STATEMENT = Rule.createPassThrough(TYPE_BODY_ELEMENT,
            INITIALIZER_STATEMENT);
    static final Rule TYPE_BODY_ELEMENT_____METHOD_DECLARATION = Rule.createPassThrough(TYPE_BODY_ELEMENT,
            METHOD_DECLARATION);
    static final Rule TYPE_BODY_ELEMENT_____PROPERTY_DECLARATION = Rule.createPassThrough(TYPE_BODY_ELEMENT,
            PROPERTY_DECLARATION);
    static final Rule TYPE_BODY_ELEMENT_____TYPE_DECLARATION = Rule
            .createPassThrough(TYPE_BODY_ELEMENT, TYPE_DECLARATION);

    static final Rule TYPE_DECLARATION_____CLASS_DECLARATION = Rule
            .createPassThrough(TYPE_DECLARATION, CLASS_DECLARATION);
    static final Rule TYPE_DECLARATION_____OBJECT_DECL = Rule.createPassThrough(TYPE_DECLARATION, OBJECT_DECL);
    static final Rule TYPE_DECLARATION_____PACKAGE_OBJECT_DECL = Rule.createPassThrough(TYPE_DECLARATION,
            PACKAGE_OBJECT_DECL);
    static final Rule TYPE_DECLARATION_____TRAIT_DECLARATION = Rule
            .createPassThrough(TYPE_DECLARATION, TRAIT_DECLARATION);
    static final Rule TYPE_DECLARATION_____UNION_DECLARATION = Rule
            .createPassThrough(TYPE_DECLARATION, UNION_DECLARATION);

    static final Rule TYPE_EXPRESSION_____LITERAL_TYPE = Rule.createPassThrough(TYPE_EXPRESSION, LITERAL_TYPE);
    static final Rule TYPE_EXPRESSION_____NAMED_TYPE = Rule.createPassThrough(TYPE_EXPRESSION, NAMED_TYPE);
    static final Rule TYPE_EXPRESSION_____PAREN_TYPE = Rule.createPassThrough(TYPE_EXPRESSION, PAREN_TYPE);

    static final Rule TYPE_PARAMETERS_____ = Rule.create(TYPE_PARAMETERS,
            production(EMPTY_TYPE_PARAM_DECLS, EMPTY_TYPE_BOUNDS), 1, 2);
    static final Rule TYPE_PARAMETERS_____LT__TYPE_PARAMETER_DESCRIPTORS__GT = Rule.create(TYPE_PARAMETERS,
            production(LT, TYPE_PARAMETER_DECLARATIONS, GT), 2, 0);
    static final Rule TYPE_PARAMETERS_____LT__TYPE_PARAMETER_DESCRIPTORS__PIPE__TYPE_BOUNDS__GT = Rule.create(
            TYPE_PARAMETERS, production(LT, TYPE_PARAMETER_DECLARATIONS, PIPE, TYPE_BOUNDS, GT), 2, 4);

    static final Rule TYPE_PARAMETER_DECLARATION_____IDENTIFIER = Rule.create(TYPE_PARAMETER_DECLARATION,
            production(IDENTIFIER), 1);
    static final Rule TYPE_PARAMETER_DECLARATION_____IMPLICIT__IDENTIFIER = Rule.create(TYPE_PARAMETER_DECLARATION,
            production(IMPLICIT, IDENTIFIER), 2, 1);
    static final Rule TYPE_PARAMETER_DECLARATION_____MINUS__IDENTIFIER = Rule.create(TYPE_PARAMETER_DECLARATION,
            production(MINUS, IDENTIFIER), 2, 1);
    static final Rule TYPE_PARAMETER_DECLARATION_____PLUS__IDENTIFIER = Rule.create(TYPE_PARAMETER_DECLARATION,
            production(PLUS, IDENTIFIER), 2, 1);
    static final Rule TYPE_PARAMETER_DECLARATION_____PLUS__MINUS__IDENTIFIER = Rule.create(TYPE_PARAMETER_DECLARATION,
            production(PLUS, MINUS, IDENTIFIER), 3, 1, 2);

    static final Rule UNION_DECLARATION_____ANNOTATIONS__MODIFIERS__UNION__IDENTIFIER__TYPE_PARAMETERS__OF__TYPE_LIST__TYPE_BODY = Rule
            .create(UNION_DECLARATION,
                    production(ANNOTATIONS, MODIFIERS, UNION, IDENTIFIER, TYPE_PARAMETERS, OF, TYPE_LIST, TYPE_BODY), 1, 2, 4, 5,
                    7, 8);

    static final ImmutableSet<Rule> RULES;

    static {
        ImmutableSet.Builder<Rule> builder = ImmutableSet.builder();

        for (Field f : EarleyCofiParser.class.getDeclaredFields()) {
            if (f.getType() == Rule.class) {
                try {
                    builder.add((Rule) f.get(null));
                } catch (Exception e) {
                    throw Throwables.propagate(e);
                }
            }
        }

        builder.addAll(Rule.createList(ANNOTATIONS, ANNOTATION, true));
        builder.addAll(Rule.createList(FULLY_QUALIFIED_NAME_ELEMENTS, FULLY_QUALIFIED_NAME_ELEMENT, false));
        builder.addAll(Rule.createList(IMPORTS, IMPORT_STATEMENT, true));
        builder.addAll(Rule.createList(TYPE_BODY_ELEMENTS, TYPE_BODY_ELEMENT, true));
        builder.addAll(Rule.createList(METHOD_DECLARATION_PIECES, METHOD_DECLARATION_PIECE, false));
        builder.addAll(Rule.createList(MODIFIERS, MODIFIER, true));
        builder.addAll(Rule.createList(STATEMENTS, STATEMENT, true));
        builder.addAll(Rule.createList(TYPE_DECLARATIONS, TYPE_DECLARATION, true));
        builder.addAll(Rule.createList(CLOSURE_CASES, CLOSURE_CASE, false));

        builder.addAll(Rule.createSeparatedList(ANNOTATION_PROPERTY_ASSIGNMENTS, ANNOTATION_PROPERTY_ASSIGNMENT, COMMA, true));
        builder.addAll(Rule.createSeparatedList(ARGUMENT_LIST, EXPRESSION, COMMA, true));
        builder.addAll(Rule.createSeparatedList(EXPRESSIONS, EXPRESSION, COMMA, true));
        builder.addAll(Rule.createSeparatedList(IDENTIFIERS, IDENTIFIER, DOT, false));
        builder.addAll(Rule.createSeparatedList(PARAMETER_LIST, PARAMETER, COMMA, true));
        builder.addAll(Rule.createSeparatedList(TYPE_LIST, TYPE_EXPRESSION, COMMA, false));
        builder.addAll(Rule.createSeparatedList(TYPE_BOUNDS, TYPE_BOUNDS_CHAIN, COMMA, false));
        builder.addAll(Rule.createSeparatedList(TYPE_BOUNDS_CHAIN, TYPE_EXPRESSION, LT_COLON, false));
        builder.addAll(Rule.createSeparatedList(TYPE_PARAMETER_DECLARATIONS, TYPE_PARAMETER_DECLARATION, COMMA, false));
        builder.addAll(Rule.createSeparatedList(VARIABLE_TRAITS, NAMED_TYPE, AMPERSAND, true));

        RULES = builder.build();
    }

    // ------------------- GRAMMAR ---------------------------------------------------------------------------------------

    private static Grammar GRAMMAR = Grammar.create(RULES, COMPILATION_UNIT);

    // ------------------- UTILITIES -------------------------------------------------------------------------------------

    private static ImmutableList<Symbol> production(Symbol... symbols) {
        return ImmutableList.copyOf(symbols);
    }

    // ------------------- "USER DEFINED" FACTORIES ----------------------------------------------------------------------

    private static <X extends ModelContext<X>> Object userDefinedAssignment(Object[] args) {
        return new UserDefinedStatement<>(
                ImmutableList.copyOf(args),
                new UserDefinedNodeTransformation<X, UserDefinedStatement<X>, Statement<X>>() {
                    @Override
                    public Optional<Statement<X>> apply(TransformationContext<X> context, UserDefinedStatement<X> untransformed) {
                        final boolean simple = untransformed.getSymbols().size() == 2;
                        final int offset = simple ? 0 : 1;

                        final RelativeNameImpl<X> variableName = RelativeNameImpl.of((Token) untransformed.getSymbol(offset + 0));
                        final ExpressionNode<X> value = (ExpressionNode<X>) untransformed.getSymbol(offset + 1);

                        final ExpressionNode<X> transformedPrimary = simple
                                ? NameExpression.of(variableName)
                                : MemberAccessExpression.of((ExpressionNode<X>) untransformed.getSymbol(0), variableName);

                        return Optional.of(ExpressionStatement.of(
                                ImmutableList.of(),
                                FunctionInvocationExpression.of(
                                        MemberAccessExpression.of(
                                                transformedPrimary,
                                                RelativeNameImpl.of(ArtificialToken.create(Token.Kind.IDENTIFIER, "set"))
                                        ),
                                        ImmutableList.of(value)
                                )));
                    }

                    @Override
                    public boolean test(TransformationContext<X> context, Statement<X> transformed) {
                        return true;
                    }
                }
        );
    }

    private static <X extends ModelContext<X>> Object userDefinedMemberAccessExpression(Object[] args) {
        return new UserDefinedExpression<>(
                ImmutableList.copyOf(args),
                new UserDefinedNodeTransformation<X, UserDefinedExpression<X>, ExpressionNode<X>>() {
                    @Override
                    public Optional<ExpressionNode<X>> apply(TransformationContext<X> context, UserDefinedExpression<X> untransformed) {
                        return Optional.of(MemberAccessExpression.of(
                                (ExpressionNode<X>) untransformed.getSymbol(0),
                                (RelativeNameImpl<X>) untransformed.getSymbol(1)
                        ));
                    }

                    @Override
                    public boolean test(TransformationContext<X> context, ExpressionNode<X> transformed) {
                        ProperTypeMixin<X, ?> primaryType = context.lookUpTypeOf(((MemberAccessExpression<X>) transformed).primary);
                        ProperTypeConstructorMixin<X, ?, ?> gettable = (ProperTypeConstructorMixin<X, ?, ?>) context.resolveType(Fqn.from("cofi", "lang", "Gettable"));

                        return !primaryType.tryGetInvocationOf(gettable).isPresent();
                    }
                },
                new UserDefinedNodeTransformation<X, UserDefinedExpression<X>, ExpressionNode<X>>() {
                    @Override
                    public Optional<ExpressionNode<X>> apply(TransformationContext<X> context, UserDefinedExpression<X> untransformed) {
                        return Optional.of(FunctionInvocationExpression.of(
                                MemberAccessExpression.of(
                                        MemberAccessExpression.of(
                                                (ExpressionNode<X>) untransformed.getSymbol(0),
                                                (RelativeNameImpl<X>) untransformed.getSymbol(1)
                                        ),
                                        RelativeNameImpl.of(ArtificialToken.create(Token.Kind.IDENTIFIER, "get"))
                                ),
                                ImmutableList.of()
                        ));
                    }

                    @Override
                    public boolean test(TransformationContext<X> context, ExpressionNode<X> transformed) {
                        MemberAccessExpression<X> primary = (MemberAccessExpression<X>) ((FunctionInvocationExpression<X>) transformed).primary;
                        ProperTypeMixin<X, ?> primaryPrimaryType = context.lookUpTypeOf(primary.primary);
                        ProperTypeConstructorMixin<X, ?, ?> gettable = (ProperTypeConstructorMixin<X, ?, ?>) context.resolveType(Fqn.from("cofi", "lang", "Gettable"));

                        return primaryPrimaryType.tryGetInvocationOf(gettable).isPresent();
                    }
                }
        );
    }

    private static <X extends ModelContext<X>> Object userDefinedNameExpression(Object[] args) {
        return new UserDefinedExpression<>(
                ImmutableList.copyOf(args),
                new UserDefinedNodeTransformation<X, UserDefinedExpression<X>, ExpressionNode<X>>() {
                    @Override
                    public Optional<ExpressionNode<X>> apply(TransformationContext<X> context, UserDefinedExpression<X> untransformed) {
                        RelativeNameImpl<X> name = (RelativeNameImpl<X>) untransformed.getSymbol(0);
                        TypeMixin<X, ?> type = context.resolve(Iterables.getOnlyElement(name.ids).getLexeme());

                        type = type instanceof TypeConstructorMixin && ((TypeConstructorMixin<X, ?, ?>) type).getParameters().isEmpty()
                                ? ((TypeConstructorMixin<X, ?, ?>) type).applyTrivially()
                                : type;

                        if (!type.getKind().isProperOrder())
                            return Optional.empty();

                        ProperTypeConstructorMixin<X, ?, ?> gettable = (ProperTypeConstructorMixin<X, ?, ?>) context.resolveType(Fqn.from("cofi", "lang", "Gettable"));
                        ProperTypeMixin<X, ?> properType = (ProperTypeMixin<X, ?>) type;

                        Optional<TypeConstructorInvocation<X>> invocation = properType.tryGetInvocationOf(gettable);

                        return invocation.map(i -> FunctionInvocationExpression.of(
                                MemberAccessExpression.of(
                                        NameExpression.of(name),
                                        RelativeNameImpl.of(ArtificialToken.create(Token.Kind.IDENTIFIER, "get"))
                                ),
                                ImmutableList.of()
                        ));
                    }

                    @Override
                    public boolean test(TransformationContext<X> context, ExpressionNode<X> transformed) {
                        return true;
                    }
                },
                new UserDefinedNodeTransformation<X, UserDefinedExpression<X>, ExpressionNode<X>>() {
                    @Override
                    public Optional<ExpressionNode<X>> apply(TransformationContext<X> context, UserDefinedExpression<X> untransformed) {
                        RelativeNameImpl<X> name = (RelativeNameImpl<X>) untransformed.getSymbol(0);
                        TypeMixin<X, ?> type = context.resolve(Iterables.getOnlyElement(name.ids).getLexeme());

                        type = type instanceof TypeConstructorMixin && ((TypeConstructorMixin<X, ?, ?>) type).getParameters().isEmpty()
                                ? ((TypeConstructorMixin<X, ?, ?>) type).applyTrivially()
                                : type;

                        if (!type.getKind().isProperOrder())
                            return Optional.of(NameExpression.of(name));

                        ProperTypeConstructorMixin<X, ?, ?> gettable = (ProperTypeConstructorMixin<X, ?, ?>) context.resolveType(Fqn.from("cofi", "lang", "Gettable"));
                        ProperTypeMixin<X, ?> properType = (ProperTypeMixin<X, ?>) type;

                        Optional<TypeConstructorInvocation<X>> invocation = properType.tryGetInvocationOf(gettable);

                        return invocation.isPresent() ? Optional.empty() : Optional.of(NameExpression.of(name));
                    }

                    @Override
                    public boolean test(TransformationContext<X> context, ExpressionNode<X> transformed) {
                        return true;
                    }
                }
        );
    }

    // ------------------- PUBLIC API ------------------------------------------------------------------------------------

    public <X extends ModelContext<X>> CompilationUnit<X> parse(Reader input) {
        return (CompilationUnit<X>) GRAMMAR.parse(TokenStreamImpl.create(input));
    }

}
