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
public class TraitDeclaration<X extends ModelContext<X>> extends AbstractTypeDeclaration<X> implements AnnotatedNodeMixin<X>, CopyableTraitDeclaration<X> {
    @AstNodeConstructorMethod
    @CopyFactory
    public static <X extends ModelContext<X>> TraitDeclaration<X> of(ImmutableList<AnnotationImpl<X>> annotations, ImmutableList<ModifierImpl<X>> modifiers, Token id, TypeParameters<X> typeParameters, ImmutableList<TypeExpression<X>> extending, TypeBody<X> body) {

        return new TraitDeclaration<>(annotations, modifiers, id, typeParameters, extending, body);
    }

    public final Token id;
    public final TypeParameters<X> typeParameters;
    public final UnboundTemplateTypeConstructor<X> unbound;

    public TraitDeclaration(ImmutableList<AnnotationImpl<X>> annotations,
                            ImmutableList<ModifierImpl<X>> modifiers, Token id, TypeParameters<X> typeParameters,
                            ImmutableList<TypeExpression<X>> extending, TypeBody<X> body) {
        super(annotations, modifiers, extending, body);
        this.id = id;

        this.unbound = AbstractTemplateTypeConstructor.<X>create(
                TemplateTypeDeclaration.memoizing(
                        x -> x.lookUpTypeParametersOf(this),
                        x -> getExtending().isEmpty()
                                ? ImmutableList.of(SourceType.of(x.getTypeSystem().getTop(), id))
                                : getExtending().stream().map(e -> SourceType.of(x.lookUpTypeOf(e), e.getSourceSnippet())).collect(Collectors.list()),
                        x -> x.lookUpMemberDescriptorsOf(this),
                        x -> IndividualTags.empty()
                                .set(TypeTags.NAME, FullyQualifiedTypeName.create(() -> x.lookUpFqnOf(this)))
                                .set(AbstractTypeDeclaration.Tag.INSTANCE, this)
                )
        );

        this.typeParameters = typeParameters;
    }

    @Override
    public Token getId() {
        return id;
    }

    @Override
    public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
        return visitor.visitTraitDeclaration(this, aggregate);
    }

    @Override
    public <N, L extends N, D extends L, S extends N, E extends N, T extends N> D accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
        return transformer.transformTraitDeclaration(this);
    }

    @Override
    public TypeParameters<X> getTypeParameters() {
        return typeParameters;
    }

    @Override
    public boolean isTrait() {
        return true;
    }

    @Override
    public ProperTypeConstructorMixin<X, ?, ?> bind(X context) {
        return unbound.bind(context);
    }
}
