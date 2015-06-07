package de.benshu.cofi.model;

public interface Modifier<X> extends Annotation<X> {
	public enum Kind {
		ABSTRACT,
		EXPLICIT,
		FINAL,
		IMPLICIT,
		MODULE,
		PACKAGE,
		PRIVATE,
		PUBLIC,
		SEALED;
	}
	
	Kind getKind();
}
