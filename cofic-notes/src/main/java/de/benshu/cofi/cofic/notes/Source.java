package de.benshu.cofi.cofic.notes;

public interface Source {
	interface Snippet {
		int getBeginColumn();
		
		int getBeginLine();
		
		int getEndColumn();
		
		int getEndLine();
	}
}
