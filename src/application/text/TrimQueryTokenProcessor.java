
package application.text;

import java.util.ArrayList;

/**
 * A TrimQueryTokenProcessor creates terms from tokens by: removing all non-alphanumeric characters from the
 * beginning and end of the token, stripping all apostrophes and quotation marks, converting the token to lowercase,
 * and then stemming it.
 */
public class TrimQueryTokenProcessor implements TokenProcessor {

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

        // 4. Convert the token to lowercase.
        term = term.toLowerCase();

        String[] splitTerms = term.split(" ");
        for (String currentTerm : splitTerms) {
            // 5. Stem the token using an implementation of the Porter2 stemmer.
            TokenStemmer stemmer = new TokenStemmer();
            terms.add(stemmer.processToken(currentTerm).get(0));
        }

        return terms;
    }
}
