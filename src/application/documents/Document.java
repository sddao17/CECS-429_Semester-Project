
package application.documents;

import java.io.Reader;

/**
 * Represents a document in an index.
 */
public interface Document extends Comparable<Document> {

	/**
	 * The ID used by the index to represent the document.
	 */
	int getId();
	
	/**
	 * Gets a stream over the content of the document.
	 */
	Reader getContent();
	
	/**
	 * The title of the document, for displaying to the user.
	 */
	String getTitle();

	@Override
	default int compareTo(Document otherDocument) {
		return this.getTitle().compareTo(otherDocument.getTitle());
	}
}
