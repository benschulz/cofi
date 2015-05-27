package de.benshu.cofi.parser.lexer;

import com.google.common.collect.ImmutableSet;

import java.util.EnumSet;

public interface Token extends TokenString {
	enum Kind {
		// specific kinds 
		ABSTRACT,
		ANNOTATION,
		CALLBACK,
		CHARACTER_LITERAL,
		CLASS,
		ENUM,
		EOF,
		EXPLICIT,
		EXTENDABLE,
		FALSE,
		FINAL,
		IDENTIFIER,
		IMPLICIT,
		INTERSECTION,
		MODULE,
		NIL,
		NUMERICAL_LITERAL,
		OBJECT,
		OVERRIDABLE,
		PACKAGE,
		PRIVATE,
		PUBLIC,
		SEALED,
		STRING_LITERAL,
		SUPER,
		SYMBOL,
		THIS,
		TRAIT,
		TRUE,
		UNION,
		WHITESPACE,
		
		// general kinds
		ANY,
		LITERAL,
		MODIFIER,
		SKIPPABLE;
		
		static {
			for (Kind kind : values()) {
				kind.kinds = ImmutableSet.of(kind);
			}
			
			ANY.kinds = ImmutableSet.copyOf(EnumSet.allOf(Token.Kind.class));
			LITERAL.kinds = ImmutableSet.of(NUMERICAL_LITERAL, CHARACTER_LITERAL, FALSE, NIL, STRING_LITERAL, TRUE);
			MODIFIER.kinds = ImmutableSet.of(ABSTRACT, EXPLICIT, FINAL, IMPLICIT, MODULE, PACKAGE, PRIVATE, PUBLIC, SEALED);
			SKIPPABLE.kinds = ImmutableSet.of(WHITESPACE);
		}
		
		private ImmutableSet<Kind> kinds = null;
		
		public ImmutableSet<Kind> getSpecificKinds() {
			return kinds;
		}
		
		public boolean isGeneric() {
			return !kinds.equals(ImmutableSet.of(this));
		}
	}
	
	/**
	 * 
	 * @return the kind of this token
	 */
	Kind getKind();
	
	/**
	 * 
	 * @param successor
	 * @return a token string spanning from this to the successor token
	 */
	TokenString getTokenString(Token successor);
	
	/**
	 * @param kind
	 * @return true iff this is of kind <code>kind</code>
	 */
	boolean isA(Kind kind);
}