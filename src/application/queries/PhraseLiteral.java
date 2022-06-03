
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
	private final List<String> mTerms = new ArrayList<>();

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
		/* Program this method. Retrieve the postings for the individual terms in the phrase,
		  and positional merge them together. */
		/* `k` represents the position difference between acceptable consecutive terms;
		  in the future, we may need to adjust this value - if so, it would have to be a static member variable */
		int k = 1;

		/* store posting-position1-position2 tuples where all the terms are sequentially in +1 positional order,
		  beginning with the postings of the first term */
		// Object[0], Object[1], Object[2] --> Posting, position1 (int), position2 (int)
		List<Object[]> positionalIntersects = new ArrayList<>();
		List<Posting> finalIntersects = new ArrayList<>();
		int firstTermIntersects = 0;

		// start positional intersecting with postings two at a time
		for (int i = 0; i < mTerms.size() - 1; ++i) {
			// store the current postings for readability
			List<Posting> leftPostings = index.getPostings(mTerms.get(i));
			List<Posting> rightPostings = index.getPostings(mTerms.get(i + 1));

			// positional intersect our current intersections list with the next postings list
			positionalIntersects.addAll(positionalIntersect(leftPostings, rightPostings, k));

			// mark the position of where the first terms' positional intersections end
			if (i == 0) {
				firstTermIntersects = positionalIntersects.size();
			}
		}

		/* the first intersection results are the foundation of determining whether position tuples are consecutive;
		  we start with comparing the first term intersections with all other term intersections;
		  then, we will check if later position tuples are consecutive by adjusting our left/right position boundaries
		  and keep track of the count of consecutive position tuples */
		for (int i = 0; i < firstTermIntersects; ++i) {
			// store values for readability
			Posting leftPosting = (Posting) positionalIntersects.get(i)[0];
			int leftDocumentId = leftPosting.getDocumentId();
			int leftPosition = (int) positionalIntersects.get(i)[2];
			int consecutiveCount = 0;

			// if there are only two terms, the first intersection is the only positional intersection recorded
			if (firstTermIntersects == positionalIntersects.size()) {
				addPosting(finalIntersects, leftPosting, leftDocumentId);
			} else {
				// check each term intersection against all the other intersections
				for (int j = firstTermIntersects; j < positionalIntersects.size(); ++j) {
					// store values for readability
					Posting rightPosting = (Posting) positionalIntersects.get(j)[0];
					int rightDocumentId = rightPosting.getDocumentId();
					int rightPosition = (int) positionalIntersects.get(j)[2];

					// we only care about consecutive chains of terms in the same document
					if (leftDocumentId == rightDocumentId) {
						// if the term positions falls within the range of `k`, they are considered consecutive
						if (Math.abs(leftPosition - rightPosition) <= k) {
							++consecutiveCount;

							// if the number of intersections matches the expected amount, add it to our final list
							if (consecutiveCount == mTerms.size() - 2) {
								addPosting(finalIntersects, leftPosting, leftDocumentId);
							}

							// set the left boundary to the latest right boundary to check the consecutive chain
							leftPosition = rightPosition;
						}
					}
				}
			}
		}

		return finalIntersects;
	}

	private ArrayList<Object[]> positionalIntersect(List<Posting> leftList, List<Posting> rightList, int k) {
		ArrayList<Object[]> positionalIntersects = new ArrayList<>();

		int leftListIndex = 0;
		int rightListIndex = 0;

		// implement the positional intersection algorithm found in the textbook
		while (leftListIndex < leftList.size() && rightListIndex < rightList.size()) {
			// store values for readability
			Posting leftPosting = leftList.get(leftListIndex);
			Posting rightPosting = rightList.get(rightListIndex);
			int leftDocumentId = leftPosting.getDocumentId();
			int rightDocumentId = rightPosting.getDocumentId();
			ArrayList<Integer> leftPositions = leftPosting.getPositions();
			ArrayList<Integer> rightPositions = rightPosting.getPositions();

			// only compare postings with the same document
			if (leftDocumentId == rightDocumentId) {
				List<Integer> consecutivePositions = new ArrayList<>();
				int leftPositionsIndex = 0;
				int rightPositionsIndex = 0;

				// compare all left term positions against all right term positions
				while (leftPositionsIndex < leftPositions.size()) {
					int leftPosition = leftPositions.get(leftPositionsIndex);

					// add all positions in the right posting that match the consecutive requirements
					while (rightPositionsIndex < rightPositions.size()) {
						int rightPosition = rightPositions.get(rightPositionsIndex);

						// positions within range of `k` are considered to be consecutive
						if (Math.abs(leftPosition - rightPosition) <= k) {
							consecutivePositions.add(rightPosition);
						} else if (rightPosition > leftPosition) {
							break;
						}

						++rightPositionsIndex;
					}

					// remove all elements where the left term is positioned after the right term
					while (consecutivePositions.size() > 0 &&
							Math.abs(consecutivePositions.get(0) - leftPosition) > k) {
						consecutivePositions.remove(0);
					}

					// add all consecutive posting-position1-position2 tuples
					for (int rightPosition : consecutivePositions) {
						Object[] documentPositions = new Object[]{leftPosting, leftPosition, rightPosition};
						positionalIntersects.add(documentPositions);
					}

					++leftPositionsIndex;
				}
				++leftListIndex;
				++rightListIndex;
				// skip postings that don't have the same documentId
			} else if (leftDocumentId < rightDocumentId) {
				++leftListIndex;
			} else {
				++rightListIndex;
			}
		}

		return positionalIntersects;
	}

	private void addPosting(List<Posting> finalIntersects, Posting leftPosting, int leftDocumentId) {
		// if it's empty, we can safely add it as a unique element
		if (finalIntersects.size() <= 0) {
			finalIntersects.add(leftPosting);
		} else {
			// store for values for readability
			int latestIndex = finalIntersects.size() - 1;
			int latestDocumentId = finalIntersects.get(latestIndex).getDocumentId();

			// skip duplicate postings with the same documentId
			if (latestDocumentId != leftDocumentId) {
				finalIntersects.add(leftPosting);
			}
		}
	}

	@Override
	public String toString() {
		return "\"" + String.join(" ", mTerms) + "\"";
	}
}

