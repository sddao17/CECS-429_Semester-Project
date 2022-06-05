
package application.text;

import java.util.ArrayList;

/**
 * A BasicTokenProcessor creates terms from tokens by removing all non-alphanumeric characters from the token, and
 * converting it to all lowercase.
 */
public class BasicTokenProcessor extends TokenProcessor {

	@Override
	public ArrayList<String> processToken(String token) {
		return new ArrayList<>(){{add(token.replaceAll("\\W", "").toLowerCase());}};
	}
}
