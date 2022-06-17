
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

		// return the first element if it exists
		if (processedTerms.size() > 0) {
			return new ArrayList<>(index.getPostings(processedTerms.get(0)));
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	public String toString() {
		return mTerm;
	}
}
