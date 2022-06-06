
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
    public KGramIndex() {
        kGramIndex = new HashMap<>();
        distinctKGrams = new ArrayList<>();
    }

    public void buildKGramIndex(List<String> vocabulary, int k) {
        // for each token, generate the k-grams and map its postings to the token
        for (String token : vocabulary) {
            addToken(token, k);
        }
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

    public void addToken(String token, int k) {
        List<String> existingPostings = kGramIndex.get(token);
        List<String> kGrams;

        if (existingPostings == null) {
            String parsedToken = token;
            // if a token doesn't have asterisks in it, add the flag; add it otherwise
            if (!token.startsWith("*")) {
                parsedToken = "$" + parsedToken;
            }
            if (!token.endsWith("*")) {
                parsedToken = parsedToken + "$";
            }
            // if the token has asterisks, it's most likely a query; split and remove them
            if (parsedToken.contains("*")) {
                if (parsedToken.startsWith("*")) {
                    token = token.substring(1);
                    parsedToken = parsedToken.substring(1);
                }
                String[] splitTokens = token.split("\\*");
                String[] splitParsedTokens = parsedToken.split("\\*");

                for (int i = 0; i < splitParsedTokens.length; ++i) {
                    kGrams = createKGrams(splitParsedTokens[i], k);
                    kGramIndex.put(splitTokens[i], kGrams);
                }
            } else {
                kGrams = createKGrams(parsedToken, k);
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

                // ignore empty strings and excessive flags
                if (!currentKGram.equals("") && !currentKGram.equals("$$") &&
                        !(currentKGram.startsWith("$") && currentKGram.endsWith("$"))) {
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

    // testing purposes only
    public static void main(String[] args) {
        ArrayList<String> vocabulary = new ArrayList<>(){{
            //add("national");
            //add("nation*");
            //add("*nal");
            //add("*al");
            //add("na*");
            add("na*al");
            //add("n*al");
            //add("n*");
            //add("*n");
            //add("*finan*cial*");
        }};
        KGramIndex kGramIndex = new KGramIndex();
        kGramIndex.buildKGramIndex(vocabulary, 3);
        List<String> testVocabulary = kGramIndex.getVocabulary();
        for (String token : testVocabulary) {
            System.out.println(token + ": " + kGramIndex.getPostings(token));
        }
    }
}
