package application.indexes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.*;

public class BiwordIndex implements Index<String, Integer>{

    private final Map<String, List<Integer>> biwordIndex;
    private int lastDocID = 0;
    private String lastToken = null;

    public BiwordIndex(){
        biwordIndex = new HashMap<>();

    }
    @Override
    public List<Integer> getPostings(String term) {
        // return an empty list if the term doesn't exist in the map
        if (!biwordIndex.containsKey(term))
            return new ArrayList<>();

        return biwordIndex.get(term);
    }

    @Override
    public List<Integer> getPositionlessPostings(String term) {
        // return an empty list if the term doesn't exist in the map
        if (!biwordIndex.containsKey(term))
            return new ArrayList<>();

        // the biword index does not store positions
        return biwordIndex.get(term);
    }

    @Override
    public List<String> getVocabulary() {
        // remember to return a sorted vocabulary
        List<String> vocabulary = new ArrayList<>(biwordIndex.keySet().stream().toList());
        Collections.sort(vocabulary);

        return vocabulary;
    }

    public void addTerm(String term, int docId){

        if(docId != lastDocID){
            lastToken = term;
            lastDocID = docId;
        }
        else {
            String finalTerm = String.format("%s %s", lastToken, term);
            List<Integer> existingPostings = biwordIndex.get(finalTerm);

            if(existingPostings == null){
                ArrayList<Integer> termDocs = new ArrayList<>();
                termDocs.add(docId);
                biwordIndex.put(finalTerm, termDocs);
            }
            else{
                int lastDocId= existingPostings.get(existingPostings.size()-1);
                if(lastDocId != docId){
                    existingPostings.add(docId);
                }
            }
            lastToken = term;
        }
    }
}