
package application.indexes;

/**
 * A Posting encapsulates a document ID associated with a search query component.
 */
public class Posting {

	private int mDocumentId;
	
	public Posting(int documentId) {
		mDocumentId = documentId;
	}
	
	public int getDocumentId() {
		return mDocumentId;
	}
}
