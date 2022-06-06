
package application.queries;

import java.util.ArrayList;
import java.util.List;

import application.Application;
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

    public WildcardLiteral(String term) {
        // Somehow incorporate a TokenProcessor into the getPostings call sequence.
        WildcardTokenProcessor processor = new WildcardTokenProcessor();

        mTerm = processor.processToken(term).get(0);
    }

    public String getTerm() {
        return mTerm;
    }

    @Override
    public List<Posting> getPostings(Index<String, Posting> corpusIndex) {
        Index<String, String> corpusKGramIndex = Application.getKGramIndex();
        Index<String, String> kGramIndex = new KGramIndex(new ArrayList<>(){{add(mTerm);}}, 3);
        List<String> candidateTokens = new ArrayList<>();
        String[] splitTokens = mTerm.split("\\*");

        // intersect terms within their respective vocabularies
        for (String wildcardToken : splitTokens) {
            List<String> wildcardKGrams = kGramIndex.getPostings(wildcardToken);

            for (String corpusToken : corpusKGramIndex.getVocabulary()) {
                ArrayList<String> corpusKGrams = new ArrayList<>(corpusKGramIndex.getPostings(corpusToken));

                // if the corpus KGrams list contains all wildcard k-grams, the corpus token is a candidate
                if (corpusKGrams.containsAll(wildcardKGrams)) {
                    candidateTokens.add(corpusToken);
                }
            }
        }
        System.out.println("Candidate tokens: " + candidateTokens);

        List<String> finalTerms = new ArrayList<>();

        // post-filtering step: confirm that the candidate token matches the original pattern
        for (String candidateToken : candidateTokens) {
            if (!finalTerms.contains(candidateToken)) {
                int currentIndex = 0;
                boolean candidateMatchesOrder = true;

                for (String splitToken : splitTokens) {
                    int tokenIndex = candidateToken.indexOf(splitToken, currentIndex);
                    if (tokenIndex < 0) {
                        candidateMatchesOrder = false;
                        break;
                    }
                    currentIndex = tokenIndex + splitToken.length();
                }

                // if the candidate matches the original token, we can finally process and add the term
                if (candidateMatchesOrder) {
                    TrimQueryTokenProcessor processor = new TrimQueryTokenProcessor();
                    finalTerms.add(processor.processToken(candidateToken).get(0));
                }
            }
        }
        System.out.println("Final terms: " + finalTerms);

        // once we collect all of our final terms, we "OR" the postings to combine them and ignore duplicates
        List<QueryComponent> literals = new ArrayList<>();
        finalTerms.forEach(term -> literals.add(new TermLiteral(term)));
        OrQuery query = new OrQuery(literals);

        return query.getPostings(corpusIndex);
    }

    @Override
    public String toString() {
        return mTerm;
    }
}
