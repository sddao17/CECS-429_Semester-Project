package application.queries;

import java.util.ArrayList;
import java.util.List;

import application.Application;
import application.UI.CorpusSelection;
import application.indexes.Index;
import application.indexes.KGramIndex;
import application.indexes.Posting;
import application.text.QueryTokenProcessor;
import application.text.VocabularyTokenProcessor;
import application.text.WildcardTokenProcessor;

/**
 * A WildcardLiteral represents a single token containing one or more * characters.
 */
public class WildcardLiteral implements QueryComponent {

    private final String mTerm;
    private static CorpusSelection CSelect = new CorpusSelection();

    public WildcardLiteral(String term) {
        /* don't fully normalize the term yet; it will be normalized once we confirm the term's
          unprocessed counterpart is within the document.
          for now, keep the asterisks as the only non-alphanumeric character within our term */
        WildcardTokenProcessor processor = new WildcardTokenProcessor();

        mTerm = processor.processToken(term).get(0);
    }

    public String getTerm() {
        return mTerm;
    }

    @Override
    public List<Posting> getPostings(Index<String, Posting> corpusIndex) {
        Index<String, String> corpusKGramIndex = CSelect.getKGramIndex();
        KGramIndex kGramIndex = new KGramIndex();
        kGramIndex.buildKGramIndex(new ArrayList<>(){{add(mTerm);}}, 3);
        String[] originalTokens = mTerm.split("\\*");

        List<String> candidateTokens = findCandidates(corpusKGramIndex, kGramIndex);
        //System.out.println("Candidate tokens: " + candidateTokens);

        List<String> finalTerms = postFilter(candidateTokens, originalTokens);
        //System.out.println("Final terms: " + finalTerms);

        // once we collect all of our final terms, we "OR" the postings to combine them and ignore duplicates
        List<QueryComponent> literals = new ArrayList<>();
        finalTerms.forEach(term -> literals.add(new TermLiteral(term)));
        OrQuery query = new OrQuery(literals);

        return query.getPostings(corpusIndex);
    }

    private List<String> findCandidates(Index<String, String> corpusKGramIndex, Index<String, String> kGramIndex) {
        List<String> candidateTokens = new ArrayList<>();

        /* find candidate tokens by traversing through the corpus and comparing each of them to our
          generated k-grams; intersect the postings by finding tokens that share the same k-gram patterns */
        for (String corpusToken : corpusKGramIndex.getVocabulary()) {
            ArrayList<String> corpusKGrams = new ArrayList<>(corpusKGramIndex.getPostings(corpusToken));

            // intersect terms within their respective vocabularies
            for (String wildcardToken : kGramIndex.getVocabulary()) {
                List<String> wildcardKGrams = kGramIndex.getPostings(wildcardToken);

                // if the corpus KGrams list contains all wildcard k-grams, the corpus token is a candidate
                if (corpusKGrams.containsAll(wildcardKGrams)) {
                    candidateTokens.add(corpusToken);
                }
            }
        }

        return candidateTokens;
    }

    private List<String> postFilter(List<String> candidateTokens, String[] originalTokens) {
        List<String> finalTerms = new ArrayList<>();

        // post-filtering step: confirm that the candidate token matches the original pattern
        for (String candidateToken : candidateTokens) {
            int tokenCount = 0;
            int startIndex = 0;
            int endIndex = 0;
            int candidateIndex = 0;
            boolean candidateMatchesOrder = true;

            /* traverse through the original `mTerm`; for every substring leading to an asterisk, verify
              that the original token's substrings exists in the same order within the candidate token */
            while (startIndex < mTerm.length()) {
                char currentChar = mTerm.charAt(endIndex);

                // stop incrementing the right bound when reached either an asterisk or the last character
                while (currentChar != '*' && endIndex < mTerm.length() - 1) {
                    ++endIndex;
                    currentChar = mTerm.charAt(endIndex);
                }
                if (endIndex == mTerm.length() - 1  && currentChar != '*') {
                    ++endIndex;
                }

                // if there is an asterisk at the first index, skip to the next substring
                if (mTerm.charAt(startIndex) != '*') {
                    String tokenSubString = mTerm.substring(startIndex, endIndex);
                    int currentCandidateIndex = candidateToken.indexOf(tokenSubString, candidateIndex);

                    if (currentCandidateIndex < 0) {
                        candidateMatchesOrder = false;
                        break;
                    } else {
                        candidateIndex = currentCandidateIndex + tokenSubString.length();
                        ++tokenCount;
                    }
                }
                // increment towards the end of the string
                endIndex += 1;
                startIndex = endIndex;
            }

            /* if we've matched the number of split tokens (separated by asterisks) within the original token
              then the token fulfills the pattern */
            if (tokenCount < originalTokens.length - 1) {
                candidateMatchesOrder = false;
            }

            /* if the candidate matches the original token, we can finally process and add the term;
              only add the processed term once */
            if (candidateMatchesOrder) {
                VocabularyTokenProcessor processor = new VocabularyTokenProcessor();
                String term = processor.processToken(candidateToken).get(0);

                if (!finalTerms.contains(term)) {
                    finalTerms.add(term);
                }
            }
        }

        return finalTerms;
    }

    @Override
    public String toString() {
        return mTerm;
    }
}