
package application.indexes;

import java.util.ArrayList;
import java.util.List;

/**
 * An Index can retrieve postings for a term from a data structure associating terms and the documents
 * that contain them.
 */
public interface Index {

	/**
	 * Retrieves a list of Postings of documents that contain the given term.
	 * Set to default so specialized Indexes like `KGramIndex` can still implement this interface reasonably.
	 */
	default List<Posting> getPostings(String term) {
		return new ArrayList<>();
	}
	
	/**
	 * A (sorted) list of all terms in the index vocabulary.
	 */
	List<String> getVocabulary();
}
