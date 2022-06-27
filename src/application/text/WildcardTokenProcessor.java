
package application.text;

import java.util.ArrayList;
import java.util.List;

/**
 * A TrimQueryTokenProcessor creates terms from tokens by: removing all non-alphanumeric characters from the
 * beginning and end of the token, stripping all apostrophes and quotation marks, splitting on hyphens, and
 * converting the token to lowercase.
 * @see TokenProcessor
 */
public class WildcardTokenProcessor extends TokenProcessor {

    @Override
    public ArrayList<String> processToken(String token) {
        ArrayList<String> terms = new ArrayList<>();
        // 1. Perform minimal processing on wildcard tokens.
        token = this.trimNonAlphanumeric(token);

        // 2. Remove all apostrophes or quotation marks (single or double quotes) from anywhere in the string.
        token = removeQuotes(token);

        /* 3. For hyphens in words, do both:
          (a) Remove the hyphens from the token and then proceed with the modified token.
          (b) Split the original hyphenated token into multiple tokens without a hyphen,
          and proceed with all split tokens. */
        List<String> splitTerms = splitOnHyphens(token);

        for (String currentToken : splitTerms) {
            // 4. Convert the token to lowercase.
            currentToken = convertToLowercase(currentToken);

            // skip 5 (stemming)

            terms.add(currentToken);
        }

        return terms;
    }

    @Override
    public String trimNonAlphanumeric(String token) {
        // do minimal processing - keep asterisks
        if (token.length() <= 0) {
            return "";
        }

        int startIndex = 0;
        int endIndex = token.length() - 1;

        while (isNotAlphanumeric(token.charAt(startIndex)) && startIndex < token.length() - 1) {
            ++startIndex;
        }

        while (isNotAlphanumeric(token.charAt(endIndex)) && endIndex > 0) {
            --endIndex;
        }

        if (startIndex >= endIndex) {
            return "";
        }

        return token.substring(startIndex, endIndex + 1);
    }

    public boolean isNotAlphanumeric(char character) {
        return !(Character.isLetterOrDigit(character) | character == '*');
    }
}
