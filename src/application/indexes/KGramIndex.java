
package application.indexes;

import java.util.*;

/**
 * Maps the k-grams of the given term to the list of vocabulary words that contain the k-gram.
 */
public class KGramIndex implements Index {

    private final Index corpusIndex;
    private final Map<String, List<String>> kGramIndex;
    private final Map<String, List<Posting>> kGramPostings;
    private final int k; // the number of adjacent characters from the term

    /**
     * Constructs a k-gram and postings index using the input index, vocabulary, and `k` value.
     */
    public KGramIndex(Index inputIndex, List<String> vocabulary, int inputK) {
        corpusIndex = inputIndex;
        kGramIndex = new HashMap<>();
        kGramPostings = new HashMap<>();
        k = inputK;

        buildKGramIndex(vocabulary);
        createPostings();
    }

    private void buildKGramIndex(List<String> vocabulary) {
        // for each token, generate the k-grams and map its postings to the token
        for (String token : vocabulary) {
            /* generate all k-grams up to `k` for the token, then store it to be mapped to the token
              from which it was generated */
            List<String> kGrams = createKGrams(token);

            kGramIndex.put(token, new ArrayList<>(kGrams));
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
                    String beginKGram = "$" + currentKGram;

                    kGrams.add("$" + currentKGram);
                    if (!kGramPostings.containsKey(beginKGram)) {
                        kGramPostings.put(beginKGram, new ArrayList<>());
                    }
                }
                kGrams.add(currentKGram);
                if (!kGramPostings.containsKey(currentKGram)) {
                    kGramPostings.put(currentKGram, new ArrayList<>());
                }
                if (j == (token.length() - i) && currentKGram.length() < k) {
                    String endKGram = currentKGram + "$";

                    kGrams.add(endKGram);
                    if (!kGramPostings.containsKey(endKGram)) {
                        kGramPostings.put(endKGram, new ArrayList<>());
                    }
                }
            }
        }

        return kGrams;
    }

    private void createPostings() {
        List<String> corpusVocabulary = corpusIndex.getVocabulary();

        for (String kGram : kGramPostings.keySet()) {
            for (String token : corpusVocabulary) {
                if (kGram.startsWith("$")) {
                    if (token.startsWith(kGram.substring(1))) {
                        if (!kGramPostings.containsKey(kGram)) {
                            kGramPostings.put(kGram, corpusIndex.getPostings(token));
                        } else {
                            kGramPostings.get(kGram).addAll(corpusIndex.getPostings(token));
                        }
                    }
                } else if (kGram.endsWith("$")) {
                    if ((token.length() > kGram.length()) && token.endsWith(kGram.substring(0, token.indexOf('$') + 1))) {
                        if (!kGramPostings.containsKey(kGram)) {
                            kGramPostings.put(kGram, corpusIndex.getPostings(token));
                        } else {
                            kGramPostings.get(kGram).addAll(corpusIndex.getPostings(token));
                        }
                    }
                } else if (token.contains(kGram)) {
                    if (!kGramPostings.containsKey(kGram)) {
                        kGramPostings.put(kGram, corpusIndex.getPostings(token));
                    } else {
                        kGramPostings.get(kGram).addAll(corpusIndex.getPostings(token));
                    }
                }
            }
        }
    }

    public List<String> getKGrams(String term) {
        // return an empty list if the term doesn't exist in the map
        if (!kGramIndex.containsKey(term))
            return new ArrayList<>();

        return kGramIndex.get(term);
    }

    @Override
    public List<Posting> getPostings(String term) {
        // return an empty list if the term doesn't exist in the map
        if (!kGramPostings.containsKey(term))
            return new ArrayList<>();

        return kGramPostings.get(term);
    }

    @Override
    public List<String> getVocabulary() {
        // remember to return a sorted vocabulary
        List<String> vocabulary = new ArrayList<>(kGramPostings.keySet().stream().toList());
        Collections.sort(vocabulary);

        return vocabulary;
    }

    public static void main(String[] args) {
        ArrayList<String> vocabulary = new ArrayList<>(){{
            add("revive");
            add("redacted");
            add("rejuvenate");
            add("revival");
            add("revivers");
        }};
        KGramIndex kGramIndex = new KGramIndex(null, vocabulary, 3);
    }
}