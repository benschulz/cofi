package de.benshu.cofi.runtime;

import de.benshu.cofi.types.TemplateType;
import de.benshu.cofi.types.TypeList;

public interface Singleton extends Multiton {
    default TemplateType getProperType() {
        return getType().apply(TypeList.empty());
    }
}
