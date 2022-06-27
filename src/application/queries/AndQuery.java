
package application.queries;

import java.util.*;
import java.util.stream.Collectors;

import application.indexes.Index;
import application.indexes.Posting;
import application.text.TokenProcessor;

/**
 * An AndQuery composes other QueryComponents and merges their postings in an intersection-like operation.
 */
public class AndQuery implements QueryComponent {

	private final List<QueryComponent> mComponents;
	
	public AndQuery(List<QueryComponent> components) {
		mComponents = components;
	}
	
	@Override
	public List<Posting> getPostings(Index<String, Posting> index, TokenProcessor processor) {
		/* Program the merge for an AndQuery, by gathering the postings of the composed QueryComponents and
		  unionizing the results. */
		// initialize the intersections to be the postings of the first term
		List<Posting> intersections = mComponents.get(0).getPostings(index, processor);

		// start intersecting with the postings of the second term
		for (int i = 1; i < mComponents.size(); ++i) {
			QueryComponent currentComponent = mComponents.get(i);
			// store current posting for readability
			List<Posting> currentPostings = currentComponent.getPostings(index, processor);

			// intersect the current intersections with the new postings
			intersections = intersectPostings(intersections, currentPostings);
		}

		return intersections;
	}

	@Override
	public List<Posting> getPositionlessPostings(Index<String, Posting> index, TokenProcessor processor) {
		/* Program the merge for an AndQuery, by gathering the postings of the composed QueryComponents and
		  unionizing the results. */
		// initialize the intersections to be the postings of the first term
		List<Posting> intersections = mComponents.get(0).getPositionlessPostings(index, processor);

		// start intersecting with the postings of the second term
		for (int i = 1; i < mComponents.size(); ++i) {
			QueryComponent currentComponent = mComponents.get(i);
			// store current posting for readability
			List<Posting> currentPostings = currentComponent.getPositionlessPostings(index, processor);

			// intersect the current intersections with the new postings
			intersections = intersectPostings(intersections, currentPostings);
		}

		return intersections;
	}

	private List<Posting> intersectPostings(List<Posting> leftList, List<Posting> rightList) {
		List<Posting> intersections = new ArrayList<>();

		int leftIndex = 0;
		int rightIndex = 0;

		// implement the intersection algorithm found in the textbook
		while (leftIndex < leftList.size() && rightIndex < rightList.size()) {
			// store values for readability
			Posting leftPosting = leftList.get(leftIndex);
			Posting rightPosting = rightList.get(rightIndex);
			int leftDocumentId = leftPosting.getDocumentId();
			int rightDocumentId = rightPosting.getDocumentId();

			// add the intersection if the posting has the same document ID and progress the index iterators
			if (leftDocumentId == rightDocumentId) {
				intersections.add(leftPosting);
				++leftIndex;
				++rightIndex;
			} else if (leftDocumentId < rightDocumentId){
				++leftIndex;
			} else {
				++rightIndex;
			}
		}

		return intersections;
	}
	
	@Override
	public String toString() {
		return
		 String.join(" ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()));
	}
}
