
package application.indexes;

import java.util.*;

/**
 * Maps the k-grams of the given term to the list of vocabulary words that contain the k-gram.
 */
public class KGramIndex implements Index {

    private final HashMap<String, List<Posting>> indexMap;
    private final int k; // the number of adjacent characters from the term

    /**
     * Constructs an empty k-gram index using the input K.
     */
    public KGramIndex(String term, Index index, int inputK) {
        indexMap = new HashMap<>();
        k = inputK;

        List<String> kGrams = createKGrams(term);
        buildKGramIndex(kGrams, index);
    }

    private List<String> createKGrams(String term) {
        List<String> kGrams = new ArrayList<>();

        // create all k-grams between 1 and the inputK
        for (int i = 1; i <= k; ++i) {
            for (int j = 0; j < (term.length() - i + 1); ++j) {
                String currentKGram = term.substring(j, j + i);

                kGrams.add(currentKGram);
            }
        }

        return kGrams;
    }

    private void buildKGramIndex(List<String> kGrams, Index index) {
        List<String> vocabulary = index.getVocabulary();

        for (String kGram : kGrams) {
            // add the k-gram ands its postings if it doesn't exist yet
            if (!indexMap.containsKey(kGram)) {
                indexMap.put(kGram, new ArrayList<>());

                // add each term's postings to the postings list if it contains the k-gram
                for (String term : vocabulary) {
                    if (term.contains(kGram)) {
                        indexMap.get(kGram).addAll(index.getPostings(term));
                    }
                }
            }
        }
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
}