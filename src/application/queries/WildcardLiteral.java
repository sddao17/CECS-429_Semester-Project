
package application.queries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import application.indexes.Index;
import application.indexes.KGramIndex;
import application.indexes.Posting;
import application.text.TrimQueryTokenProcessor;
import application.text.WildcardTokenProcessor;

/**
 * A WildcardLiteral represents a single token containing one or more * characters.
 */
public class WildcardLiteral implements QueryComponent {

    private final String mTerm;
    private static final List<String> tokenVocabulary = new ArrayList<>();

    public WildcardLiteral(String term) {
        // Somehow incorporate a TokenProcessor into the getPostings call sequence.
        WildcardTokenProcessor processor = new WildcardTokenProcessor();

        mTerm = processor.processToken(term).get(0);
    }

    public String getTerm() {
        return mTerm;
    }

    public static List<String> getTokenVocabulary() {
        return tokenVocabulary;
    }

    @Override
    public List<Posting> getPostings(Index<String, Posting> index) {
        KGramIndex kGramIndex = new KGramIndex(index.getVocabulary());
        String[] kGramVocabulary = mTerm.split("\\*");
        List<String> candidateTerms = new ArrayList<>();
        List<String> finalTerms = new ArrayList<>();
        ArrayList<Posting> finalPostings = new ArrayList<>();

        // traverse through the split tokens and get their generated k-grams
        for (String token : kGramVocabulary) {
            // for each k-gram, find all candidate terms from the main corpus vocabulary
            for (String kGram : kGramIndex.getVocabulary()) {

                // for efficiency, use the appropriate matching methods depending on whether it has a flag
                if (kGram.startsWith("$")) {
                    // get all terms that match the start of this token
                    for (String corpusToken : tokenVocabulary) {
                        if (corpusToken.startsWith(kGram.substring(1))) {
                            candidateTerms.add(corpusToken);
                        }
                    }
                } else if (kGram.endsWith("$")) {
                    // get all terms that match the end of this token
                    for (String corpusToken : tokenVocabulary) {
                        if (corpusToken.endsWith(kGram.substring(0, kGram.lastIndexOf("$")))) {
                            candidateTerms.add(corpusToken);
                        }
                    }
                } else {
                    // get all terms that contain this token
                    for (String corpusToken : tokenVocabulary) {
                        if (corpusToken.contains(kGram)) {
                            candidateTerms.add(corpusToken);
                        }
                    }
                }
            }
        }
        System.out.println(Arrays.toString(kGramVocabulary));

        // post-filtering step: make sure the terms match the order of the k-gram pattern
        for (String candidateTerm : candidateTerms) {
            boolean containsAllKGrams = true;
            int currentIndex = 0;

            // traverse through all k-gram tokens
            for (String kGram : kGramVocabulary) {
                int currentKGramIndex = candidateTerm.indexOf(kGram, currentIndex);
                if ((currentKGramIndex < currentIndex)) {
                    containsAllKGrams = false;
                    break;
                }
                currentIndex += kGram.length();
            }

            // if the term passed all k-gram patterns, they're legit!
            if (containsAllKGrams) {
                // now that we've confirmed the token is in the document, we can process and add it
                TrimQueryTokenProcessor processor = new TrimQueryTokenProcessor();
                List<String> parsedTerms = processor.processToken(candidateTerm);

                for (String term : parsedTerms) {
                    if (!finalTerms.contains(term)) {
                        finalTerms.add(term);
                    }
                }
            }
        }

        System.out.println(finalTerms);

        // intersect candidate terms for each token in the k-gram vocabulary
        List<QueryComponent> components = new ArrayList<>();
        finalTerms.forEach(c -> components.add(new TermLiteral(c)));
        AndQuery query = new AndQuery(components);
        finalPostings.addAll(query.getPostings(index));

        return finalPostings;
    }

    public static void addToTokenVocab(String token) {
        tokenVocabulary.add(token);
    }

    public static void resetTokenVocab() {
        tokenVocabulary.clear();
    }

    @Override
    public String toString() {
        return mTerm;
    }
}
