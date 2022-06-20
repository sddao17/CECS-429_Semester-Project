
package application.text;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

/**
 * An EnglishTokenStream creates tokens by splitting on whitespace.
 */
public class EnglishTokenStream implements TokenStream, AutoCloseable {

	private final Reader mReader;
	
	private class EnglishTokenIterator implements Iterator<String> {
		private final Scanner mScanner;
		
		private EnglishTokenIterator() {
			// A Scanner automatically tokenizes text by splitting on whitespace. By composing a Scanner we don't have to
			// duplicate that behavior.
			mScanner = new Scanner(mReader);
		}
	
		@Override
		public boolean hasNext() {
			return mScanner.hasNext();
		}
		
		@Override
		public String next() {
			return mScanner.next();
		}
	}
	
	/**
	 * Constructs an EnglishTokenStream to create tokens from the given Reader.
	 */
	public EnglishTokenStream(Reader inputStream) {
		mReader = inputStream;
	}
	
	@Override
	public Iterable<String> getTokens() {
		// Fancy trick to convert an Iterator to an Iterable.
		return EnglishTokenIterator::new;
	}
	
	@Override
	public void close() throws IOException {
		if (mReader != null)
			mReader.close();
	}
}
