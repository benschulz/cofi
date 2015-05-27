package de.benshu.cofi.cofic.notes;

import java.io.Closeable;
import java.io.PrintStream;

public class PrintStreamNotes implements Notes {
	public static PrintStreamNotes create(PrintStream printStream) {
		return new PrintStreamNotes(printStream);
	}
	
	public static PrintStreamNotes err() {
		return create(System.err);
	}
	
	private final PrintStream printStream;
	
	private PrintStreamNotes(PrintStream printStream) {
		this.printStream = printStream;
	}
	
	@Override
	public Closeable attach(Source.Snippet source, Note note) {
		printStream.println(note.getKind() + ": " + note + " at " + source.getBeginLine() + ":" + source.getBeginColumn());
		return null;
	}
}
