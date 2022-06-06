
package application.indexes;

import java.util.*;

/**
 * Maps the k-grams of the given term to the list of vocabulary words that contain the k-gram.
 */
public class KGramIndex implements Index {

    private final HashMap<String, List<String>> kGramIndex;
    private final List<String> distinctKGrams;  // distinct tokens in the entire corpus vocabulary
    private final List<String> tokenVocabulary;  // pre-processed vocabulary tokens from the corpus vocabulary


    /**
     * Constructs an empty k-gram index using the input K.
     */
    public KGramIndex(List<String> vocabulary) {
        kGramIndex = new HashMap<>();
        distinctKGrams = new ArrayList<>();
        tokenVocabulary = new ArrayList<>();

        buildKGramIndex(vocabulary);
    }

    private void buildKGramIndex(List<String> vocabulary) {
        // for each token, generate the k-grams and map its postings to the token
        for (String token : vocabulary) {
            List<String> kGrams = createKGrams(token);

            kGramIndex.put(token, new ArrayList<>(kGrams));
        }
        System.out.println(kGramIndex);
    }

    private List<String> createKGrams(String token) {
        List<String> kGrams = new ArrayList<>();
        // the number of adjacent characters within the string
        int k = token.length();

        // for all integers leading up to k, add the substrings of variable length throughout the whole token
        for (int i = 1; i <= k; ++i) {
            for (int j = 0; j < (token.length() - i + 1); ++j) {
                String currentKGram = token.substring(j, j + i);

                // add a copy of the substrings denoting the beginning or ending of the token
                if (j == 0 && currentKGram.length() < k) {
                    String newKGram = "$" + currentKGram;
                    kGrams.add(newKGram);
                    if (!distinctKGrams.contains(newKGram)) {
                        distinctKGrams.add(newKGram);
                    }
                }
                kGrams.add(currentKGram);
                if (!distinctKGrams.contains(currentKGram)) {
                    distinctKGrams.add(currentKGram);
                }
                if (j == (token.length() - i) && currentKGram.length() < k) {
                    String newKGram = currentKGram + "$";
                    kGrams.add(newKGram);
                    if (!distinctKGrams.contains(newKGram)) {
                        distinctKGrams.add(newKGram);
                    }
                }
            }
        }

        return kGrams;
    }

    @Override
    public List<Posting> getPostings(String term) {
        return new ArrayList<>();
    }

    @Override
    public List<String> getVocabulary() {
        // remember to return a sorted vocabulary
        List<String> vocabulary = new ArrayList<>(kGramIndex.keySet().stream().toList());
        Collections.sort(vocabulary);

        return vocabulary;
    }

    public List<String> getDistinctKGrams() {
        return distinctKGrams;
    }

    public List<String> getTokenVocabulary() {
        return tokenVocabulary;
    }

    public void addTokenToVocab(String token) {
        tokenVocabulary.add(token);
    }

    // testing purposes only
    public static void main(String[] args) {
        ArrayList<String> vocabulary = new ArrayList<>(){{
            add("revive");
            add("redacted");
            add("rejuvenate");
            add("revival");
            add("revivers");
            add("read");
            add("red");
            add("at");
            add("i");
        }};
        KGramIndex kGramIndex = new KGramIndex(vocabulary);
        List<String> testVocabulary = kGramIndex.getVocabulary();
        for (String token : testVocabulary) {
            System.out.println(token + ": " + kGramIndex.getPostings(token));
        }
    }
}