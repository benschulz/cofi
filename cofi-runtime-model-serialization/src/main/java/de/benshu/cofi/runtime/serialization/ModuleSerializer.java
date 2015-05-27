package de.benshu.cofi.runtime.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.runtime.Module;
import de.benshu.cofi.types.Type;
import de.benshu.cofi.types.TypeList;
import de.benshu.cofi.types.TypeParameterList;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.commons.core.Optional;

import java.lang.reflect.ParameterizedType;
import java.util.function.Supplier;

import static de.benshu.commons.core.streams.Collectors.list;

public class ModuleSerializer {
    public void serialize(Module module, Appendable appendable) {
        final Gson gson = new GsonBuilder().setPrettyPrinting()
                .registerTypeAdapter(Fqn.class, (JsonSerializer<Fqn>) this::serializeFqn)
                .registerTypeAdapter(Supplier.class, (JsonSerializer<Supplier<?>>) this::serializeSupplier)
                .registerTypeHierarchyAdapter(Type.class, (JsonSerializer<Type>) this::serializeType)
                .registerTypeAdapter(TypeList.class, (JsonSerializer<TypeList<?>>) this::serializeTypeList)
                .registerTypeAdapter(TypeParameterList.class, (JsonSerializer<TypeParameterList>) this::serializeTypeParameterList)
                .registerTypeAdapter(Optional.class, (JsonSerializer<Optional<?>>) this::serializeOptional)
                .create();

        gson.toJson(module, appendable);
    }

    private JsonElement serializeFqn(Fqn fqn, java.lang.reflect.Type type, JsonSerializationContext context) {
        return context.serialize(fqn.toCanonicalString());
    }

    private JsonElement serializeSupplier(Supplier<?> supplier, java.lang.reflect.Type t, JsonSerializationContext context) {
        final java.lang.reflect.Type suppliedType = ((ParameterizedType) t).getActualTypeArguments()[0];
        return context.serialize(supplier.get(), suppliedType);
    }

    private JsonElement serializeType(Type type, java.lang.reflect.Type t, JsonSerializationContext context) {
        return context.serialize( TypeMixin.rebind(type).toDescriptor());
    }

    private JsonElement serializeTypeList(TypeList<?> types, java.lang.reflect.Type t, JsonSerializationContext context) {
        return context.serialize(types.stream().collect(list()));
    }

    private JsonElement serializeTypeParameterList(TypeParameterList typeParameters, java.lang.reflect.Type type, JsonSerializationContext context) {
        return context.serialize(TypeParameterListImpl.rebind(typeParameters).toDescriptor());
    }

    private JsonElement serializeOptional(Optional<?> optional, java.lang.reflect.Type t, JsonSerializationContext context) {
        for (Object o : optional)
            return context.serialize(o);
        return context.serialize(null);
    }

}
