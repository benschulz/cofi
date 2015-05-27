package de.benshu.cofi.types;

import de.benshu.cofi.types.tags.Tagged;
import de.benshu.commons.core.Debuggable;

public interface Type extends Tagged, Debuggable {
    Kind getKind();
}
