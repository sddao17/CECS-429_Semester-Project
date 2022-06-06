
package application.indexes;

import java.util.*;

/**
 * Maps the k-grams of the given term to the list of vocabulary words that contain the k-gram.
 */
public class KGramIndex implements Index<String, String> {

    private final HashMap<String, List<String>> kGramIndex;
    private final List<String> distinctKGrams;  // distinct tokens in the entire corpus vocabulary

    /**
     * Constructs an empty k-gram index.
     */
    public KGramIndex(List<String> vocabulary, int k) {
        kGramIndex = new HashMap<>();
        distinctKGrams = new ArrayList<>();

        buildKGramIndex(vocabulary, k);
    }

    private void buildKGramIndex(List<String> vocabulary, int k) {
        List<String> kGrams;

        // for each token, generate the k-grams and map its postings to the token
        for (String token : vocabulary) {
            // if a token has asterisks in it, it's most likely a query; split it into separate k-grams
            if (token.contains("*")) {
                String[] splitTokens = token.split("\\*");
                String[] splitParsedTokens = ("$" + token + "$").split("\\*");

                for (int i = 0; i < splitTokens.length; ++i) {
                    String parsedToken = splitParsedTokens[i];
                    kGrams = createKGrams(parsedToken, 3);
                    kGramIndex.put(splitTokens[i], kGrams);
                }
            } else {
                kGrams = createKGrams("$" + token + "$", k);
                kGramIndex.put(token, kGrams);
            }
        }
    }

    private List<String> createKGrams(String token, int k) {
        List<String> kGrams = new ArrayList<>();

        // for all integers leading up to k, add the substrings of variable length throughout the whole token
        for (int i = 0; i <= k; ++i) {
            for (int j = 0; j < (token.length() - i + 1); ++j) {
                String currentKGram = token.substring(j, j + i);

                // ignore empty strings and flags
                if (!currentKGram.equals("") && !currentKGram.equals("$")) {
                    // add distinct tokens only
                    if (!kGrams.contains(currentKGram)) {
                        kGrams.add(currentKGram);
                    }
                    if (!distinctKGrams.contains(currentKGram)) {
                        distinctKGrams.add(currentKGram);
                    }
                }
            }
        }

        return kGrams;
    }

    @Override
    public List<String> getPostings(String term) {
        // return an empty list if the term doesn't exist in the map
        if (!kGramIndex.containsKey(term))
            return new ArrayList<>();

        return kGramIndex.get(term);
    }

    @Override
    public List<String> getVocabulary() {
        // remember to return a sorted vocabulary
        List<String> vocabulary = new ArrayList<>(kGramIndex.keySet().stream().toList());
        Collections.sort(vocabulary);

        return vocabulary;
    }

    public List<String> getDistinctKGrams() {
        Collections.sort(distinctKGrams);

        return distinctKGrams;
    }

    // testing purposes only
    public static void main(String[] args) {
        ArrayList<String> vocabulary = new ArrayList<>(){{
            add("national");
            add("na*al");
            add("n*al");
            add("financial");
        }};
        KGramIndex kGramIndex = new KGramIndex(vocabulary, 3);
        List<String> testVocabulary = kGramIndex.getVocabulary();
        for (String token : testVocabulary) {
            System.out.println(token + ": " + kGramIndex.getPostings(token));
        }
    }
}
