
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
		System.out.println("Phrase literal terms: " + mTerms);
		/*
		 TODO:
		 Program this method. Retrieve the postings for the individual terms in the phrase,
		 and positional merge them together.
		 */

		// intersect the common postings using AndQuery
		List<QueryComponent> allSubqueries = new ArrayList<>();
		mTerms.forEach(c -> allSubqueries.add(new TermLiteral(c)));
		AndQuery intersections = new AndQuery(allSubqueries);
		List<Posting> postings = intersections.getPostings(index);

		// if there are no intersections, immediately return an empty list
		if (postings.size() <= 0) {
			return postings;
		}

		// store the postings and consecutive positions separately
		ArrayList<Posting> results = new ArrayList<>();
		ArrayList<Integer> consecutivePositions = new ArrayList<>();

		// iterate through the terms, comparing their Postings 2 at a time
		for (int i = 0; i < mTerms.size() - 1; ++i) {
			List<Posting> leftPostings = index.getPostings(mTerms.get(i));
			List<Posting> rightPostings = index.getPostings(mTerms.get(i + 1));

			// debugging only
			try {
				// iterate through each posting from the AndQuery intersections
				for (Posting currentPosting : postings) {
					int currentDocumentId = currentPosting.getDocumentId();
					// get the indexes of the current documentId using binary search
					int leftPostingIndex = binarySearch(leftPostings, currentDocumentId);
					int rightPostingIndex = binarySearch(rightPostings, currentDocumentId);

					// store values for readability
					Posting leftPosting = leftPostings.get(leftPostingIndex);
					Posting rightPosting = rightPostings.get(rightPostingIndex);
					ArrayList<Integer> leftPositions = leftPosting.getPositions();
					ArrayList<Integer> rightPositions = rightPosting.getPositions();

					boolean found = false;
					int leftIndex = 0;
					int rightIndex = 0;

					// continue until the positional index intersection is found
					// or until one of the iterators have reached the end of their list
					while (!found && (leftIndex < leftPositions.size() && rightIndex < rightPositions.size())) {
						// store the current left / right position values for readability
						int rightPosition = rightPositions.get(rightIndex);
						int leftPosition = leftPositions.get(leftIndex);

						// if there is an established consecutive chain, compare that instead
						if (consecutivePositions.size() > 0) {
							leftPosition = consecutivePositions.get(consecutivePositions.size() - 1);
						}

						// if the right term is off by one from the left, add it and the posting to our lists
						if (leftPosition == rightPosition - 1) {
							// only add both the left and right positions when the lists are empty;
							// the first two elements are compared when the consecutivePositions is empty
							if (consecutivePositions.size() <= 0) {
								consecutivePositions.add(leftPosition);
							}
							consecutivePositions.add(rightPosition);
							found = true;

							// else, increment the lesser value's iterator to progress the next comparison
						} else {
							if (leftPosition <= rightPosition) {
								++leftIndex;
							} else {
								++rightIndex;
							}
						}
					}

					// if the consecutive position chain is broken, we must start over
					if (!found) {
						consecutivePositions.clear();
					}
				}
			} catch (Exception err) {
				err.printStackTrace();
			}
		}

		return results;
	}

	/**
	 * Returns the index of the Postings list that contains the documentId.
	 * @param postings 		the list of Postings to search through
	 * @param documentId	the document ID to find the index for
	 * @return              the index of the documentId within the ArrayList
	 */
	private static int binarySearch(List<Posting> postings, int documentId) {
		if (postings.size() <= 0)
			return -1;

		return binarySearchRecursively(postings, 0, postings.size(), documentId);
	}

	/**
	 * Returns the index of the Postings list that contains the documentId.
	 * @param postings 		the list of Postings to search through
	 * @param left	        the left boundary of the ArrayList to check
	 * @param right         the right boundary of the ArrayList to check
	 * @param documentId	the document ID to find the index for
	 * @return              the index of the documentId within the ArrayList
	 */
	private static int binarySearchRecursively(List<Posting> postings, int left, int right, int documentId) {
		// base case: left and right boundaries are positioned at empty array
		// or the int to search is greater than the array's greatest element
		if (right < left) {
			return -1;
		}

		int middle = ((right - left) / 2) + left;

		// return the middle index if it's the element we're searching for
		if (postings.get(middle).getDocumentId() == documentId) {
			return middle;
		}

		// if our intToSearch is less than the current midpoint, halve our search section and try again
		if (documentId < postings.get(middle).getDocumentId()) {
			return binarySearchRecursively(postings, left, middle - 1, documentId);
		}

		// else, the intToSearch must be in the upper half
		return binarySearchRecursively(postings, middle + 1, right, documentId);
	}
	
	@Override
	public String toString() {
		return "\"" + String.join(" ", mTerms) + "\"";
	}
}
