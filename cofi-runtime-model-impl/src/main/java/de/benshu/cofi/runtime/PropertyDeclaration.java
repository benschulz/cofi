package de.benshu.cofi.runtime;

import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.binary.internal.Ancestry;
import de.benshu.cofi.binary.internal.Constructor;
import de.benshu.cofi.binary.internal.MemoizingSupplier;
import de.benshu.cofi.runtime.internal.TypeParameterListReference;
import de.benshu.cofi.runtime.internal.TypeReference;
import de.benshu.cofi.types.ProperType;
import de.benshu.cofi.types.ProperTypeConstructor;
import de.benshu.cofi.types.TemplateTypeConstructor;
import de.benshu.cofi.types.TypeParameterList;
import de.benshu.commons.core.Optional;
import de.benshu.jswizzle.data.Data;

import java.util.function.Supplier;

import static de.benshu.cofi.runtime.internal.Resolution.resolve;
import static de.benshu.commons.core.streams.Collectors.set;

@Data
public class PropertyDeclaration extends TypeBody.Containable implements MemberDeclaration, VariableDeclaration, PropertyDeclarationAccessors {
    final ImmutableSet<Annotation> annotations;
    final String name;
    @Data.Exclude
    final transient Supplier<TypeParameterList> typeParameters;
    @Data.Exclude
    final transient Supplier<ProperTypeConstructor<?>> type;
    @Data.Exclude
    final Supplier<ImmutableSet<TemplateTypeConstructor>> traits;
    @Data.Exclude
    final Supplier<ProperType> valueType;
    final Optional<Expression> initialValue;

    public PropertyDeclaration(
            Ancestry ancestry,
            ImmutableSet<Constructor<Annotation>> annotations,
            String name,
            TypeParameterListReference typeParameters,
            ImmutableSet<TypeReference<TemplateTypeConstructor>> traits,
            TypeReference<ProperTypeConstructor<?>> type,
            TypeReference<ProperType> valueType,
            Optional<Constructor<Expression>> initialValue) {

        final Ancestry ancestryIncludingMe = ancestry.append(this);

        this.annotations = ancestryIncludingMe.constructAll(annotations);
        this.name = name;
        this.type = resolve(ancestryIncludingMe, type);
        this.traits = MemoizingSupplier.of(() -> traits.stream()
                .map(t -> resolve(ancestryIncludingMe, t).get())
                .collect(set()));
        this.valueType = resolve(ancestryIncludingMe, valueType);
        this.initialValue = initialValue.map(ancestryIncludingMe::construct);
        this.typeParameters = resolve(ancestryIncludingMe, typeParameters);
    }

    @Override
    public <R> R accept(MemberDeclarationVisitor<R> visitor) {
        return visitor.visitPropertyDeclaration(this);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public TypeParameterList getTypeParameters() {
        return typeParameters.get();
    }

    public ProperTypeConstructor<?> getType() {
        return type.get();
    }

    public ProperType getValueType() {
        return valueType.get();
    }

    public ImmutableSet<TemplateTypeConstructor> getTraits() {
        return traits.get();
    }

    @Override
    public String debug() {
        return name + " : " + getValueType().debug();
    }
}
