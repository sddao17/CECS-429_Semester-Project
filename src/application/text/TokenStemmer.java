
package application.text;

import org.tartarus.snowball.ext.PorterStemmer;

import java.util.ArrayList;

public class TokenStemmer implements TokenProcessor {

    @Override
    public ArrayList<String> processToken(String token) {
        // Stem the token using an implementation of the Porter2 stemmer.
        PorterStemmer stemmer = new PorterStemmer();
        stemmer.setCurrent(token);
        stemmer.stem();

        return new ArrayList<>(){{add(stemmer.getCurrent());}};
    }
}
