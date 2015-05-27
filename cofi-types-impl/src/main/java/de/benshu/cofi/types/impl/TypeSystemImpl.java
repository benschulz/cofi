package de.benshu.cofi.types.impl;

import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import de.benshu.cofi.types.bound.TypeSystem;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.templates.AbstractTemplateTypeConstructor;
import de.benshu.cofi.types.impl.templates.TemplateTypeConstructorMixin;
import de.benshu.cofi.types.impl.templates.TemplateTypeImpl;
import de.benshu.cofi.types.tags.DefaultingTag;
import de.benshu.commons.core.Debuggable;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class TypeSystemImpl<X extends TypeSystemContext<X>> implements TypeSystem<X> {
    public static <X extends TypeSystemContext<X>> TypeSystemImpl<X> create(Function<String, ? extends TypeMixin<X, ?>> computer, DefaultingTag<? extends Debuggable> nameTag,
                                                                            Supplier<TemplateTypeConstructorMixin<X>> topConstructorSupplier) {
        return new TypeSystemImpl<>(computer, nameTag, topConstructorSupplier);
    }

    private final LoadingCache<String, ? extends TypeMixin<X, ?>> lookup;
    private final DefaultingTag<? extends Debuggable> nameTag;
    private final Bottom<X> bottom;
    private final Supplier<TemplateTypeConstructorMixin<X>> topConstructor;
    private final AtomicReference<TemplateTypeImpl<X>> top = new AtomicReference<>();

    private TypeSystemImpl(Function<String, ? extends TypeMixin<X, ?>> computer, DefaultingTag<? extends Debuggable> nameTag, Supplier<TemplateTypeConstructorMixin<X>> topConstructorSupplier) {
        this.lookup = CacheBuilder.newBuilder().build(CacheLoader.from(computer));
        this.nameTag = nameTag;
        this.bottom = Bottom.create();
        this.topConstructor = topConstructorSupplier;
    }

    public TemplateTypeImpl<X> constructFunction(AbstractTypeList<X, ?> parameterTypes, TypeMixin<X, ?> returnType) {
        AbstractTypeList<X, ?> arguments = AbstractTypeList.of(ImmutableList.copyOf(Iterables.concat(parameterTypes, ImmutableList.of(returnType))));
        return getFunction(parameterTypes.size()).apply(arguments);
    }

    public TypeMixin<X, ?> constructTuple(AbstractTypeList<X, ?> componentTypes) {
        if (componentTypes.size() == 1)
            return componentTypes.get(0);
        else
            return getTuple(componentTypes.size()).apply(componentTypes);
    }

    @Override
    public Bottom<X> getBottom() {
        return bottom;
    }

    @Override
    public AbstractTemplateTypeConstructor<X> getFunction() {
        return lookUpUnchecked("Function");
    }

    public AbstractTemplateTypeConstructor<X> getFunction(int arity) {
        if (arity < 0)
            throw new IllegalArgumentException(String.valueOf(arity));
        return lookUpUnchecked("Function" + arity);
    }

    @Override
    public AbstractTemplateTypeConstructor<X> getMetaType() {
        return lookUpUnchecked("Type");
    }

    public DefaultingTag<? extends Debuggable> getNameTag() {
        return nameTag;
    }

    public AbstractTemplateTypeConstructor<X> getPackage() {
        return lookUpUnchecked("Package");
    }

    @Override
    public TemplateTypeImpl<X> getTop() {
        TemplateTypeImpl<X> t = top.get();
        if (t != null)
            return t;

        AbstractTypeList<X, ?> arguments = AbstractTypeList.empty();
        t = topConstructor.get().apply(arguments);
        return top.compareAndSet(null, t) ? t : top.get();
    }

    @Override
    public AbstractTemplateTypeConstructor<X> getTuple() {
        return lookUpUnchecked("Tuple");
    }

    @Override
    public AbstractTemplateTypeConstructor<X> getTuple(int arity) {
        if (arity == 0) {
            return getUnitConstructor();
        } else if (arity <= 1) {
            throw new IllegalArgumentException(String.valueOf(arity));
        }

        return lookUpUnchecked("Tuple" + arity);
    }

    @Override
    public TemplateTypeImpl<X> getUnit() {
        AbstractTypeList<X, ?> arguments = AbstractTypeList.empty();
        return getUnitConstructor().apply(arguments);
    }

    public AbstractTemplateTypeConstructor<X> getUnitConstructor() {
        return lookUpUnchecked("Unit");
    }

    @Override
    public ProperTypeConstructorMixin<X, ?, ?> lookUp(String name) {
        return lookUpUnchecked(name);
    }

    @SuppressWarnings("unchecked")
    private <T extends ProperTypeConstructorMixin<X, ?, ?>> T lookUpUnchecked(String name) {
        return (T) lookup.getUnchecked(name);
    }
}
