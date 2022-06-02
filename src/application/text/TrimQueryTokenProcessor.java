
package application.text;

import org.tartarus.snowball.ext.PorterStemmer;

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
        int leftIndex = 0;
        int rightIndex = term.length() - 1;
        int indexOfFirst = 0;
        int indexOfLast = term.length();
        boolean foundFirst = false;
        boolean foundLast = false;

        // continue while we have not established the range of middle alphanumeric characters
        // and we are still within the length of the string
        while ((!foundFirst || !foundLast) && (leftIndex < term.length() && rightIndex >= 0)) {
            char leftChar = term.charAt(leftIndex);
            char rightChar = term.charAt(rightIndex);
            boolean isLeftAlphaNumeric = Character.isLetter(leftChar) || Character.isDigit(leftChar);
            boolean isRightAlphaNumeric = Character.isLetter(rightChar) || Character.isDigit(rightChar);

            // scan each character at opposite sides of the string and store the range of the first
            // alphanumeric character up to the last alphanumeric character index +1
            if (!foundFirst && isLeftAlphaNumeric) {
                indexOfFirst = leftIndex;
                foundFirst = true;
            }

            if (!foundLast && isRightAlphaNumeric) {
                indexOfLast = rightIndex + 1;
                foundLast = true;
            }

            ++leftIndex;
            --rightIndex;
        }

        // create a new substring using the marked range of indices
        term = term.substring(indexOfFirst, indexOfLast);

        // 2. Remove all apostrophes or quotation marks (single or double quotes) from anywhere in the string.
        term = term.replaceAll("'", "").replaceAll("’", "").replaceAll("\"", "");

        // 4. Convert the token to lowercase.
        term = term.toLowerCase();

        String[] splitTerms = term.split(" ");
        for (String currentTerm : splitTerms) {
            // 5. Stem the token using an implementation of the Porter2 stemmer.
            PorterStemmer stemmer = new PorterStemmer();
            stemmer.setCurrent(currentTerm);
            stemmer.stem();
            terms.add(stemmer.getCurrent());
        }

        return terms;
    }
}
