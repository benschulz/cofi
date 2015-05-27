package de.benshu.cofi.types;

public interface Kind {
    /**
     * @return true if this is '(*, ...) -> ... -> (*, ...) -> *' (the kind of a type constructor that yields a type constructor)
     */
	boolean isHigherOrder();

    /**
     * @return true iff this is '(K*) -> *' (the kind of a type constructor that yields a proper type)
     */
	boolean isFirstOrder();

    /**
     * @return true iff this is '*' (the kind of a proper type)
     */
    boolean isProperOrder();
}
