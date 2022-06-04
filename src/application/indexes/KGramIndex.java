
package application.indexes;

import java.util.*;

/**
 * Maps the k-grams of the given term to the list of vocabulary words that contain the k-gram.
 */
public class KGramIndex implements Index<String, String> {

    private final Map<String, List<String>> index;
    private final int k; // the number of adjacent characters from the term

    /**
     * Constructs a k-gram and postings index using the input index, vocabulary, and `k` value.
     */
    public KGramIndex(List<String> vocabulary, int inputK) {
        index = new HashMap<>();
        k = inputK;

        buildKGramIndex(vocabulary);
    }

    private void buildKGramIndex(List<String> vocabulary) {
        // for each token, generate the k-grams and map its postings to the token
        for (String token : vocabulary) {
            /* generate all k-grams up to `k` for the token, then store it to be mapped to the token
              from which it was generated */
            List<String> kGrams = createKGrams(token);

            index.put(token, new ArrayList<>(kGrams));
        }
    }

    private List<String> createKGrams(String token) {
        List<String> kGrams = new ArrayList<>();

        // for all integers leading up to k, add the substrings of variable length throughout the whole token
        for (int i = 1; i <= k; ++i) {
            for (int j = 0; j < (token.length() - i + 1); ++j) {
                String currentKGram = token.substring(j, j + i);

                // if applicable, add a copy of the substrings denoting the beginning or ending of the token
                if (j == 0 && currentKGram.length() < k) {
                    kGrams.add("$" + currentKGram);
                }

                kGrams.add(currentKGram);

                if (j == (token.length() - i) && currentKGram.length() < k) {
                    kGrams.add(currentKGram + "$");
                }
            }
        }

        return kGrams;
    }

    @Override
    public List<String> getPostings(String term) {
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
}