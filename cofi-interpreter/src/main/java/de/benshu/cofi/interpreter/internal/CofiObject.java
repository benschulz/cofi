package de.benshu.cofi.interpreter.internal;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MutableClassToInstanceMap;
import de.benshu.cofi.runtime.VariableDeclaration;
import de.benshu.cofi.types.TemplateType;

public class CofiObject {
    private final TemplateType type;

    private final ClassToInstanceMap<Object> specialValues = MutableClassToInstanceMap.create();

    private final ImmutableMap<VariableDeclaration, CofiObject> properties;

    public CofiObject(TemplateType type, ImmutableMap<VariableDeclaration, CofiObject> properties) {
        this.type = type;
        this.properties = properties;
    }

    public CofiObject(TemplateType type) {
        this(type, ImmutableMap.of());
    }

    @Override
    public String toString() {
        return type.debug();
    }

    public TemplateType getType() {
        return type;
    }

    public ClassToInstanceMap<Object> getSpecialValues() {
        return specialValues;
    }

    public ImmutableMap<VariableDeclaration, CofiObject> getProperties() {
        return properties;
    }
}
