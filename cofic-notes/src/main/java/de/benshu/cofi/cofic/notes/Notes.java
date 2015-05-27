package de.benshu.cofi.cofic.notes;

import java.io.Closeable;

public interface Notes {
	Closeable attach(Source.Snippet source, Note note);
}
