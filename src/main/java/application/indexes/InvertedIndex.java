
package application.indexes;

import java.util.*;

public class InvertedIndex implements Index {

    private final HashMap<String, List<Posting>> indexMap;

    /**
     * Constructs an empty inverted index.
     */
    public InvertedIndex() {
        indexMap = new HashMap<>();
    }

    @Override
    public List<Posting> getPostings(String term) {
        // return an empty list if the term doesn't exist in the map
        if (!indexMap.containsKey(term))
            return new ArrayList<>();

        return indexMap.get(term);
    }

    @Override
    public List<String> getVocabulary() {
        // remember to return a sorted vocabulary
        List<String> vocabulary = new ArrayList<>(indexMap.keySet().stream().toList());
        Collections.sort(vocabulary);

        return vocabulary;
    }

    public void addTerm(String term, int documentId) {
        // each term (key) is mapped to a List of Postings (value)
        List<Posting> existingPostings = indexMap.get(term);

        // if `map.get(term)` returns null, the term doesn't exist in our vocabulary yet
        if (existingPostings == null) {
            // initialize a new ArrayList with a single Posting and add it, along with the term, to our map
            ArrayList<Posting> newPostings = new ArrayList<>(){{add(new Posting(documentId));}};
            indexMap.put(term, newPostings);
        } else {
            // since we're fully checking one document at a time,
            // any redundant documentID additions would be at the end of the list
            int latestIndex = existingPostings.size() - 1;
            int latestDocumentId = existingPostings.get(latestIndex).getDocumentId();

            // if the same term is found more than once in the same document, only add the documentId once
            if (latestDocumentId != documentId) {
                existingPostings.add(new Posting(documentId));
            }
        }
    }
}