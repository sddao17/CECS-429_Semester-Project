
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
		/*
		 Program this method. Retrieve the postings for the individual terms in the phrase,
		 and positional merge them together.
		 */
		// `k` represents the position difference between words to be acceptable as "consecutive"
		int k = 1;

		// store postings where all the terms are sequentially in +1 positional order,
		// beginning with the postings of the first term
		// Object[0], Object[1], Object[2] = Postings, position1 (int), position2 (int)
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

			if (i == 0) {
				firstTermIntersects = positionalIntersects.size();
			}
		}

		// starting from index 0, compare the first index against all others after it;
		// if ((leftIndex[0] == rightIndex[0]) && (rightIndex[2] - leftIndex[1] <= k))
		// 	increment consecutiveCount and check if consecutiveCount == mTerms.size() - 1;
		// 	if it is, add the posting to the finalIntersections
		for (int i = 0; i < firstTermIntersects; ++i) {
			Posting leftPosting = (Posting) positionalIntersects.get(i)[0];
			int leftDocumentId = leftPosting.getDocumentId();
			int leftPosition = (int) positionalIntersects.get(i)[2];
			int consecutiveCount = 0;

			if (firstTermIntersects == positionalIntersects.size()) {
				addPosting(finalIntersects, leftPosting, leftDocumentId);
			} else {
				for (int j = firstTermIntersects; j < positionalIntersects.size(); ++j) {
					Posting rightPosting = (Posting) positionalIntersects.get(j)[0];
					int rightDocumentId = rightPosting.getDocumentId();
					int rightPosition = (int) positionalIntersects.get(j)[2];

					if (leftDocumentId == rightDocumentId) {
						System.out.println("Entered: " + leftPosition + ", " + rightPosition);
						if (Math.abs(leftPosition - rightPosition) <= k) {
							++consecutiveCount;

							if (consecutiveCount == mTerms.size() - 2) {
								addPosting(finalIntersects, leftPosting, leftDocumentId);
								break;
							}

							leftPosition = rightPosition;
						}
					}
				}
			}
		}

		return finalIntersects;
	}

	private ArrayList<Object[]> positionalIntersect(List<Posting> leftList, List<Posting> rightList, int k) {
		/*
			answer ← {}
			while p1 != NIL and p2 != NIL
				do if docID(p1) = docID(p2)
					then l ← {}
						pp1 ← positions(p1)
						pp2 ← positions(p2)
						while pp1 != NIL
						do while pp2 != NIL
							do if |pos(pp1) − pos(pp2)| ≤ k
								then ADD(l, pos(pp2))
								else if pos(pp2) > pos(pp1)
									then break
								pp2 ← next(pp2)
							while l != h i and |l[0] − pos(pp1)| > k
							do DELETE(l[0])
							for each ps ∈ l
							do ADD(answer, <docID(p1), pos(pp1), psi>)
							pp1 ← next(pp1)
						p1 ← next(p1)
						p2 ← next(p2)
					else if docID(p1) < docID(p2)
						then p1 ← next(p1)
						else p2 ← next(p2)
			return answer
		 */
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

			if (leftDocumentId == rightDocumentId) {
				List<Integer> consecutivePositions = new ArrayList<>();
				int leftPositionsIndex = 0;
				int rightPositionsIndex = 0;

				while (leftPositionsIndex < leftPositions.size()) {
					int leftPosition = leftPositions.get(leftPositionsIndex);

					while (rightPositionsIndex < rightPositions.size()) {
						int rightPosition = rightPositions.get(rightPositionsIndex);

						if (Math.abs(leftPosition - rightPosition) <= k) {
							consecutivePositions.add(rightPosition);
						} else if (rightPosition > leftPosition) {
							break;
						}

						++rightPositionsIndex;
					}

					while (consecutivePositions.size() > 0 &&
							Math.abs(consecutivePositions.get(0) - leftPosition) > k) {
						consecutivePositions.remove(0);
					}

					for (int rightPosition : consecutivePositions) {
						if (leftPosition < rightPosition) {
							Object[] documentPositions = new Object[]{leftPosting, leftPosition, rightPosition};
							positionalIntersects.add(documentPositions);
						}
					}

					++leftPositionsIndex;
				}
				++leftListIndex;
				++rightListIndex;
			} else if (leftDocumentId < rightDocumentId) {
				++leftListIndex;
			} else {
				++rightListIndex;
			}
		}

		return positionalIntersects;
	}

	private void addPosting(List<Posting> finalIntersects, Posting leftPosting, int leftDocumentId) {
		if (finalIntersects.size() <= 0) {
			finalIntersects.add(leftPosting);
		} else {
			int latestIndex = finalIntersects.size() - 1;
			int latestDocumentId = finalIntersects.get(latestIndex).getDocumentId();

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

