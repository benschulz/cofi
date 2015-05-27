package de.benshu.cofi.parser.lexer.impl;

import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.Reader;

/**
 * Simplifies lexical analysis of Java source code.
 * <p/>
 * <p>
 * {@code JavaSourceReader} treats the Java source as a tape. The caret starts to the left of the
 * source's first character.
 * </p>
 * <p/>
 * <pre>
 * 1   2   3   4  ...
 * +---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+
 * | p | a | c | k | a | g | e |   | e | x | a | m | p | l | e | ; |
 * +---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+
 * &circ;
 * </pre>
 * <p/>
 * <p>
 * Level 1 (unicode) escaping is completely covered by this class. The following Java source would
 * produce a tape similar to the above example. (Notice the column numbers.)
 * </p>
 * <p/>
 * <pre>
 * \u0070ackage example;
 *
 * 1   7   8   9  ...
 * +---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+
 * | p | a | c | k | a | g | e |   | e | x | a | m | p | l | e | ; |
 * +---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+
 * &circ;
 * </pre>
 * <p/>
 * <p>
 * Line terminators are also recognized as per the JLS. To further simplify the analysis of the
 * source, line terminators are reduced to the line feed form. This means a call to
 * {@code lookAhead()} or {@code read()} will never return {@code '\r'}.
 * </p>
 * <p/>
 * <p>
 * The following table illustrates how line terminators are treated.
 * </p>
 * <p/>
 * <table>
 * <tr>
 * <th>Line Terminator&#x00B9;</th>
 * <th>line()&#x00B2;</th>
 * <th>column()&#x00B2;</th>
 * </tr>
 * <tr>
 * <td>"\n"</td>
 * <td>l + 1</td>
 * <td>1</td>
 * </tr>
 * <tr>
 * <td>"\r"</td>
 * <td>l + 1</td>
 * <td>1</td>
 * </tr>
 * <tr>
 * <td>"\r\n"</td>
 * <td>l + 1</td>
 * <td>1</td>
 * </tr>
 * <tr>
 * <td>"&#x005C;u000A"</td>
 * <td>l</td>
 * <td>c + 6</td>
 * </tr>
 * <tr>
 * <td>"&#x005C;u000D"</td>
 * <td>l</td>
 * <td>c + 6</td>
 * </tr>
 * <tr>
 * <td>"&#x005C;u000D&#x005C;u000A"</td>
 * <td>l</td>
 * <td>c + 12</td>
 * </tr>
 * <tr>
 * <td>"\r&#x005C;u000A"</td>
 * <td>l + 1</td>
 * <td>7</td>
 * </tr>
 * <tr>
 * <td>"&#x005C;u000D\n"</td>
 * <td>l + 1</td>
 * <td>1</td>
 * </tr>
 * </table>
 * <p/>
 * <p>
 * &#x00B9;: {@code \n} and {@code \r} are auxiliary and stand for the actual character.<br />
 * &#x00B2;: The given value is returned by a call subsequent to reading the line terminator.
 * {@code l} and {@code c} were returned before reading the line terminator.
 * </p>
 *
 * @see <a href="http://java.sun.com/docs/books/jls/third_edition/html/lexical.html#3.3">JLS 3:
 * Unicode Escapes</a>
 * @see <a href="http://java.sun.com/docs/books/jls/third_edition/html/lexical.html#3.4">JLS 3: Line
 * Terminators</a>
 */
public class JavaSourceReader extends Reader {
    /**
     * Magic number, used to index {@code charBuffer} and {@code charLengths}.
     */
    private static final int LAST = 0;

    /**
     * Magic number, used to index {@code charBuffer} and {@code charLengths}.
     */
    private static final int NEXT = 1;

    /**
     * Magic number, used to index {@code charBuffer}.
     */
    private static final int RAW = 2;

    /**
     * The {@code Reader} of the java source.
     */
    private final Reader javaSource;

    /**
     * Implicates whether the character directly to the right of the caret is an odd-numbered
     * backslash, assuming that it is a backslash.
     * <p/>
     * <p>
     * Set to {@code true} if and only if the number of consecutive backslashes ({@code '\\'})
     * directly to the left of the caret is even.
     * </p>
     */
    private boolean oddBackslash = true;

    /**
     * Certain file formats (such as the property file format) disallow Unicode escape sequences to
     * contain more than one 'u'. If and only if this flag is set to {@code true}, only one u is
     * permitted.
     */
    private final boolean singleU;

    /**
     * The currently relevant characters.
     * <p/>
     * <ul>
     * <li> {@code charBuffer[LAST]} was returned by the last call to {@code read()}. If
     * {@code backtracked} is {@code true}/{@code false} is directly to the right/left of the caret.</li>
     * <li> {@code charBuffer[NEXT]} is the character following {@code charBuffer[LAST]}.</li>
     * <li> {@code charBuffer[RAW]} is the first raw character of the character following
     * {@code charBuffer[NEXT]}. It may be {@code '\'} and part of a Unicode escape sequence.</li>
     * <li>{@code -1} stands for the end of the source.</li>
     * </ul>
     *
     * @see #backtracked
     */
    private final int[] charBuffer = new int[3];

    /**
     * Holds the lengths of the two characters {@code charBuffer[LAST]} and {@code charBuffer[NEXT]}.
     * <p/>
     * <p>
     * Characters which were not escaped, including multi-character line terminators, get assigned a
     * length of {@code 1}.
     * </p>
     * <p/>
     * <p>
     * If {@code charBuffer[LAST]} or {@code charBuffer[NEXT]} was Unicode escaped it was longer than
     * a single character in the underlying source. Since the escape sequence itself is discarded, its
     * length has to be stored in order to keep {@code column} corresponding to the source.
     * </p>
     * <p/>
     * <p>
     * Multi-character line terminators which were partially escaped, so that they begin a new line
     * and the character immediately following it is at column {@code c != 1} in the source, get
     * assigned the length of {@code (-c + 1)}.
     * </p>
     */
    private final int[] charLengths = new int[2];

    /**
     * Indicates whether {@code backtrack()} was called since last {@code read()} was called.
     * <p/>
     * <p>
     * The initial value is {@code false}, to satisfy the precondition of
     * {@code adjustLineAndColumn()}, but it is set to {@code true} before the constructor returns,
     * thereby disallowing calls to {@code backtrack()} before {@code read()} was called for the first
     * time.
     * </p>
     *
     * @see #adjustLineAndColumn()
     * @see #backtrack()
     * @see #charBuffer
     */
    private boolean backtracked = false;

    /**
     * The line of the source file which the caret was in before the last call to {@code read()}.
     *
     * @see #backtracked
     */
    private int line;

    /**
     * The column of the source file which the caret was in before the last call to {@code read()}.
     *
     * @see #backtracked
     */
    private int column;

    /**
     * Equivalent to {@code new JavaSourceReader(javaSource, false)}.
     *
     * @param javaSource
     * @throws IOException
     * @see #JavaSourceReader(Reader, boolean)
     */
    public JavaSourceReader(final Reader javaSource) throws IOException {
        this(javaSource, false);
    }

    /**
     * Constructs a new {@code JavaSourceReader}.
     *
     * @param javaSource java source which is to be read
     * @param singleU    only accept Unicode escapes containing exactly one 'u'
     * @throws IOException if reading from {@code javaSource} fails
     */
    public JavaSourceReader(final Reader javaSource, final boolean singleU) throws IOException {
        if (javaSource == null) {
            throw new IllegalArgumentException("null");
        }

        this.javaSource = javaSource;
        this.singleU = singleU;

        line = 1;
        column = 1;

        // initialize charBuffer and charLengths
        charBuffer[RAW] = javaSource.read();
        readInternal();
        readInternal();

        // see backtracked and adjustLineAndColumn()
        backtracked = true;
    }

    /**
     * Updates line and column.
     *
     * @see #column
     * @see #line
     */
    private void adjustLineAndColumn() {
        line = getLine();
        column = getColumn();
    }

    /**
     * Moves the caret back one character.
     * <p/>
     * <p>
     * Backtracking is only allowed after reading the first character and if a character was read
     * since the last time {@code backtrack()} was called.
     * </p>
     */
    public void backtrack() {
        Preconditions.checkState(!backtracked);

        backtracked = true;
    }

    /**
     * Closes the underlying reader.
     *
     * @throws IOException
     * @see Reader#close()
     */
    @Override
    public final void close() throws IOException {
        javaSource.close();
    }

    /**
     * Converts {@code '\r'} and {@code "\r\n"} to {@code '\n'}, adjusting {@code charLengths[LAST]}
     * and calling {@code readNextChar()} if necessary.
     *
     * @throws IOException
     */
    private void convertLineTerminators() throws IOException {
        if (charBuffer[LAST] == '\r') {
            if (charBuffer[NEXT] == '\n') {
                // Invoking readNextChar() now will prevent it from
                // overwriting our changes to charLengths[].
                int crLength = charLengths[LAST];
                readNextChar();

                if (crLength > 1 && charLengths[LAST] > 1) {
                    // Both '\r' and '\n' were escaped.
                    charLengths[LAST] = crLength + charLengths[LAST];
                } else if (charLengths[LAST] > 1) {
                    // If only '\n' was escaped then after reading the line terminator getColumn()
                    // will have to return what's currently in charLength[1]. In order to be able
                    // to deduce that it was not simply an escaped line terminator, it will be signed.
                    // See getColumn() to see why it is incremented.
                    charLengths[LAST] = -charLengths[LAST] + 1;
                }

            }
            charBuffer[LAST] = '\n';
        }
    }

    /**
     * Returns the current column number.
     *
     * @return the current column number
     */
    public final int getColumn() {
        if (backtracked) {
            return column;
        } else if (charBuffer[LAST] == '\n' && charLengths[LAST] <= 1) {
            // If charLength[LAST] is negative, it was a partially escaped line terminator,
            // -charLengths[LAST] + 1 characters following the line break.
            // Since non-escaped line terminators have length 1, we subtract it from 2.
            return 2 - charLengths[LAST];
        } else {
            // Add the length of the passed character
            return column + charLengths[LAST];
        }
    }

    /**
     * Returns the current line number.
     *
     * @return the current line number
     */
    public final int getLine() {
        if (!backtracked && charBuffer[LAST] == '\n' && charLengths[LAST] <= 1) {
            // not backtracked, just passed a line feed which was not fully escaped
            return line + 1;
        } else {
            return line;
        }
    }

    /**
     * Returns the character to the right of the caret, without advancing the caret.
     *
     * @return the character to the right of the caret.
     */
    public final int lookAhead() {
        return charBuffer[backtracked ? LAST : NEXT];
    }

    public boolean lookAhead(char expected) throws IOException {
        if (lookAhead() == expected) {
            read();
            return true;
        } else {
            return false;
        }
    }

    public final int lookAhead(char... expected) throws IOException {
        final int next = lookAhead();

        for (char c : expected) {
            if (c == next) {
                read();
                return c;
            }
        }

        return -1;
    }

    /**
     * Advances the caret to the next position.
     *
     * @return the character directly to the left of the caret after it was moved
     * @throws IOException
     */
    @Override
    public int read() throws IOException {
        if (!backtracked) {
            readInternal();
        }
        backtracked = false;

        return charBuffer[LAST];
    }

    @Override
    public int read(final char[] toBuffer, final int offset, final int length) throws IOException {
        if (offset < 0) {
            throw new IndexOutOfBoundsException("offset = " + offset);
        }
        if (length < 0) {
            throw new IndexOutOfBoundsException("length = " + length);
        }
        if (toBuffer.length - offset < length) {
            throw new IndexOutOfBoundsException(offset + " + " + length + " >= " + toBuffer.length);
        }

        int index = 0;

        for (index = 0; index < length; ++index) {
            final int character = read();
            if (character < 0) {
                return index <= 0 ? -1 : index;
            }
            toBuffer[offset + index] = (char) character;
        }

        return index;
    }

    /**
     * Advances the caret to the next position. This is basically the read() implementation. But since
     * it has to be called twice during object creation, it was extracted to this private method which
     * can not be overridden.
     *
     * @throws IOException
     * @see #read()
     */
    private void readInternal() throws IOException {
        if (charBuffer[LAST] != -1) {
            adjustLineAndColumn();
            readNextChar();
            convertLineTerminators();
        }
    }

    /**
     * Reads the next character in the stream, converting escape sequences to the characters they
     * represent.
     *
     * @throws IOException
     */
    private void readNextChar() throws IOException {
        // Read the next character
        charBuffer[LAST] = charBuffer[NEXT];
        charLengths[LAST] = charLengths[NEXT];
        charBuffer[NEXT] = charBuffer[RAW];
        charLengths[NEXT] = 1;
        charBuffer[RAW] = javaSource.read();

        // Unicode escaping
        if (charBuffer[NEXT] == '\\') {
            if (oddBackslash && charBuffer[2] == 'u') {
                // Skip further u's, if allowed
                while (!singleU && readValidChar() == 'u') {
                    ++charLengths[NEXT];
                }

                // Read the escaped character.
                final int a = Character.digit(charBuffer[RAW], 16);
                final int b = Character.digit(readValidChar(), 16);
                final int c = Character.digit(readValidChar(), 16);
                final int d = Character.digit(readValidChar(), 16);

                if (a < 0 || b < 0 || c < 0 || d < 0) {
                    throw new IOException("Illegal unicode escape sequence at " + getLine() + ":" + getColumn());
                }

                charBuffer[NEXT] = (a << 12) + (b << 8) + (c << 4) + d;
                charBuffer[RAW] = javaSource.read();

                // The u's were already counted, backslash and digits weren't
                charLengths[NEXT] += 5;
            } else {
                // If this one was even, then the next one won't be and vice versa.
                oddBackslash = !oddBackslash;
            }
        } else if (charBuffer[NEXT] == -1) {
            charLengths[NEXT] = 0;
        } else {
            // The next backslash will be proceeded by 0 backslashes, which is an even number.
            oddBackslash = true;
        }
    }

    /**
     * Reads the next character and returns it. Expects the next character to be a valid one (EOF is
     * not accepted).
     *
     * @return the next character
     * @throws IOException
     */
    private int readValidChar() throws IOException {
        charBuffer[RAW] = javaSource.read();
        if (charBuffer[RAW] < 0) {
            throw new IOException("Unexpected end of file at " + getLine() + ':' + getColumn());
        }

        return charBuffer[RAW];
    }

    @Override
    public String toString() {
        return "JavaSourceReader [" //
                + "last = "
                + (charBuffer[LAST] < 0 ? "-1" : "'" + ((char) charBuffer[LAST]) + "'")
                + ", next = "
                + (charBuffer[NEXT] < 0 ? "-1" : "'" + ((char) charBuffer[NEXT]) + "'")
                + ", backtracket = " + backtracked + "]";
    }
}
