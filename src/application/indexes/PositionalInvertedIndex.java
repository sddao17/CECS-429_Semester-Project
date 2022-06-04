
package application.indexes;

import java.util.*;

// Program the PositionalInvertedIndex class and incorporate it into the indexing process.
public class PositionalInvertedIndex implements Index {

    private final Map<String, List<Posting>> index;

    /**
     * Constructs an empty positional inverted index.
     */
    public PositionalInvertedIndex() {
        index = new HashMap<>();
    }

    @Override
    public List<Posting> getPostings(String term) {
        // return an empty list if the term doesn't exist in the map
        if (!index.containsKey(term))
            return new ArrayList<>();

        return index.get(term);
    }

    @Override
    public List<String> getVocabulary() {
        // remember to return a sorted vocabulary
        List<String> vocabulary = new ArrayList<>(index.keySet().stream().toList());
        Collections.sort(vocabulary);

        return vocabulary;
    }

    public void addTerm(String term, int documentId, int position) {
        // each term (key) is mapped to a List of Postings (value)
        List<Posting> existingPostings = index.get(term);

        // if `map.get(term)` returns null, the term doesn't exist in our vocabulary yet
        if (existingPostings == null) {
            // initialize a new ArrayList with a single Posting and add it, along with the term, to our map
            ArrayList<Posting> newPostings = new ArrayList<>(){{add(new Posting(documentId,
                    new ArrayList<>(){{add(position);}}));}};

            index.put(term, newPostings);
        } else {
            // get the last index of the existing postings
            int latestIndex = existingPostings.size() - 1;
            int latestDocumentId = existingPostings.get(latestIndex).getDocumentId();

            // check if the term has our current document id
            if (latestDocumentId != documentId) {
                /* since the term exists but the document ID has not been established yet,
                  we must add a new Posting with the new document ID */
                existingPostings.add(new Posting(documentId, new ArrayList<>(){{add(position);}}));
            } else {
                /* since we've confirmed the term exists within our map, we simply add the term's new position
                  into the Posting with the specific document ID*/
                existingPostings.get(latestIndex).addPosition(position);
            }

        }
    }
}