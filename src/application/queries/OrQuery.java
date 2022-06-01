
package application.queries;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import application.indexes.Index;
import application.indexes.Posting;

/**
 * An OrQuery composes other QueryComponents and merges their postings with a union-type operation.
 */
public class OrQuery implements QueryComponent {
	// The components of the Or query.
	private List<QueryComponent> mComponents;
	
	public OrQuery(List<QueryComponent> components) {
		mComponents = components;
	}
	
	@Override
	public List<Posting> getPostings(Index index) {
		List<Posting> result = new ArrayList<>();
		List<Integer> pool = new ArrayList<>();
		
		// TODO: program the merge for an OrQuery, by gathering the postings of the composed QueryComponents and
		// unioning the resulting postings.
		System.out.println("OR query terms: " + mComponents);
		for (QueryComponent mComponent : mComponents) {
			List<Posting> currentPostings = mComponent.getPostings(index);

			for (Posting posting : currentPostings) {
				int currentDocId = posting.getDocumentId();
				if (!pool.contains(currentDocId)) {
					pool.add(currentDocId);
					result.add(posting);
				}
			}
		}
		
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
