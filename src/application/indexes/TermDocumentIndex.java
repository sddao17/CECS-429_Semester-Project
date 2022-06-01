
package application.indexes;

import java.util.*;

/**
 * Implements an Index using a term-document matrix. Requires knowing the full corpus vocabulary and number of documents
 * prior to construction.
 */
public class TermDocumentIndex implements Index {

	private final boolean[][] mMatrix;
	private final List<String> mVocabulary;
	private int mCorpusSize;
	
	/**
	 * Constructs an empty index with given vocabulary set and corpus size.
	 * @param vocabulary a collection of all terms in the corpus vocabulary.
	 * @param corpusSize the number of documents in the corpus.
	 */
	public TermDocumentIndex(Collection<String> vocabulary, int corpusSize) {
		mMatrix = new boolean[vocabulary.size()][corpusSize];
		mVocabulary = new ArrayList<>();
		mVocabulary.addAll(vocabulary);
		mCorpusSize = corpusSize;
		
		Collections.sort(mVocabulary);
	}
	
	/**
	 * Associates the given documentId with the given term in the index.
	 */
	public void addTerm(String term, int documentId) {
		int vIndex = Collections.binarySearch(mVocabulary, term);
		if (vIndex >= 0) {
			mMatrix[vIndex][documentId] = true;
		}
	}
	
	@Override
	public List<Posting> getPostings(String term) {
		List<Posting> results = new ArrayList<>();
		
		// TODO: implement this method.
		// Binary search the mVocabulary array for the given term.
		// Walk down the mMatrix row for the term and collect the document IDs (column indices)
		// of the "true" entries.
		int index = Collections.binarySearch(mVocabulary, term);

		// return an empty list if the element was not found
		if (index < 0) {
			return results;
		}

		int rowLength = mMatrix[index].length;

		// traverse the row of the index of the term
		for (int i = 0; i < rowLength; ++i) {
			// if the column entry is "true", add the column index as a Posting to our results
			if (mMatrix[index][i]) {
				results.add(new Posting(i));
			}
		}
		
		return results;
	}
	
	public List<String> getVocabulary() {
		return Collections.unmodifiableList(mVocabulary);
	}
}
