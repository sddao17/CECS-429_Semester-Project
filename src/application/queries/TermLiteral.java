
package application.queries;

import java.util.List;

import application.indexes.Index;
import application.indexes.Posting;
import application.text.QueryTokenProcessor;

/**
 * A TermLiteral represents a single term in a subquery.
 */
public class TermLiteral implements QueryComponent {

	private final String mTerm;
	
	public TermLiteral(String term) {
		// Somehow incorporate a TokenProcessor into the getPostings call sequence.
		QueryTokenProcessor processor = new QueryTokenProcessor();

		mTerm = processor.processToken(term).get(0);
	}
	
	public String getTerm() {
		return mTerm;
	}
	
	@Override
	public List<Posting> getPostings(Index<String, Posting> index) {
		System.out.println("Term literal: " + mTerm);
		return index.getPostings(mTerm);
	}
	
	@Override
	public String toString() {
		return mTerm;
	}
}
