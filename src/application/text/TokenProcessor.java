
package application.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A TokenProcessor applies some rules of normalization to a token from a document, and returns a term for that token.
 */
public abstract class TokenProcessor {

	/**
	 * Normalizes a token into a term.
	 */
	public abstract List<String> processToken(String token);

	/**
	 * Returns a substring of the token that trims all non-alphanumeric characters at its beginning and end.
	 * @param token the token to normalize
	 * @return the normalized token
	 */
	public String trimNonAlphanumeric(String token) {
		/* To normalize a token into a term, perform these steps in order:
          1. Remove all non-alphanumeric characters from the beginning and end of the token, but not the middle.
          (a) Example: Hello. becomes Hello ; 192.168.1.1 remains unchanged. */
		if (token.length() == 0 || (token.length() == 1 && isNotAlphanumeric(token.charAt(0)))) {
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

		if (startIndex > endIndex) {
			return "";
		}

		return token.substring(startIndex, endIndex + 1);
	}

	public boolean isNotAlphanumeric(char character) {
		return !Character.isLetterOrDigit(character);
	}

	/**
	 * Returns a parsed token that removes all single and double quotes from the original token.
	 * @param token the token to normalize
	 * @return the normalized token
	 */
	public String removeQuotes(String token) {
		/* To normalize a token into a term, perform these steps in order:
          1. Remove all non-alphanumeric characters from the beginning and end of the token, but not the middle.
          (a) Example: Hello. becomes Hello ; 192.168.1.1 remains unchanged. */
		// 2. Remove all apostrophes or quotation marks (single or double quotes) from anywhere in the string.
		return token.replaceAll("'", "").replaceAll("’", "").replaceAll("\"", "");
	}

	/**
	 * Returns a list of tokens which are substrings of the original hyphenated token.
	 * @param token the token to normalize
	 * @return the normalized list of tokens
	 */
	public List<String> splitOnHyphens(String token) {
		ArrayList<String> tokens = new ArrayList<>();

		/* 3. For hyphens in words, do both:
          (a) Remove the hyphens from the token and then proceed with the modified token. */
		tokens.add(token.replaceAll("-", ""));

        /* (b) Split the original hyphenated token into multiple tokens without a hyphen,
          and proceed with all split tokens. */
		String[] splitTerms = token.split("-");
		Collections.addAll(tokens, splitTerms);
		if (tokens.size() > 1) {
			tokens.remove(token);
		}

		return tokens;
	}

	/**
	 * Returns the token after converting it to lowercase.
	 * @param token the token to normalize
	 * @return the normalized token
	 */
	public String convertToLowercase(String token) {
		// 4. Convert the token to lowercase.
		return token.toLowerCase();
	}

	/**
	 * Returns a stemmed version of the token.
	 * @param token the token to normalize
	 * @return the normalized token
	 */
	public String stem(String token) {
		// 5. Stem the token using an implementation of the Porter2 stemmer.
		TokenStemmer stemmer = new TokenStemmer();
		return stemmer.processToken(token).get(0);
	}
}
