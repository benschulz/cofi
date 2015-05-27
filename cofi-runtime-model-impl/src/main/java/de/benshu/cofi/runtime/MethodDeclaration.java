package de.benshu.cofi.runtime;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.runtime.internal.Ancestry;
import de.benshu.cofi.runtime.internal.Constructor;
import de.benshu.cofi.runtime.internal.TypeParameterListReference;
import de.benshu.cofi.runtime.internal.TypeReference;
import de.benshu.cofi.types.TemplateType;
import de.benshu.cofi.types.TemplateTypeConstructor;
import de.benshu.cofi.types.TypeParameterList;
import de.benshu.commons.core.Debuggable;
import de.benshu.commons.core.Optional;
import de.benshu.jswizzle.data.Data;

import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MethodDeclaration extends TypeBody.Containable implements MemberDeclaration, MethodDeclarationAccessors {
    @Data
    final ImmutableSet<Annotation> annotations;
    @Data
    final String name;
    @Data
    final ImmutableList<Piece> pieces;
    @Data
    final Optional<ImmutableList<Statement>> body;

    final Supplier<TypeParameterList> typeParameters;
    final Supplier<TemplateType> signature;
    final transient Supplier<TemplateTypeConstructor> signatureConstructor;

    public MethodDeclaration(
            Ancestry ancestry,
            ImmutableSet<Constructor<Annotation>> annotations,
            String name,
            ImmutableList<Constructor<Piece>> pieces,
            TypeParameterListReference typeParameters,
            TypeReference<TemplateType> signature,
            TypeReference<TemplateTypeConstructor> signatureConstructor,
            Optional<ImmutableList<Constructor<Statement>>> body) {

        final Ancestry ancestryIncludingMe = ancestry.append(this);

        this.annotations = ancestryIncludingMe.constructAll(annotations);
        this.name = name;
        this.pieces = ancestryIncludingMe.constructAll(pieces);
        this.typeParameters = ancestryIncludingMe.resolve(typeParameters);
        this.signature = ancestryIncludingMe.resolve(signature);
        this.signatureConstructor = ancestryIncludingMe.resolve(signatureConstructor);
        this.body = body.map(ancestryIncludingMe::constructAll);
    }

    @Override
    public <R> R accept(MemberDeclarationVisitor<R> visitor) {
        return visitor.visitMethodDeclaration(this);
    }

    @Override
    public String getName() {
        return name;
    }

    public TypeParameterList getTypeParameters() {
        return typeParameters.get();
    }

    public TemplateTypeConstructor getSignature() {
        return signatureConstructor.get();
    }

    @Override
    public String debug() {
        // TODO add return type
        return pieces.stream().map(Debuggable::debug).collect(Collectors.joining(" "));
    }

    @Data
    public static class Piece implements ModelNode, MethodDeclarationPieceAccessors {
        final String name;
        final ImmutableList<Parameter> parameters;

        public Piece(
                Ancestry ancestry,
                String name,
                ImmutableList<Constructor<Parameter>> parameters) {

            Ancestry ancestryIncludingMe = ancestry.append(this);

            this.name = name;
            this.parameters = ancestryIncludingMe.constructAll(parameters);
        }

        @Override
        public String debug() {
            return name + "(" + parameters.stream().map(Debuggable::debug).collect(Collectors.joining(", ")) + ")";
        }
    }
}
