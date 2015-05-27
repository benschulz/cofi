package de.benshu.cofi.parser;

class AmbiguityException extends RuntimeException {
	private static final long serialVersionUID = -2652764214178131413L;
	
	final Chart.Entry a;
	final Chart.Entry b;
	
	public AmbiguityException(Chart.Entry a, Chart.Entry b) {
		this.a = a;
		this.b = b;
	}
}
