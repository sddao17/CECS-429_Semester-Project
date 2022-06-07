
package application.text;

import java.util.ArrayList;
import java.util.List;

/**
 * A TrimQueryTokenProcessor creates terms from tokens by: removing all non-alphanumeric characters from the
 * beginning and end of the token, stripping all apostrophes and quotation marks, converting the token to lowercase,
 * and then stemming it.
 * @see TokenProcessor
 */
public class QueryTokenProcessor extends TokenProcessor {

    @Override
    public List<String> processToken(String token) {
        List<String> terms = new ArrayList<>();
        /* To normalize a token into a term, perform these steps in order:
          1. Remove all non-alphanumeric characters from the beginning and end of the token, but not the middle.
          (a) Example: Hello. becomes Hello ; 192.168.1.1 remains unchanged. */
        token = trimNonAlphanumeric(token);

        // 2. Remove all apostrophes or quotation marks (single or double quotes) from anywhere in the string.
        token = removeQuotes(token);

        // skip 3 (splitting token on hyphens)

        // 4. Convert the token to lowercase.
        token = convertToLowercase(token);

        String[] splitTokens = token.split(" ");
        for (String currentToken : splitTokens) {
            // 5. Stem the token using an implementation of the Porter2 stemmer.
            terms.add(stem(currentToken));
        }

        return terms;
    }
}
