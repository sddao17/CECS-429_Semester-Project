
package application.queries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import application.indexes.Index;
import application.indexes.Posting;

/**
 * Represents a phrase literal consisting of one or more terms that must occur in sequence.
 */
public class PhraseLiteral implements QueryComponent {
	// The list of individual terms in the phrase.
	private List<String> mTerms = new ArrayList<>();
	
	/**
	 * Constructs a PhraseLiteral with the given individual phrase terms.
	 */
	public PhraseLiteral(List<String> terms) {
		mTerms.addAll(terms);
	}
	
	/**
	 * Constructs a PhraseLiteral given a string with one or more individual terms separated by spaces.
	 */
	public PhraseLiteral(String terms) {
		mTerms.addAll(Arrays.asList(terms.split(" ")));
	}
	
	@Override
	public List<Posting> getPostings(Index index) {
		// TODO: program this method. Retrieve the postings for the individual terms in the phrase,
		// and positional merge them together.
		// find the same postings by merging them together using AndQueries
		List<QueryComponent> allSubqueries = new ArrayList<>();
		mTerms.forEach(c -> allSubqueries.add(new TermLiteral(c)));
		AndQuery mergedQuery = new AndQuery(allSubqueries);

		return mergedQuery.getPostings(index);
	}
	
	@Override
	public String toString() {
		return "\"" + String.join(" ", mTerms) + "\"";
	}
}
