
package application.indexes;


import java.util.*;

/**
 * Maps the k-grams of the given term to the list of vocabulary words that contain the k-gram.
 */
public class KGramIndex implements Index<String, String> {

    private final HashMap<String, List<String>> kGramIndex;
    private final TreeSet<String> distinctKGrams;  // distinct tokens in the entire corpus vocabulary

    /**
     * Constructs an empty k-gram index.
     */
    public KGramIndex() {
        kGramIndex = new HashMap<>();
        distinctKGrams = new TreeSet<>();
    }

    public void buildKGramIndex(List<String> vocabulary, int k) {
        // for each token, generate the k-grams and map its postings to the token
        for (String token : vocabulary) {
            addToken(token, k);
        }
    }

    public void addToken(String token, int k) {
        List<String> existingPostings = kGramIndex.get(token);
        List<String> kGrams;

        if (existingPostings == null) {
            String parsedToken = token;
            // if a token doesn't have asterisks at the start/end, add flags
            if (!token.startsWith("*")) {
                parsedToken = "$" + parsedToken;
            }
            if (!token.endsWith("*")) {
                parsedToken = parsedToken + "$";
            }
            // if the token has asterisks, it's most likely a query; split and remove them, then create the k-grams
            if (parsedToken.contains("*")) {
                if (parsedToken.startsWith("*")) {
                    token = token.substring(1);
                    parsedToken = parsedToken.substring(1);
                }
                String[] splitTokens = token.split("\\*");
                String[] splitParsedTokens = parsedToken.split("\\*");

                for (int i = 0; i < splitParsedTokens.length; ++i) {
                    kGrams = createKGrams(splitParsedTokens[i], k);
                    kGramIndex.putIfAbsent(splitTokens[i], kGrams);
                }
            } else {
                kGrams = createKGrams(parsedToken, k);
                kGramIndex.putIfAbsent(token, kGrams);
            }
        }
    }

    private List<String> createKGrams(String token, int k) {
        List<String> kGrams = new ArrayList<>();

        // for all integers leading up to k, add the substrings of variable length throughout the whole token
        for (int i = 0; i < k; ++i) {
            for (int j = 0; j < (token.length() - i); ++j) {
                String currentKGram = token.substring(j, j + i + 1);

                // ignore empty strings and excessive flags
                if (!currentKGram.equals("") && !currentKGram.equals("$") && !currentKGram.equals("$$")) {
                    kGrams.add(currentKGram);
                }

                distinctKGrams.add(currentKGram);
            }
        }

        return kGrams;
    }

    public void addKeyValue(String key, List<String> value) {
        kGramIndex.put(key, value);
    }

    public void addDistinctKGram(String kGram) {
        distinctKGrams.add(kGram);
    }

    public TreeSet<String> getDistinctKGrams() {
        return distinctKGrams;
    }

    @Override
    public List<String> getPostings(String term) {
        // return an empty list if the term doesn't exist in the map
        if (!kGramIndex.containsKey(term))
            return new ArrayList<>();

        return kGramIndex.get(term);
    }

    @Override
    public List<String> getPositionlessPostings(String term) {
        // return an empty list if the term doesn't exist in the map
        if (!kGramIndex.containsKey(term))
            return new ArrayList<>();

        // by default, k-grams do not store positions
        return kGramIndex.get(term);
    }

    @Override
    public List<String> getVocabulary() {
        // remember to return a sorted vocabulary
        List<String> vocabulary = new ArrayList<>(kGramIndex.keySet().stream().toList());
        Collections.sort(vocabulary);

        return vocabulary;
    }

    public String toString() {
        StringBuilder description = new StringBuilder();
        List<String> keys = kGramIndex.keySet().stream().sorted().toList();

        for (int i = 0; i < keys.size(); ++i) {
            String currentKey = keys.get(i);

            description.append(currentKey).append(" -> ").append(kGramIndex.get(currentKey));
            if (i < kGramIndex.size() - 1) {
                description.append("\n");
            }
        }

        return description.toString();
    }
}