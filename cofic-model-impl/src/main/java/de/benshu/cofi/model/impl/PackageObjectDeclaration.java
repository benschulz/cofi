package de.benshu.cofi.model.impl;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.parser.AstNodeConstructorMethod;
import de.benshu.cofi.parser.lexer.Token;
import de.benshu.cofi.types.impl.templates.AbstractTemplateTypeConstructor;
import de.benshu.cofi.types.impl.ProperTypeConstructorMixin;
import de.benshu.cofi.types.impl.templates.UnboundTemplateTypeConstructor;
import de.benshu.cofi.types.impl.declarations.SourceType;
import de.benshu.cofi.types.impl.declarations.TemplateTypeDeclaration;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.commons.core.streams.Collectors;
import de.benshu.jswizzle.copyable.CopyFactory;
import de.benshu.jswizzle.copyable.Copyable;

@Copyable
public class PackageObjectDeclaration<X extends ModelContext<X>> extends AbstractObjectDeclaration<X> implements AnnotatedNodeMixin<X>, CopyablePackageObjectDeclaration<X> {
    @AstNodeConstructorMethod
    public static <X extends ModelContext<X>> PackageObjectDeclaration<X> of(ImmutableList<AnnotationImpl<X>> annotations,
                                                                             ImmutableList<ModifierImpl<X>> modifiers, ImmutableList<TypeExpression<X>> extending,
                                                                             TypeBody<X> body) {
        return of(annotations, modifiers, null, TypeParameters.none(), extending, body);
    }

    @CopyFactory
    public static <X extends ModelContext<X>> PackageObjectDeclaration<X> of(ImmutableList<AnnotationImpl<X>> annotations, ImmutableList<ModifierImpl<X>> modifiers, @SuppressWarnings("unused") Token id, TypeParameters<X> typeParameters, ImmutableList<TypeExpression<X>> extending, TypeBody<X> body) {
        return new PackageObjectDeclaration<>(annotations, modifiers, typeParameters, extending, body);
    }

    private final TypeParameters<X> typeParameters;
    private final UnboundTemplateTypeConstructor<X> unbound;

    private PackageObjectDeclaration(ImmutableList<AnnotationImpl<X>> annotations, ImmutableList<ModifierImpl<X>> modifiers, TypeParameters<X> typeParameters, ImmutableList<TypeExpression<X>> extending, TypeBody<X> body) {
        super(annotations, modifiers, extending, body);

        this.typeParameters = typeParameters;
        this.unbound = AbstractTemplateTypeConstructor.<X>create(
                TemplateTypeDeclaration.memoizing(
                        x -> x.lookUpTypeParametersOf(this),
                        x -> getExtending().stream().map(e -> SourceType.of(x.lookUpTypeOf(e), e.getSourceSnippet())).collect(Collectors.list()),
                        x -> x.lookUpMemberDescriptorsOf(this),
                        x -> IndividualTags.empty()
                                .set(TypeTags.NAME, () -> x.lookUpFqnOf(PackageObjectDeclaration.this).toString())
                                .set(AbstractTypeDeclaration.Tag.INSTANCE, PackageObjectDeclaration.this)
                )
        );
    }

    @Override
    public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
        return visitor.visitPackageObjectDeclaration(this, aggregate);
    }

    @Override
    public <N, L extends N, D extends L, S extends N, E extends N, T extends N> D accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
        return transformer.transformPackageObjectDeclaration(this);
    }

    @Override
    public Token getId() {
        throw new AssertionError();
    }

    @Override
    public String getName() {
        return "<package>";
    }

    @Override
    public TypeParameters<X> getTypeParameters() {
        return typeParameters;
    }

    @Override
    public ProperTypeConstructorMixin<X, ?, ?> bind(X context) {
        return unbound.bind(context);
    }
}
