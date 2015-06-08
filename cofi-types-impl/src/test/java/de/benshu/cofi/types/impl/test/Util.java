package de.benshu.cofi.types.impl.test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.declarations.source.SourceMemberDescriptor;
import de.benshu.cofi.types.impl.declarations.source.SourceMethodDescriptor;
import de.benshu.cofi.types.impl.declarations.source.SourceMethodSignatureDescriptor;
import de.benshu.cofi.types.impl.declarations.source.SourceType;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.commons.core.streams.Collectors;
import de.benshu.cofi.cofic.notes.Source;

import static java.util.stream.Collectors.joining;

// TODO eliminate
public class Util {
    public static String flattenFqn(String... fqn) {
        return "." + ImmutableList.copyOf(fqn).stream().collect(joining("."));
    }

    @SafeVarargs
    public static AbstractTypeList<TestContext, ?> types(TypeMixin<TestContext, ?>... types) {
        return AbstractTypeList.of(ImmutableList.copyOf(types));
    }

    @SafeVarargs
    public static ImmutableSet<SourceMemberDescriptor<TestContext>> mdm(SourceMemberDescriptor<TestContext>... mds) {
        return ImmutableSet.copyOf(mds);
    }

    public static SourceMethodDescriptor<TestContext> md(String name, SourceMethodSignatureDescriptor<TestContext>... sigDescs) {
        return new MethodDesc(name, ImmutableList.copyOf(sigDescs));
    }

    public static MethodSignatureDesc msd(TypeParameterListImpl<TestContext> typeParams, ImmutableList<AbstractTypeList<TestContext, ?>> paramTypes, TypeMixin<TestContext, ?> returnType) {
        return new MethodSignatureDesc(typeParams, paramTypes.stream().map(Util::source).collect(Collectors.list()), source(returnType));
    }

    public static MethodSignatureDesc msd(TypeParameterListImpl<TestContext> typeParams, AbstractTypeList<TestContext, ?> paramTypes, TypeMixin<TestContext, ?> returnType) {
        return msd(typeParams, ImmutableList.of(paramTypes), returnType);
    }

    public static MethodSignatureDesc msd(AbstractTypeList<TestContext, ?> paramTypes, TypeMixin<TestContext, ?> returnType) {
        return msd(TypeParameterListImpl.empty(), paramTypes, returnType);
    }

    public static ImmutableList<SourceType<TestContext>> source(AbstractTypeList<TestContext, ?> unsourced) {
        return unsourced.stream().map(Util::source).collect(Collectors.list());
    }

    private static SourceType<TestContext> source(TypeMixin<TestContext, ?> type) {
        return SourceType.of(type, new Source.Snippet() {
            @Override
            public int getBeginColumn() {
                return -1;
            }

            @Override
            public int getBeginLine() {
                return -1;
            }

            @Override
            public int getEndColumn() {
                return -1;
            }

            @Override
            public int getEndLine() {
                return -1;
            }
        });
    }
}
