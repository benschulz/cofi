package de.benshu.cofi.parser.lexer.impl;

import org.testng.annotations.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.testng.Assert.*;

public class JavaSourceReaderTest {
	private static JavaSourceReader make(final String source) throws IOException {
		return new JavaSourceReader(new StringReader(source));
	}
	
	private static JavaSourceReader make(String source, boolean singleU) throws IOException {
		return new JavaSourceReader(new StringReader(source), singleU);
	}
	
	@Test
	public void backtrackAfterEndOfFile() throws IOException {
		final JavaSourceReader jsr = make("a");
		
		assertEquals('a', jsr.read());
		jsr.backtrack();
		assertEquals('a', jsr.read());
		
		assertEquals(-1, jsr.read());
		jsr.backtrack();
		assertEquals(-1, jsr.read());
		
		assertEquals(-1, jsr.read());
		jsr.backtrack();
		assertEquals(-1, jsr.read());
	}
	
	@Test
	public void backtrackAndlookAhead() throws IOException {
		final JavaSourceReader jsr = make("a");
		
		jsr.read();
		jsr.backtrack();
		
		assertEquals('a', jsr.lookAhead());
	}
	
	@Test
	public void backtrackAndRead() throws IOException {
		final JavaSourceReader jsr = make("a");
		
		jsr.read();
		jsr.backtrack();
		
		assertEquals('a', jsr.read());
	}
	
	@Test
	public void backtrackOnEmptyFile() throws IOException {
		final JavaSourceReader jsr = make("");
		
		assertEquals(-1, jsr.read());
		jsr.backtrack();
		assertEquals(-1, jsr.read());
		
		assertEquals(-1, jsr.read());
		jsr.backtrack();
		assertEquals(-1, jsr.read());
	}
	
	@Test
	public void carriageReturnBecomesLineFeed() throws IOException {
		final JavaSourceReader jsr = make("\r");
		
		assertEquals('\n', jsr.read());
		assertTrue(jsr.read() < 0);
	}
	
	@Test
	public void carriageReturnCaretPosition() throws IOException {
		final JavaSourceReader jsr = make("\r");
		
		jsr.read();
		
		assertEquals(2, jsr.getLine());
		assertEquals(1, jsr.getColumn());
	}
	
	@Test
	public void carriageReturnPlusLineFeedBecomesLineFeed() throws IOException {
		final JavaSourceReader jsr = make("\r\n");
		
		assertEquals('\n', jsr.read());
		assertTrue(jsr.read() < 0);
	}
	
	@Test
	public void carriageReturnPlusLineFeedCaretPosition() throws IOException {
		final JavaSourceReader jsr = make("\r\n");
		
		jsr.read();
		
		assertEquals(2, jsr.getLine());
		assertEquals(1, jsr.getColumn());
	}
	
	@Test
	public void carriageReturnPlusUnicodeLineFeedCaretPosition() throws IOException {
		final JavaSourceReader jsr = make("\r\\uuuuuu000A");
		
		jsr.read();
		
		assertEquals(2, jsr.getLine());
		assertEquals(12, jsr.getColumn());
	}
	
	@Test
	public void close() throws IOException {
		final boolean[] closed = {
			false
		};
		
		final JavaSourceReader jsr = new JavaSourceReader(new StringReader("insignificant") {
			@Override
			public void close() {
				closed[0] = true;
			}
		});
		
		assertFalse(closed[0]);
		jsr.close();
		assertTrue(closed[0]);
	}
	
	@Test
	public void columnPastEndOfFile() throws IOException {
		final JavaSourceReader jsr = make("a");
		
		assertEquals('a', jsr.read());
		assertEquals(1, jsr.getLine());
		assertEquals(2, jsr.getColumn());
		
		assertEquals(-1, jsr.read());
		assertEquals(1, jsr.getLine());
		assertEquals(2, jsr.getColumn());
		
		assertEquals(-1, jsr.read());
		assertEquals(1, jsr.getLine());
		assertEquals(2, jsr.getColumn());
	}
	
	@Test(expectedExceptions = IllegalStateException.class)
	public void doubleBacktracks() throws IOException {
		final JavaSourceReader jsr = make("abc");
		
		jsr.read();
		jsr.backtrack();
		jsr.backtrack();
	}
	
	@Test
	public void expectantLookAheadFailure() throws IOException {
		final JavaSourceReader jsr = make("ab");
		
		assertFalse(jsr.lookAhead('b'));
		assertEquals('a', jsr.lookAhead());
	}
	
	@Test
	public void expectantLookAheadSuccess() throws IOException {
		final JavaSourceReader jsr = make("ab");
		
		assertTrue(jsr.lookAhead('a'));
		assertEquals('b', jsr.lookAhead());
	}
	
	@Test(expectedExceptions = IOException.class)
	public void expectedHexDigit() throws IOException {
		make("\\u000X");
	}
	
	@Test
	public void ignoreEvenBackslashUnicodeEscapes() throws IOException {
		final JavaSourceReader jsr = make("\\\\u0061");
		
		assertEquals('\\', jsr.read());
		assertEquals('\\', jsr.read());
		assertEquals('u', jsr.read());
		assertEquals('0', jsr.read());
		assertEquals('0', jsr.read());
		assertEquals('6', jsr.read());
		assertEquals('1', jsr.read());
		assertTrue(jsr.read() < 0);
	}
	
	@Test(expectedExceptions = IOException.class)
	public void illegalUnicodeEscape1() throws IOException {
		make("\\uX234");
	}
	
	@Test(expectedExceptions = IOException.class)
	public void illegalUnicodeEscape2() throws IOException {
		make("\\u1X34");
	}
	
	@Test(expectedExceptions = IOException.class)
	public void illegalUnicodeEscape3() throws IOException {
		make("\\u12X4");
	}
	
	@Test(expectedExceptions = IOException.class)
	public void illegalUnicodeEscape4() throws IOException {
		make("\\u123X");
	}
	
	@Test
	public void initialState() throws IOException {
		final JavaSourceReader jsr = make("insignificant");
		
		assertEquals(1, jsr.getLine());
		assertEquals(1, jsr.getColumn());
	}
	
	@Test
	public void lineFeedCaretPosition() throws IOException {
		final JavaSourceReader jsr = make("\n");
		
		jsr.read();
		
		assertEquals(2, jsr.getLine());
		assertEquals(1, jsr.getColumn());
	}
	
	@Test
	public void lineFeedIsLineFeed() throws IOException {
		final JavaSourceReader jsr = make("\n");
		
		assertEquals('\n', jsr.read());
		assertTrue(jsr.read() < 0);
	}
	
	@Test
	public void lookAhead() throws IOException {
		final JavaSourceReader jsr = make("ab");
		
		assertEquals('a', jsr.lookAhead());
		
		jsr.read();
		
		assertEquals('b', jsr.lookAhead());
	}
	
	@Test
	public void multiBacktrack() throws IOException {
		final JavaSourceReader jsr = make("ab");
		
		jsr.read();
		jsr.backtrack();
		
		assertEquals('a', jsr.lookAhead());
		assertEquals('a', jsr.read());
		
		jsr.backtrack();
		
		assertEquals('a', jsr.lookAhead());
		assertEquals('a', jsr.read());
		
		assertEquals('b', jsr.read());
		assertTrue(jsr.read() < 0);
	}
	
	@Test
	public void multiExpectantLookAheadFailure() throws IOException {
		final JavaSourceReader jsr = make("ab");
		
		assertFalse(jsr.lookAhead('b', 'o', 'w') >= 0);
		assertEquals('a', jsr.lookAhead());
	}
	
	@Test
	public void multiExpectantLookAheadSuccess() throws IOException {
		final JavaSourceReader jsr = make("ab");
		
		assertEquals('a', jsr.lookAhead('c', 'a', 't'));
		assertEquals('b', jsr.lookAhead());
	}
	
	@SuppressWarnings("resource")
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void nullReaderOneArgConstructor() throws IOException {
		new JavaSourceReader(null);
	}
	
	@SuppressWarnings("resource")
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void nullReaderTwoArgConstructor() throws IOException {
		new JavaSourceReader(null, true);
	}
	
	@Test
	public void readAfterEndOfFile() throws IOException {
		final JavaSourceReader jsr = make("");
		
		final char[] buffer = new char[1];
		
		assertEquals(-1, jsr.read(buffer, 0, 1));
		assertEquals(0, buffer[0]);
	}
	
	@Test
	public void readBeyondEndOfFile() throws IOException {
		final JavaSourceReader jsr = make("abc");
		
		final char[] buffer = new char[5];
		
		assertEquals(3, jsr.read(buffer, 0, 5));
		
		assertEquals('a', buffer[0]);
		assertEquals('b', buffer[1]);
		assertEquals('c', buffer[2]);
		assertEquals(0, buffer[3]);
		assertEquals(0, buffer[4]);
	}
	
	@Test
	public void readMultiple() throws IOException {
		final JavaSourceReader jsr = make("abc");
		
		final char[] buffer = new char[5];
		
		assertEquals(3, jsr.read(buffer, 1, 3));
		
		assertEquals(0, buffer[0]);
		assertEquals('a', buffer[1]);
		assertEquals('b', buffer[2]);
		assertEquals('c', buffer[3]);
		assertEquals(0, buffer[4]);
	}
	
	@Test
	public void readSingle() throws IOException {
		final JavaSourceReader jsr = make("abc");
		
		final char[] buffer = new char[3];
		assertEquals(1, jsr.read(buffer, 1, 1));
		
		assertEquals(0, buffer[0]);
		assertEquals('a', buffer[1]);
		assertEquals(0, buffer[2]);
	}
	
	@Test(expectedExceptions = IndexOutOfBoundsException.class)
	public void readWithNegativeLength() throws IOException {
		final JavaSourceReader jsr = make("abc");
		
		jsr.read(new char[10], 1, -1);
	}
	
	@Test(expectedExceptions = IndexOutOfBoundsException.class)
	public void readWithNegativeOffset() throws IOException {
		final JavaSourceReader jsr = make("abc");
		
		jsr.read(new char[10], -1, 1);
	}
	
	@Test(expectedExceptions = IndexOutOfBoundsException.class)
	public void readWithOffsetInsideButOffsetPlusLengthOutsideBuffer() throws IOException {
		final JavaSourceReader jsr = make("abc");
		
		jsr.read(new char[1], 0, 2);
	}
	
	@Test(expectedExceptions = IndexOutOfBoundsException.class)
	public void readWithOffsetOutsideBuffer() throws IOException {
		final JavaSourceReader jsr = make("abc");
		
		jsr.read(new char[1], 2, 0);
	}
	
	@Test(expectedExceptions = IOException.class)
	public void singleU() throws IOException {
		make("\\uu0061", true);
	}
	
	@Test(expectedExceptions = IOException.class)
	public void unexpectedEndOfFile() throws IOException {
		make("\\u000");
	}
	
	@Test
	public void unicodeCarriageReturnCaretPosition() throws IOException {
		final JavaSourceReader jsr = make("\\uuu000D");
		
		jsr.read();
		
		assertEquals(1, jsr.getLine());
		assertEquals(9, jsr.getColumn());
	}
	
	@Test
	public void unicodeCarriageReturnPlusLineFeedCaretPosition() throws IOException {
		final JavaSourceReader jsr = make("\\u000D\n");
		
		jsr.read();
		
		assertEquals(2, jsr.getLine());
		assertEquals(1, jsr.getColumn());
	}
	
	@Test
	public void unicodeCarriageReturnPlusUnicodeLineFeedCaretPosition() throws IOException {
		final JavaSourceReader jsr = make("\\uuuu000D\\uuuuu000A");
		
		jsr.read();
		
		assertEquals(1, jsr.getLine());
		assertEquals(20, jsr.getColumn());
	}
	
	@Test
	public void unicodeEscapeCaretPosition() throws IOException {
		final JavaSourceReader jsr = make("\\u0061\\uuuuuuuu0062");
		
		assertEquals('a', jsr.read());
		assertEquals(1, jsr.getLine());
		assertEquals(7, jsr.getColumn());
		
		assertEquals('b', jsr.read());
		assertEquals(1, jsr.getLine());
		assertEquals(20, jsr.getColumn());
	}
	
	@Test
	public void unicodeEscapes() throws IOException {
		final JavaSourceReader jsr = make(""
		    + "\\u0061"
		    + "\\u0062"
		    + "\\u0063"
		    + "\\u2665"
		    + "\\u263A"
		);
		
		assertEquals('a', jsr.read());
		assertEquals('b', jsr.read());
		assertEquals('c', jsr.read());
		assertEquals('\u2665', jsr.read());
		assertEquals('\u263A', jsr.read());
		
		assertTrue(jsr.read() < 0);
	}
	
	@Test
	public void unicodeLineFeedCaretPosition() throws IOException {
		final JavaSourceReader jsr = make("\\uu000A");
		
		jsr.read();
		
		assertEquals(1, jsr.getLine());
		assertEquals(8, jsr.getColumn());
	}
}