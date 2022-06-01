
package application.documents;

import java.nio.file.Path;

/**
 * Represents a document saved as a file on the local file system.
 */
public interface FileDocument extends Document, Comparable<Document> {

	/**
	 * The absolute path to the document's file.
	 */
	Path getFilePath();
}
