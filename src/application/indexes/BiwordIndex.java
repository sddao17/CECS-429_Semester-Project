
package application.indexes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.*;

public class BiwordIndex implements Index<String, Posting> {

    private final Map<String, List<Posting>> index;
    private int lastDocID = 0;
    private String lastToken = "";

    public BiwordIndex() {
        index = new HashMap<>();
    }

    @Override
    public List<Posting> getPostings(String term) {
        // return an empty list if the term doesn't exist in the map
        if (!index.containsKey(term))
            return new ArrayList<>();

        return index.get(term);
    }

    @Override
    public List<Posting> getPositionlessPostings(String term) {
        // return an empty list if the term doesn't exist in the map
        if (!index.containsKey(term))
            return new ArrayList<>();

        return index.get(term);
    }

    @Override
    public List<String> getVocabulary() {
        // remember to return a sorted vocabulary
        List<String> vocabulary = new ArrayList<>(index.keySet().stream().toList());
        Collections.sort(vocabulary);

        return vocabulary;
    }


    public void addTerm(String term, int docId) {
        //if the docID is not the previous doc's ID, then update
        if (docId != lastDocID) {
            lastToken = term;
            lastDocID = docId;
        }
        else {
            //format the term to be inputted into the index
            String finalTerm = String.format("%s %s", lastToken, term);
            List<Posting> existingPostings = index.get(finalTerm);
            //term doesn't exist in the vocabulary yet, so will now need to add it.
            if (existingPostings == null) {
                ArrayList<Posting> newPostings = new ArrayList<>(){{
                    add(new Posting(docId, new ArrayList<>()));
                }};
                index.put(finalTerm, newPostings);

            } else {
                //get the last index of the existing postings
                int latestIndex = existingPostings.size() - 1;
                int latestDocumentId = existingPostings.get(latestIndex).getDocumentId();

                //if the document ID is not in the index, then add the doc ID to the term
                if (latestDocumentId != docId) {
                    existingPostings.add(new Posting(docId, new ArrayList<>()));
                }

            }
            lastToken = term;
        }
    }


}