package application.queries;

import java.util.*;
import java.util.stream.Collectors;

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
        /* Program the difference of a NotQuery, by gathering the postings of the composed QueryComponents and
		  subtracting the results. */
        List<Posting> difference = mComponents.get(0).getPostings(index, processor);
        for (int i = 1; i < mComponents.size(); ++i) {
            QueryComponent currentComponent = mComponents.get(i);
            // store current posting for readability
            List<Posting> currentPostings = currentComponent.getPostings(index, processor);

            // remove the current intersections of the new postings
            difference = differencePostings(difference, currentPostings);
        }
        return difference;
    }

    public List<Posting> differencePostings(List<Posting> leftList, List<Posting> rightList){
        List<Posting> difference = new ArrayList<>();
        int leftIndex = 0;
        int rightIndex = 0;
        while (leftIndex < leftList.size() && rightIndex < rightList.size()) {
            // store values for readability
            Posting leftPosting = leftList.get(leftIndex);
            Posting rightPosting = rightList.get(rightIndex);
            int leftDocumentId = leftPosting.getDocumentId();
            int rightDocumentId = rightPosting.getDocumentId();
            if (leftDocumentId == rightDocumentId) {
                ++leftIndex;
                ++rightIndex;
            }
            else if(leftDocumentId< rightDocumentId){
                difference.add(leftPosting);
                ++leftIndex;
            }
            else{
                ++rightIndex;
            }
        }

        return difference;
    }
    @Override
    public String toString() {
        return
                String.join(" ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()));
    }
}
