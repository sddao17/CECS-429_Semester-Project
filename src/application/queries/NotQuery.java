package application.queries;

import java.util.*;

import application.indexes.Index;
import application.indexes.Posting;
import application.text.TokenProcessor;

public class NotQuery implements QueryComponent {
    private final List<QueryComponent> mComponents;	// the components of the not query

    public NotQuery(List<QueryComponent> components) {
        mComponents = components;
    }

    @Override
    public List<Posting> getPostings(Index<String, Posting> index, TokenProcessor processor) {
        return null;
    }
}
