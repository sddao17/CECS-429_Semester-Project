
package application.queries;

import java.util.ArrayList;
import java.util.List;

import application.Application;
import application.indexes.Index;
import application.indexes.Posting;
import application.text.TokenProcessor;

/**
 * Represents a phrase literal consisting of one or more terms that must occur in sequence.
 */
public class PhraseLiteral implements QueryComponent {

	private static final int k = 1;	// the position difference between acceptable consecutive terms
	private final List<QueryComponent> mComponents = new ArrayList<>();	// the list of query components in the phrase

	/**
	 * Constructs a PhraseLiteral with the given individual components.
	 */
	public PhraseLiteral(List<QueryComponent> components) {
		mComponents.addAll(components);
	}

	/**
	 * Constructs a PhraseLiteral given a string with one or more individual components.
	 */
	public PhraseLiteral(QueryComponent component) {
		mComponents.add(component);
	}

	@Override
	public List<Posting> getPostings(Index<String, Posting> index, TokenProcessor processor) {
		List<Posting> resultPostings;
		/* Program this method. Retrieve the postings for the individual terms in the phrase,
		  and positional merge them together. */
		// if the phrase only contains one component, simply return its postings
		if (mComponents.size() == 1) {
			resultPostings = mComponents.get(0).getPostings(index, processor);
		}
		// biword indexes do not support wildcards
		else if (mComponents.size() == 2 && !(mComponents.get(0) instanceof WildcardLiteral) &&
				!(mComponents.get(1) instanceof WildcardLiteral)) {
			Index<String, Posting> biwordIndex = Application.getBiwordIndexes()
					.get(Application.getCurrentDirectory() + "/index/biwordBTree.bin");

			resultPostings = biwordIndex.getPostings(processor.processToken(mComponents.get(0).toString()).get(0) +
					" " + processor.processToken(mComponents.get(1).toString()).get(0));
		} else {
			/* store docID-position1-position2 tuples where all the terms are sequentially in +1 positional order,
		  	  beginning with the postings of the first term */
			// int[0], int[1], int[2] --> doc id, position1 (int), position2 (int)
			List<int[]> positionalIntersects = new ArrayList<>();
			int firstTermIntersects = 0;
			int numOfIntersections = 0;
			List<Posting> leftPostings = mComponents.get(0).getPostings(index, processor);

			// start positional intersecting with postings two at a time
			for (int i = 1; i < mComponents.size(); ++i) {
				// store the current postings for readability
				List<Posting> rightPostings = mComponents.get(i).getPostings(index, processor);

				// positional intersect our current intersections list with the next postings list
				positionalIntersects.addAll(positionalIntersect(leftPostings, rightPostings));

				// mark the position of where the first terms' positional intersections end
				if (i == 1) {
					firstTermIntersects = positionalIntersects.size();
				}

				++numOfIntersections;
				leftPostings = rightPostings;
			}

			resultPostings = findFinalIntersects(positionalIntersects, firstTermIntersects, numOfIntersections);

			if (Application.enabledLogs) {
				System.out.println("--------------------------------------------------------------------------------" +
						"\nPhrase literals: " + mComponents + " -- " + resultPostings.size() + " posting(s)" +
						"\n--------------------------------------------------------------------------------");
			}
		}
		return resultPostings;
	}

	@Override
	public List<Posting> getPositionlessPostings(Index<String, Posting> index, TokenProcessor processor) {
		List<Posting> resultPostings;
		/* Program this method. Retrieve the postings for the individual terms in the phrase,
		  and positional merge them together. */
		// if the phrase only contains one component, simply return its postings
		if (mComponents.size() == 1) {
			return mComponents.get(0).getPositionlessPostings(index, processor);
		}
		// biword indexes do not support wildcards
		else if (mComponents.size() == 2 && !(mComponents.get(0) instanceof WildcardLiteral) &&
				!(mComponents.get(1) instanceof WildcardLiteral)) {
			Index<String, Posting> biwordIndex = Application.getBiwordIndexes()
					.get(Application.getCurrentDirectory() + "/index/biword.bin");

			resultPostings = biwordIndex.getPositionlessPostings(
					processor.processToken(mComponents.get(0).toString()).get(0) + " " +
					processor.processToken(mComponents.get(1).toString()).get(0));
		} else {
			/* store posting-position1-position2 tuples where all the terms are sequentially in +1 positional order,
		  	  beginning with the postings of the first term */
			// int[0], int[1], int[2] --> doc id, position1 (int), position2 (int)
			List<int[]> positionalIntersects = new ArrayList<>();
			int firstTermIntersects = 0;
			int numOfIntersections = 0;
			List<Posting> leftPostings = mComponents.get(0).getPositionlessPostings(index, processor);

			// start positional intersecting with postings two at a time
			for (int i = 1; i < mComponents.size(); ++i) {
				// store the current postings for readability
				List<Posting> rightPostings = mComponents.get(i).getPositionlessPostings(index, processor);

				// positional intersect our current intersections list with the next postings list
				positionalIntersects.addAll(positionalIntersect(leftPostings, rightPostings));

				// mark the position of where the first terms' positional intersections end
				if (i == 1) {
					firstTermIntersects = positionalIntersects.size();
				}

				++numOfIntersections;
				leftPostings = rightPostings;
			}

			resultPostings = findFinalIntersects(positionalIntersects, firstTermIntersects, numOfIntersections);

			if (Application.enabledLogs) {
				System.out.println("--------------------------------------------------------------------------------" +
						"\nPhrase literals: " + mComponents + " -- " + resultPostings.size() + " posting(s)" +
						"\n--------------------------------------------------------------------------------");
			}
		}
		return resultPostings;
	}

	private ArrayList<int[]> positionalIntersect(List<Posting> leftList, List<Posting> rightList) {
		ArrayList<int[]> positionalIntersects = new ArrayList<>();

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
						if (leftPosition < rightPosition) {
							int[] documentPositions = new int[]{leftDocumentId, leftPosition, rightPosition};
							positionalIntersects.add(documentPositions);
						}
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

	private List<Posting> findFinalIntersects(List<int[]> positionalIntersects, int firstTermIntersects,
											  int numOfIntersections) {
		List<Posting> finalIntersects = new ArrayList<>();
		/* the first intersection results are the foundation of determining whether position tuples are consecutive;
		  we start with comparing the first term intersections with all other term intersections;
		  then, we will check if later position tuples are consecutive by adjusting our left/right position boundaries
		  and keep track of the count of consecutive position tuples */
		for (int i = 0; i < firstTermIntersects; ++i) {
			// store values for readability
			int leftDocumentId = positionalIntersects.get(i)[0];
			int leftPosition = positionalIntersects.get(i)[2];
			int consecutiveCount = 0;

			// if there are only two terms, the first intersection is the only positional intersection recorded
			if (numOfIntersections == 1) {
				addPosting(finalIntersects, leftDocumentId);
			} else {
				// check each term intersection against all the other intersections
				for (int j = firstTermIntersects; j < positionalIntersects.size(); ++j) {
					// store values for readability
					int rightDocumentId = positionalIntersects.get(j)[0];
					int rightPosition = positionalIntersects.get(j)[2];

					// we only care about consecutive chains of terms in the same document
					if (leftDocumentId == rightDocumentId) {
						// if the term positions falls within the range of `k`, they are considered consecutive
						if (rightPosition - leftPosition == k) {
							++consecutiveCount;

							// if the number of intersections matches the expected amount, add it to our final list
							if (consecutiveCount == mComponents.size() - 2) {
								addPosting(finalIntersects, leftDocumentId);
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

	private void addPosting(List<Posting> finalIntersects, int documentId) {
		Posting newPosting = new Posting(documentId, new ArrayList<>());

		// if it's empty, we can safely add it as a unique element
		if (finalIntersects.size() <= 0) {
			finalIntersects.add(newPosting);
		} else {
			// store for values for readability
			int latestIndex = finalIntersects.size() - 1;
			int latestDocumentId = finalIntersects.get(latestIndex).getDocumentId();

			// skip duplicate postings with the same documentId
			if (latestDocumentId != documentId) {
				finalIntersects.add(newPosting);
			}
		}
	}

	@Override
	public String toString() {
		return mComponents.toString();
	}
}
