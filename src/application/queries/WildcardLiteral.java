
package application.queries;

import java.util.List;

import application.indexes.Index;
import application.indexes.Posting;
import application.text.TrimQueryTokenProcessor;

/**
 * A WildcardLiteral represents a single token containing one or more * characters.
 */
public class WildcardLiteral implements QueryComponent {
    private final String mTerm;

    public WildcardLiteral(String term) {
        // Somehow incorporate a TokenProcessor into the getPostings call sequence.
        TrimQueryTokenProcessor processor = new TrimQueryTokenProcessor();

        mTerm = processor.processToken(term).get(0);
    }

    public String getTerm() {
        return mTerm;
    }

    @Override
    public List<Posting> getPostings(Index index) {
        return index.getPostings(mTerm);
    }

    @Override
    public String toString() {
        return mTerm;
    }
}
