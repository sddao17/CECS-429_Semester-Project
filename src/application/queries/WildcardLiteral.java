
package application.queries;

import java.util.ArrayList;
import java.util.List;

import application.Application;
import application.indexes.Index;
import application.indexes.KGramIndex;
import application.indexes.Posting;
import application.text.TrimQueryTokenProcessor;

/**
 * A WildcardLiteral represents a single token containing one or more * characters.
 */
public class WildcardLiteral implements QueryComponent {

    private final String mTerm;

    public WildcardLiteral(String term) {
        mTerm = term;
    }

    public String getTerm() {
        return mTerm;
    }

    @Override
    public List<Posting> getPostings(Index<String, Posting> corpusIndex) {
        Index<String, String> corpusKGramIndex = Application.getKGramIndex();
        KGramIndex kGramIndex = new KGramIndex();
        kGramIndex.buildKGramIndex(new ArrayList<>(){{add(mTerm);}}, 3);
        List<String> candidateTokens = new ArrayList<>();
        String[] splitTokens = mTerm.split("\\*");

        for (String corpusToken : corpusKGramIndex.getVocabulary()) {
            ArrayList<String> corpusKGrams = new ArrayList<>(corpusKGramIndex.getPostings(corpusToken));
            boolean matchFound = true;

            // intersect terms within their respective vocabularies
            for (String wildcardToken : kGramIndex.getVocabulary()) {
                List<String> wildcardKGrams = kGramIndex.getPostings(wildcardToken);

                // if the corpus KGrams list contains all wildcard k-grams, the corpus token is a candidate
                if (!corpusKGrams.containsAll(wildcardKGrams)) {
                    matchFound = false;
                    break;
                }
            }

            if (matchFound) {
                candidateTokens.add(corpusToken);
            }
        }
        System.out.println("Candidate tokens: " + candidateTokens);

        List<String> finalTerms = new ArrayList<>();

        // post-filtering step: confirm that the candidate token matches the original pattern
        for (String candidateToken : candidateTokens) {
            // only add the candidate token to the final terms once
            if (!finalTerms.contains(candidateToken)) {
                int tokenCount = 0;
                int startIndex = 0;
                int endIndex = 0;
                int candidateIndex = 0;
                boolean candidateMatchesOrder = true;

                while (startIndex < mTerm.length()) {
                    char currentChar = mTerm.charAt(endIndex);

                    while (currentChar != '*' && endIndex < mTerm.length() - 1) {
                        ++endIndex;
                        currentChar = mTerm.charAt(endIndex);
                    }
                    if (endIndex == mTerm.length() - 1  && currentChar != '*') {
                        ++endIndex;
                    }

                    if (mTerm.charAt(startIndex) != '*') {
                        String tokenSubString = mTerm.substring(startIndex, endIndex);
                        //System.out.println(tokenSubString);
                        int currentCandidateIndex = candidateToken.indexOf(tokenSubString, candidateIndex);

                        if (currentCandidateIndex < 0) {
                            candidateMatchesOrder = false;
                            break;
                        } else {
                            candidateIndex = currentCandidateIndex + tokenSubString.length();
                            ++tokenCount;
                        }
                    }
                    // skip past the next asterisk
                    endIndex += 1;
                    startIndex = endIndex;
                }

                if (tokenCount < splitTokens.length - 1) {
                    candidateMatchesOrder = false;
                }

                /* if the candidate matches the original token, we can finally process and add the term;
                  only add the processed term once */
                if (candidateMatchesOrder) {
                    TrimQueryTokenProcessor processor = new TrimQueryTokenProcessor();
                    String term = processor.processToken(candidateToken).get(0);
                    if (!finalTerms.contains(term)) {
                        finalTerms.add(term);
                    }
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
