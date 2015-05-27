package de.benshu.cofi.parser.lexer.impl;

import de.benshu.cofi.parser.lexer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.util.*;

public class TokenStreamImpl implements TokenStream {
	/** Logger */
	private static final Logger logger = LoggerFactory.getLogger(TokenStreamImpl.class);
	
	public static TokenStreamImpl create(Reader source) {
		return new TokenStreamImpl(Lexer.getFirstToken(source));
	}
	
	private AbstractTokenImpl next;
	
	private final Deque<AbstractTokenImpl> tracks = new LinkedList<>();
	
	final Set<Token.Kind> validTokens = EnumSet.noneOf(Token.Kind.class);
	
	TokenStreamImpl(AbstractTokenImpl first) {
		this.next = first;
	}
	
	private AbstractTokenImpl advance() {
		next = next(next);
		return next;
	}
	
	@Override
	public Token attemptConsume(Token.Kind... tokenKinds) {
		if (isNext(tokenKinds)) {
			AbstractTokenImpl consumed = next;
			advance();
			validTokens.clear();
			logger.debug("Consuming: " + consumed.getLexeme());
			return consumed;
		}
		
		return null;
	}
	
	@Override
	public Token consume(Token.Kind... tokenKinds) throws UnexpectedTokenException {
		final Token consumed = attemptConsume(tokenKinds);
		
		if (consumed == null) {
			throw new UnexpectedTokenException(next, validTokens);
		} else {
			return consumed;
		}
	}
	
	@Override
	public TokenString consumeString(Token.Kind... tokenKinds) throws UnexpectedTokenException {
		if (tokenKinds == null || tokenKinds.length < 1) {
			throw new IllegalArgumentException(Arrays.toString(tokenKinds));
		} else if (tokenKinds.length == 1) {
			return consume(tokenKinds[0]);
		}
		
		final Token first = consume(tokenKinds[0]);
		for (int i = 1; i < tokenKinds.length - 1; ++i) {
			consume(tokenKinds[i]);
		}
		final Token last = consume(tokenKinds[tokenKinds.length - 1]);
		
		return first.getTokenString(last);
	}
	
	private boolean isNext(final Token.Kind tokenKind) {
		if (tokenKind == null) {
			throw new IllegalArgumentException("null");
		}
		
		if (tracks.isEmpty()) {
			validTokens.add(tokenKind);
		}
		return next != null && next.isA(tokenKind);
	}
	
	@Override
	public boolean isNext(Token.Kind... tokenKinds) {
		if (tokenKinds == null || tokenKinds.length < 1) {
			throw new IllegalArgumentException(Arrays.toString(tokenKinds));
		}
		
		boolean returnValue = false;
		for (Token.Kind tokenKind : tokenKinds) {
			// do not return here, loop through all tokenKinds (validTokens)
			returnValue |= isNext(tokenKind);
		}
		return returnValue;
	}
	
	@Override
	public boolean lookAhead(Scout scout) {
		if (scout == null) {
			throw new IllegalArgumentException("null");
		}
		
		tracks.push(next);
		try {
			return scout.scout();
		} catch (LexerException e) {
			logger.debug("lookAhead failed due to exception.", e);
			return false;
		} finally {
			next = tracks.pop();
		}
	}
	
	@Override
	public boolean lookAhead(Token.Kind... tokenKinds) {
		logger.debug("Primitive look ahead by " + tokenKinds.length + " tokens");
		
		// first is done with isNext => it get's added to validTokens
		if (!isNext(tokenKinds[0])) {
			return false;
		}
		
		AbstractTokenImpl current = next;
		for (int i = 1; i < tokenKinds.length; ++i) {
			current = next(current);
			if (current == null || !current.isA(tokenKinds[i])) {
				return false;
			}
		}
		return true;
	}
	
	private AbstractTokenImpl next(AbstractTokenImpl token) {
		final AbstractTokenImpl result = token.next();
		
		if (result != null && result.isA(Token.Kind.SKIPPABLE)) {
			return next(result);
		} else {
			return result;
		}
	}
	
	@Override
	public UnexpectedTokenException throwUnexpectedTokenException() throws UnexpectedTokenException {
		throw new UnexpectedTokenException(next, validTokens);
	}
}
