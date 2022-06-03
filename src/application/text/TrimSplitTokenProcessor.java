
package application.text;

import org.tartarus.snowball.ext.PorterStemmer;

import java.util.ArrayList;

/**
 * A TrimSplitTokenProcessor creates terms from tokens by: removing all non-alphanumeric characters from the
 * beginning and end of the token, stripping all apostrophes and quotation marks, parsing hyphenated words
 * by including a unified and split version of the words, converting the token to lowercase, and then stemming it.
 */
public class TrimSplitTokenProcessor implements TokenProcessor {

    @Override
    public ArrayList<String> processToken(String token) {
        ArrayList<String> terms = new ArrayList<>();
        String term = token;
        // TODO:
        // To normalize a token into a term, perform these steps in order:
        // 1. Remove all non-alphanumeric characters from the beginning and end of the token, but not the middle.
        // (a) Example: Hello. becomes Hello ; 192.168.1.1 remains unchanged.
        term = term.replaceAll("^[^a-zA-Z\\d\\s]+|[^a-zA-Z\\d\\s]+$", "");

        // 2. Remove all apostrophes or quotation marks (single or double quotes) from anywhere in the string.
        term = term.replaceAll("'", "").replaceAll("’", "").replaceAll("\"", "");

        // 3. For hyphens in words, do both:
        // (a) Remove the hyphens from the token and then proceed with the modified token;
        String[] splitTerms = term.split("-");

        // (b) Split the original hyphenated token into multiple tokens without a hyphen,
        //     and proceed with all split tokens.
        StringBuilder combinedTerms = new StringBuilder();

        for (String currentTerm : splitTerms) {
            // 4. Convert the token to lowercase.
            currentTerm = currentTerm.toLowerCase();

            // 5. Stem the token using an implementation of the Porter2 stemmer.
            TokenStemmer stemmer = new TokenStemmer();
            terms.add(stemmer.processToken(currentTerm).get(0));
            combinedTerms.append(currentTerm);
        }

        // add all terms in a single string if any hyphen exists
        if (splitTerms.length > 1) {
            terms.add(combinedTerms.toString());
        }

        return terms;
    }
}
