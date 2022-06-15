
package application.queries;

import java.util.ArrayList;
import java.util.List;

import application.indexes.Index;
import application.indexes.Posting;
import application.text.TokenProcessor;

/**
 * A TermLiteral represents a single term in a subquery.
 */
public class TermLiteral implements QueryComponent {

	private final String mTerm;

	public TermLiteral(String term) {
		mTerm = term;
	}

	public String getTerm() {
		return mTerm;
	}

	@Override
	public List<Posting> getPostings(Index<String, Posting> index, TokenProcessor processor) {
		// Somehow incorporate a TokenProcessor into the getPostings call sequence.
		List<String> processedTerms = processor.processToken(mTerm);
		List<Posting> resultPostings = new ArrayList<>();

		for (String processedTerm : processedTerms) {
			resultPostings.addAll(index.getPostings(processedTerm));
		}

		return resultPostings;
	}

	@Override
	public String toString() {
		return mTerm;
	}
}
