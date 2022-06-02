
package application.queries;

import java.util.*;
import java.util.stream.Collectors;

import application.indexes.Index;
import application.indexes.Posting;

/**
 * An AndQuery composes other QueryComponents and merges their postings in an intersection-like operation.
 */
public class AndQuery implements QueryComponent {
	private final List<QueryComponent> mComponents;
	
	public AndQuery(List<QueryComponent> components) {
		mComponents = components;
	}
	
	@Override
	public List<Posting> getPostings(Index index) {
		Set<Posting> intersections = new HashSet<>();
		
		// TODO: program the merge for an AndQuery, by gathering the postings of the composed QueryComponents and
		// intersecting the resulting postings.

		// continue checking two posting lists at a time and have them intersect each other
		for (int i = 0; i < mComponents.size() - 1; ++i) {
			List<Posting> leftPostings = mComponents.get(i).getPostings(index);
			List<Posting> rightPostings = mComponents.get(i + 1).getPostings(index);

			// iterate through all lists, get the common items, and intersect them within a set
			intersections.addAll(leftPostings.stream()
					.distinct()
					.filter(rightPostings::contains)
					.collect(Collectors.toSet()));
		}
		// store the set into a new ArrayList for sorting the document IDs
		ArrayList<Posting> result = new ArrayList<>(intersections);
		Collections.sort(result);

		return result;
	}
	
	@Override
	public String toString() {
		return
		 String.join(" ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()));
	}
}
