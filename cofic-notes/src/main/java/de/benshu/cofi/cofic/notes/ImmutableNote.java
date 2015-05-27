package de.benshu.cofi.cofic.notes;

public class ImmutableNote implements Note {
	public static ImmutableNote create(Kind kind, String text) {
		return new ImmutableNote(kind, text);
	}
	
	private final Kind kind;
	private final String text;
	
	private ImmutableNote(Kind kind, String text) {
		this.kind = kind;
		this.text = text;
	}
	
	@Override
	public Kind getKind() {
		return kind;
	}
	
	@Override
	public String toString() {
		return text;
	}
}
