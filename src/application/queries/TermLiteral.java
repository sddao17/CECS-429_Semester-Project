
package application.queries;

import java.util.List;

import application.indexes.Index;
import application.indexes.Posting;
import application.text.TrimQueryTokenProcessor;

/**
 * A TermLiteral represents a single term in a subquery.
 */
public class TermLiteral implements QueryComponent {
	private String mTerm;
	
	public TermLiteral(String term) {
		// Somehow incorporate a TokenProcessor into the getPostings call sequence.
		TrimQueryTokenProcessor processor = new TrimQueryTokenProcessor();

		String processedTerm = processor.processToken(term).get(0);
		mTerm = processedTerm;
	}
	
	public String getTerm() {
		return mTerm;
	}
	
	@Override
	public List<Posting> getPostings(Index index) {
		return index.getPostings(mTerm);
	}
	
	@Override
	public String toString() {
		return mTerm;
	}
}
