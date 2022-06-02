
package application.queries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import application.indexes.Index;
import application.indexes.Posting;

/**
 * An OrQuery composes other QueryComponents and merges their postings with a union-type operation.
 */
public class OrQuery implements QueryComponent {
	// The components of the Or query.
	private final List<QueryComponent> mComponents;
	
	public OrQuery(List<QueryComponent> components) {
		mComponents = components;
	}
	
	@Override
	public List<Posting> getPostings(Index index) {
		List<Posting> result = new ArrayList<>();
		List<Integer> pool = new ArrayList<>();
		
		/*
		 TODO:
		 program the merge for an OrQuery, by gathering the postings of the composed QueryComponents and
		 */

		// iterate through each separated query term
		for (QueryComponent mComponent : mComponents) {
			// get the postings associated for that term
			List<Posting> currentPostings = mComponent.getPostings(index);

			// iterate through each posting for the current term
			for (Posting posting : currentPostings) {
				// if the next document ID is unique to the current pool, add it / its posting to our pool / result
				int currentDocId = posting.getDocumentId();
				if (!pool.contains(currentDocId)) {
					pool.add(currentDocId);
					result.add(posting);
				}
			}
		}
		// remember to sort the documents
		Collections.sort(result);
		
		return result;
	}
	
	@Override
	public String toString() {
		// Returns a string of the form "[SUBQUERY] + [SUBQUERY] + [SUBQUERY]"
		return "(" +
		 String.join(" + ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()))
		 + ")";
	}
}
