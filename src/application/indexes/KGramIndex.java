
package application.indexes;

import java.util.*;

/**
 * Maps the k-grams of the given term to the list of vocabulary words that contain the k-gram.
 */
public class KGramIndex implements Index<String, String> {

    private final Map<String, List<String>> kGramIndex;

    /**
     * Constructs a corpus and k-gram index using the input token and value of `k`.
     */
    public KGramIndex(String token) {
        kGramIndex = new HashMap<>();

        buildKGramIndex(token);
    }

    private void buildKGramIndex(String token) {
        // if the original token has a single or no characters, map it to its own term in the map
        if (token.length() <= 1) {
            kGramIndex.put(token, new ArrayList<>(){{add(token);}});
        } else {
            // add flags to the beginning/end of the token, then split on wildcards
            String newToken = "$" + token + "$";
            String[] splitTokens = newToken.split("\\*");

            // for each split token, generate the k-grams and map its postings to the token
            for (int i = 0; i < splitTokens.length; ++i) {
                String splitToken = splitTokens[i];
                /* generate all k-grams up to `k` for each section of the token, map the generated k-grams
                  to the token from which it was generated */
                List<String> kGrams = createKGrams(splitToken);

                // remove the flags we added earlier
                if (splitTokens.length == 1) {
                    kGramIndex.put(splitToken.substring(1, splitToken.length() - 1), new ArrayList<>(kGrams));
                } else if (i == 0) {
                    kGramIndex.put(splitToken.substring(1), new ArrayList<>(kGrams));
                } else if (i == (splitTokens.length - 1)) {
                    kGramIndex.put(splitToken.substring(0, splitToken.length() - 1), new ArrayList<>(kGrams));
                } else {
                    kGramIndex.put(splitToken, new ArrayList<>(kGrams));
                }
            }
        }
        System.out.println(kGramIndex);
    }

    private List<String> createKGrams(String token) {
        List<String> kGrams = new ArrayList<>();
        int k = token.length();

        if (k <= 2) {
            kGrams.add(token);
            return kGrams;
        }

        for (int i = 0; i < 2; ++i) {
            String currentKGram = token.substring(i, k + i - 1);

            kGrams.add(currentKGram);
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

    // testing purposes only
    public static void main(String[] args) {
        KGramIndex kGramIndex1 = new KGramIndex("");
        KGramIndex kGramIndex2 = new KGramIndex("i");
        KGramIndex kGramIndex3 = new KGramIndex("at");
        KGramIndex kGramIndex4 = new KGramIndex("read");
        KGramIndex kGramIndex5 = new KGramIndex("revive");
        KGramIndex kGramIndex6 = new KGramIndex("redacted");
        KGramIndex kGramIndex7 = new KGramIndex("re*en*ate");
        KGramIndex kGramIndex8 = new KGramIndex("re*ers");
        KGramIndex kGramIndex9 = new KGramIndex("red*a*d");

        String testString = "testString";
        System.out.println("Index of \"tSt\" in " + testString + " is: " + testString.indexOf("tSt"));
    }
}