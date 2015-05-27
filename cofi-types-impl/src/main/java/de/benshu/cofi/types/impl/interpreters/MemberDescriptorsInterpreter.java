package de.benshu.cofi.types.impl.interpreters;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.cofic.notes.CofiNote;
import de.benshu.cofi.cofic.notes.ImmutableNote;
import de.benshu.cofi.cofic.notes.async.Checker;
import de.benshu.cofi.types.MemberSort;
import de.benshu.cofi.types.impl.templates.AbstractTemplateTypeConstructor;
import de.benshu.cofi.types.impl.Error;
import de.benshu.cofi.types.impl.ErrorConstructor;
import de.benshu.cofi.types.impl.ProperTypeConstructorMixin;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.declarations.InterpretedMemberDescriptor;
import de.benshu.cofi.types.impl.declarations.InterpretedMemberDescriptors;
import de.benshu.cofi.types.impl.declarations.InterpretedMethodDescriptor;
import de.benshu.cofi.types.impl.declarations.InterpretedMethodSignatureDescriptor;
import de.benshu.cofi.types.impl.declarations.InterpretedPropertyDescriptor;
import de.benshu.cofi.types.impl.declarations.InterpretedTypeDescriptor;
import de.benshu.cofi.types.impl.declarations.Interpreter;
import de.benshu.cofi.types.impl.declarations.SourceMemberDescriptor;
import de.benshu.cofi.types.impl.declarations.SourceMemberDescriptors;
import de.benshu.cofi.types.impl.declarations.SourceMethodDescriptor;
import de.benshu.cofi.types.impl.declarations.SourceMethodSignatureDescriptor;
import de.benshu.cofi.types.impl.declarations.SourcePropertyDescriptor;
import de.benshu.cofi.types.impl.declarations.SourceType;
import de.benshu.cofi.types.impl.declarations.SourceTypeDescriptor;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.commons.core.Optional;
import de.benshu.commons.core.streams.Collectors;

import java.util.stream.Stream;

import static com.google.common.collect.Maps.immutableEntry;
import static de.benshu.cofi.types.impl.lists.AbstractTypeList.typeList;
import static de.benshu.commons.core.streams.Collectors.map;
import static de.benshu.commons.core.streams.Collectors.set;

public class MemberDescriptorsInterpreter<X extends TypeSystemContext<X>> implements Interpreter<SourceMemberDescriptors<X>, InterpretedMemberDescriptors<X>> {
    public static <X extends TypeSystemContext<X>> MemberDescriptorsInterpreter<X> create(X context) {
        return new MemberDescriptorsInterpreter<>(context);
    }

    private final X context;

    public MemberDescriptorsInterpreter(X context) {
        this.context = context;
    }

    @Override
    public InterpretedMemberDescriptors<X> interpret(SourceMemberDescriptors<X> input, Checker checker) {
        return new InterpretedMemberDescriptors<>(
                input.getContextualConstraints(),
                input.getDescriptors().stream().map(this::interpretSingleDescriptor).collect(set())
        );
    }

    private InterpretedMemberDescriptor<X> interpretSingleDescriptor(SourceMemberDescriptor<X> memberDescriptor) {
        switch (memberDescriptor.getSort()) {
            case METHOD:
                return interpretMethodDescriptor((SourceMethodDescriptor<X>) memberDescriptor);
            case PROPERTY:
                return interpretPropertyDescriptor((SourcePropertyDescriptor<X>) memberDescriptor);
            case TYPE:
                return interpretTypeDescriptor((SourceTypeDescriptor<X>) memberDescriptor);
            default:
                throw new AssertionError();
        }
    }

    private InterpretedMemberDescriptor<X> interpretMethodDescriptor(SourceMethodDescriptor<X> memberDescriptor) {
        final String name = memberDescriptor.getName();
        final ImmutableList<InterpretedMethodSignatureDescriptor<X>> signatureDescriptors = memberDescriptor.getMethodSignatureDescriptors().stream().map(this::interpretMethodSignatureDescriptor).collect(Collectors.list());
        final IndividualTags tags = memberDescriptor.getTags(context);

        return new InterpretedMethodDescriptor<X>() {
            @Override
            public ImmutableList<InterpretedMethodSignatureDescriptor<X>> getMethodSignatureDescriptors() {
                return signatureDescriptors;
            }

            @Override
            public MemberSort getSort() {
                return MemberSort.METHOD;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public IndividualTags getTags(X context) {
                return tags;
            }
        };
    }

    private InterpretedMethodSignatureDescriptor<X> interpretMethodSignatureDescriptor(SourceMethodSignatureDescriptor<X> methodSignatureDescriptor) {
        final TypeParameterListImpl<X> typeParameters = methodSignatureDescriptor.getTypeParameters();
        final ImmutableList<AbstractTypeList<X, ProperTypeMixin<X, ?>>> parameterTypes = methodSignatureDescriptor.getParameterTypes().stream()
                .map(p -> p.stream().map(this::interpretTypeAsProper).collect(typeList())).collect(Collectors.list());
        final ProperTypeMixin<X, ?> returnType = interpretTypeAsProper(methodSignatureDescriptor.getReturnType());
        final IndividualTags tags = methodSignatureDescriptor.getTags();

        return new InterpretedMethodSignatureDescriptor<X>() {
            @Override
            public TypeParameterListImpl<X> getTypeParameters() {
                return typeParameters;
            }

            @Override
            public ImmutableList<AbstractTypeList<X, ProperTypeMixin<X, ?>>> getParameterTypes() {
                return parameterTypes;
            }

            @Override
            public ProperTypeMixin<X, ?> getReturnType() {
                return returnType;
            }

            @Override
            public IndividualTags getTags() {
                return tags;
            }
        };
    }

    private InterpretedPropertyDescriptor<X> interpretPropertyDescriptor(SourcePropertyDescriptor<X> propertyDescriptor) {
        final ProperTypeMixin<X, ?> type = interpretTypeAsProper(propertyDescriptor.getType(context));
        final AbstractTypeList<X, AbstractTemplateTypeConstructor<X>> traits = propertyDescriptor.getTraits().stream()
                .flatMap(this::interpretTypeAsTemplateTypeConstructor).collect(typeList());
        final String name = propertyDescriptor.getName();
        final IndividualTags tags = propertyDescriptor.getTags(context);

        return new InterpretedPropertyDescriptor<X>() {
            @Override
            public ProperTypeMixin<X, ?> getType() {
                return type;
            }

            @Override
            public AbstractTypeList<X, AbstractTemplateTypeConstructor<X>> getTraits() {
                return traits;
            }

            @Override
            public MemberSort getSort() {
                return MemberSort.PROPERTY;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public IndividualTags getTags(X context) {
                return tags;
            }
        };
    }

    private InterpretedTypeDescriptor<X> interpretTypeDescriptor(SourceTypeDescriptor<X> typeDescriptor) {
        final ProperTypeConstructorMixin<X, ?, ?> type = interpretTypeAsProperTypeConstructor(typeDescriptor.getType(context));
        final String name = typeDescriptor.getName();
        final IndividualTags tags = typeDescriptor.getTags(context);

        return new InterpretedTypeDescriptor<X>() {
            @Override
            public ProperTypeConstructorMixin<X, ?, ?> getType(X context) {
                return type;
            }

            @Override
            public MemberSort getSort() {
                return MemberSort.TYPE;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public IndividualTags getTags(X context) {
                return tags;
            }
        };
    }

    private ProperTypeMixin<X, ?> interpretTypeAsProper(SourceType<X> sourceType) {
        context.getChecker().submit(() -> Optional.some(sourceType)
                .filter(st -> !(st.getType() instanceof ProperTypeMixin<?, ?>))
                .stream()
                .map(st -> immutableEntry(st.getSource(), ImmutableNote.create(CofiNote.INVALID_TYPE_KIND, "Proper type expected.")))
                .collect(map()));

        return sourceType.getType() instanceof ProperTypeMixin<?, ?>
                ? (ProperTypeMixin<X, ?>) sourceType.getType()
                : Error.create();
    }

    private Stream<AbstractTemplateTypeConstructor<X>> interpretTypeAsTemplateTypeConstructor(SourceType<X> sourceType) {
        context.getChecker().submit(() -> Optional.some(sourceType)
                .filter(st -> !(st.getType() instanceof AbstractTemplateTypeConstructor<?>))
                .stream()
                .map(st -> immutableEntry(st.getSource(), ImmutableNote.create(CofiNote.INVALID_TYPE_KIND, "Template type constructor expected.")))
                .collect(map()));

        return sourceType.getType() instanceof AbstractTemplateTypeConstructor<?>
                ? Stream.of((AbstractTemplateTypeConstructor<X>) sourceType.getType())
                : Stream.of();
    }

    private ProperTypeConstructorMixin<X, ?, ?> interpretTypeAsProperTypeConstructor(SourceType<X> sourceType) {
        context.getChecker().submit(() -> Optional.some(sourceType)
                .filter(st -> !(st.getType() instanceof ProperTypeConstructorMixin<?, ?, ?>))
                .stream()
                .map(st -> immutableEntry(st.getSource(), ImmutableNote.create(CofiNote.INVALID_TYPE_KIND, "Proper type constructor expected.")))
                .collect(map()));

        return sourceType.getType().getKind().isFirstOrder()
                ? (ProperTypeConstructorMixin<X, ?, ?>) sourceType.getType()
                : ErrorConstructor.create();
    }

}
