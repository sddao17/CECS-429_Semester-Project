
package application.queries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import application.Application;
import application.UI.CorpusSelection;
import application.indexes.Index;
import application.indexes.KGramIndex;
import application.indexes.Posting;
import application.text.TokenProcessor;
import application.text.WildcardTokenProcessor;

/**
 * A WildcardLiteral represents a single token containing one or more `*` characters.
 */
public class WildcardLiteral implements QueryComponent {

    private final String mTerm;
    private static CorpusSelection CSelect = new CorpusSelection();

    public WildcardLiteral(String term) {
        mTerm = term;
    }

    public String getTerm() {
        return mTerm;
    }

    @Override
    public List<Posting> getPostings(Index<String, Posting> corpusIndex, TokenProcessor processor) {
        Index<String, String> corpusKGramIndex = Application.getKGramIndex();
        KGramIndex kGramIndex = new KGramIndex();

        // minimally process the original token
        WildcardTokenProcessor wildCardProcessor = new WildcardTokenProcessor();
        String processedTerm = wildCardProcessor.processToken(mTerm).get(0);
        kGramIndex.addToken(processedTerm, 3);

        List<String> candidateTokens = findCandidates(corpusKGramIndex, kGramIndex);

        List<String> finalTokens = postFilter(candidateTokens, processedTerm);

        List<String> finalTerms = new ArrayList<>();
        for (String finalToken : finalTokens) {
            List<String> terms = processor.processToken(finalToken);

            for (String term : terms) {
                if (!finalTerms.contains(term)) {
                    finalTerms.add(term);
                }
            }
        }

        // once we collect all of our final terms, we "OR" the postings to combine them and ignore duplicates
        List<Posting> resultPostings = new ArrayList<>();

        for (String finalTerm : finalTerms) {
            /* note that we add any documents, duplicates included; this is because there can be multiple
              wildcard literals within the same document (and they could be for the same or different term),
              so we must include the postings for every term that we find that match the wildcard pattern */
            resultPostings.addAll(corpusIndex.getPostings(finalTerm));
        }
        Collections.sort(resultPostings);

        if (Application.enabledLogs) {
            System.out.println("--------------------------------------------------------------------------------" +
                    "\nWildcard literal: `" + mTerm + "`" +
                    "\n---> Candidate tokens for `" + mTerm + "`: " + candidateTokens +
                    "\n---> Final tokens for `" + mTerm + "`: " + finalTokens +
                    "\n---> Final terms for `" + mTerm + "`: " + finalTerms +
                    "\n---> `" + mTerm + "` -- " + resultPostings.size() + " posting(s)" +
                    "\n--------------------------------------------------------------------------------");
        }

        return resultPostings;
    }

    private List<String> findCandidates(Index<String, String> corpusKGramIndex, Index<String, String> kGramIndex) {
        List<String> candidateTokens = new ArrayList<>();

        /* find candidate tokens by traversing through the corpus and comparing each of them to our
          generated k-grams; intersect the postings by finding tokens that share the same k-gram patterns */
        for (String corpusToken : corpusKGramIndex.getVocabulary()) {
            ArrayList<String> corpusKGrams = new ArrayList<>(corpusKGramIndex.getPostings(corpusToken));
            boolean candidateMatchesAll = true;

            // intersect terms within their respective vocabularies
            for (String wildcardToken : kGramIndex.getVocabulary()) {
                List<String> wildcardKGrams = kGramIndex.getPostings(wildcardToken);

                // if the token does not contain all wildcard k-grams, it cannot be a candidate
                if (!corpusKGrams.containsAll(wildcardKGrams)) {
                    candidateMatchesAll = false;
                }
            }

            // only add candidates with all the k-grams from the wildcard query
            if (candidateMatchesAll) {
                candidateTokens.add(corpusToken);
            }
        }

        return candidateTokens;
    }

    private List<String> postFilter(List<String> candidateTokens, String processedTerm) {
        List<String> finalTokens = new ArrayList<>();

        // post-filtering step: confirm that the candidate token matches the original pattern
        for (String candidateToken : candidateTokens) {
            // convert our mTerm to a suitable regex matching pattern
            String wildcardRegex = wildcardToRegex(processedTerm);

            // if the candidate matches the original token pattern, we can verify it as a valid term
            if (candidateToken.matches(wildcardRegex)) {
                finalTokens.add(candidateToken);
            }
        }

        return finalTokens;
    }

    public static String wildcardToRegex(String wildcard) {
        int stringLength = wildcard.length();
        StringBuilder regex = new StringBuilder(wildcard.length());

        regex.append('^');
        for (int i = 0; i < stringLength; ++i) {
            char character = wildcard.charAt(i);

            /* for each meta character in the original wildcard String,
              convert it into its escaped pattern matching character */
            switch (character) {
                case '*' -> regex.append(".*");
                case '?' -> regex.append(".");
                case '(', ')', '[', ']', '$', '^', '.', '{', '}', '|', '\\' ->
                        regex.append("\\").append(character);
                default -> regex.append(character);
            }
        }
        regex.append('$');

        return(regex.toString());
    }

    @Override
    public String toString() {
        return mTerm;
    }
}
