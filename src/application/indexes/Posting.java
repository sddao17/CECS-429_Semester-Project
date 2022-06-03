
package application.indexes;

import java.util.ArrayList;

/**
 * A Posting encapsulates a document ID associated with a search query component.
 * TODO:
 * Adjust this class so that Postings also store an ArrayList indicating the positions for each document
 * that the term occurs in.
 */
public class Posting implements Comparable<Posting> {

	private final int mDocumentId;
	private final ArrayList<Integer> mPositions;
	
	public Posting(int documentId, ArrayList<Integer> positions) {
		mDocumentId = documentId;
		mPositions = positions;
	}
	
	public int getDocumentId() {
		return mDocumentId;
	}

	/**
	 * Returns the positions of this term found within the document.
	 * @return the list of positions of the term within the document
	 */
	public ArrayList<Integer> getPositions() {
		return mPositions;
	}

	/**
	 * Adds the new position to the end of the positions ArrayList.
	 * @param newPosition the new position to insert
	 */
	public void addPosition(int newPosition) {
		mPositions.add(newPosition);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Posting otherPosting) {
			// if we've reached this point, we've confirmed that the object is castable to Posting
			return this.getDocumentId() == otherPosting.getDocumentId();
		}

		return false;
	}

	@Override
	public int compareTo(Posting otherPosting) {
		if (this.getDocumentId() < otherPosting.getDocumentId())
			return -1;
		else if (this.getDocumentId() == otherPosting.getDocumentId())
			return 0;

		return 1;
	}
}
