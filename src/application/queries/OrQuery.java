
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

	private final List<QueryComponent> mComponents;	// the components of the Or query
	
	public OrQuery(List<QueryComponent> components) {
		mComponents = components;
	}
	
	@Override
	public List<Posting> getPostings(Index index) {
		List<Posting> unions = new ArrayList<>();
		
		/* Program the merge for an OrQuery, by gathering the postings of the composed QueryComponents and
		  unionizing the resulting postings. */
		for (QueryComponent mComponent : mComponents) {
			// store current posting for readability
			List<Posting> currentPostings = mComponent.getPostings(index);

			// unionize the current unions with the new postings
			unions = unionizePostings(unions, currentPostings);
		}
		
		return unions;
	}

	private List<Posting> unionizePostings(List<Posting> leftList, List<Posting> rightList) {
		List<Posting> unions = new ArrayList<>();

		int leftIndex = 0;
		int rightIndex = 0;

		// iterate through the lists until one (or both) have been fully traversed
		while (leftIndex < leftList.size() && rightIndex < rightList.size()) {
			// store values for readability
			Posting leftPosting = leftList.get(leftIndex);
			Posting rightPosting = rightList.get(rightIndex);
			int leftDocumentId = leftPosting.getDocumentId();
			int rightDocumentId = rightPosting.getDocumentId();

			// add the union if the posting has the same document ID and progress the index iterators
			if (leftDocumentId < rightDocumentId) {
				unions.add(leftPosting);
				++leftIndex;
			} else if (leftDocumentId > rightDocumentId) {
				unions.add(rightPosting);
				++rightIndex;
			} else {
				// add one of the duplicate elements and then increment both iterators
				unions.add(rightPosting);
				++leftIndex;
				++rightIndex;
			}
		}

		// similar to mergeSort, add any leftovers of any non-fully-traversed list
		if (leftIndex < leftList.size()) {
			unions.addAll(leftList.subList(leftIndex, leftList.size()));
		} else if (rightIndex < rightList.size()) {
			unions.addAll(rightList.subList(rightIndex, rightList.size()));
		}

		return unions;
	}
	
	@Override
	public String toString() {
		// Returns a string of the form "[SUBQUERY] + [SUBQUERY] + [SUBQUERY]"
		return "(" +
		 String.join(" + ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()))
		 + ")";
	}
}
