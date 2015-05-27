package de.benshu.cofi.parser;

import com.google.common.base.Preconditions;

public interface Factory {
	Factory FST = args -> {
        Preconditions.checkArgument(args.length == 1);

        return args[0];
    };
	
	Object create(Object... args);
}
