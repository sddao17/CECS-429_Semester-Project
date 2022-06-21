
package application.indexes;

import java.util.List;

/**
 * An Index can retrieve postings for a term from a data structure associating terms and the documents
 * that contain them.
 */
public interface Index<K, V> {

	/**
	 * Retrieves a list of Postings of documents that contain the given term.
	 */
	List<V> getPostings(String term);

	/**
	 * Retrieves a list of Postings without positions of documents that contain the given term.
	 */
	List<V> getPositionlessPostings(String term);
	
	/**
	 * A (sorted) list of all terms in the index vocabulary.
	 */
	List<K> getVocabulary();
}
