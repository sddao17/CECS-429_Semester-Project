
package application.queries;

import java.util.List;

import application.Application;
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

		List<Posting> resultPostings = index.getPostings(processedTerms.get(0));

		if (Application.enabledLogs) {
			System.out.println("--------------------------------------------------------------------------------" +
					"\nTerm literal: `" + processedTerms.get(0) + "` -- " + resultPostings.size() + " posting(s)" +
					"\n--------------------------------------------------------------------------------");
		}

		return resultPostings;

	}

	@Override
	public String toString() {
		return mTerm;
	}
}
