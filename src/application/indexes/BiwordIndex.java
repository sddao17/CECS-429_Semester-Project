package application.indexes;

import org.apache.jdbm.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.*;

public class BiwordIndex implements Index<String, Posting> {

    private final Map<String, List<Posting>> biwordIndex;
    private BTree<String, Integer> bTree;
    private int lastDocID = 0;
    private String lastToken = null;
    private RandomAccessFile randomAccessPosting;
    private String pathToBTreeBin;

    public BiwordIndex() {
        biwordIndex = new HashMap<>();
        bTree = new BTree<>();
    }

    public void initializeBTree(String newPathToBtreeBin){
        pathToBTreeBin = newPathToBtreeBin;
    }

    @Override
    public List<Posting> getPostings(String term) {
        List<Posting> resultPostings = new ArrayList<>();
        try {
            // retrieve the byte position value for the term key within the B+ Tree
            int bytePosition = bTree.get(term);
            // jump to the offset containing the term's postings
            randomAccessPosting.seek(bytePosition);
            // the current int value at the offset is the size of the postings list
            int postingsSize = randomAccessPosting.readInt();
            int latestDocumentId = 0;

            // iterate through all postings for the term
            for (int i = 0; i < postingsSize; ++i) {
                ArrayList<Integer> positions = new ArrayList<>();
                // first document ID is as-is; the rest are gaps
                int currentDocumentId = randomAccessPosting.readInt() + latestDocumentId;
                latestDocumentId = currentDocumentId - latestDocumentId;
                //int positionsSize = randomAccessPosting.readInt();
                //int latestPosition = 0;

                //for (int j = 0; j < positionsSize; ++j) {
                // first position is as-is; the rest are gaps
                // int currentPosition = randomAccessPosting.readInt() + latestPosition;
                // positions.add(currentPosition);
                //latestPosition = currentPosition - latestPosition;
                //}
                Posting newPosting = new Posting(currentDocumentId, null);
                resultPostings.add(newPosting);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // bTree.get(term) returning null means that the term does not exist in the vocabulary
            return new ArrayList<>();
        }

        return resultPostings;
    }

    @Override
    public List<Posting> getPositionlessPostings(String term) {
        List<Posting> resultPostings = new ArrayList<>();

        try {
            // retrieve the byte position value for the term key within the B+ Tree
            int bytePosition = bTree.get(term);
            // jump to the offset containing the term's postings
            randomAccessPosting.seek(bytePosition);
            // the current int value at the offset is the size of the postings list
            int postingsSize = randomAccessPosting.readInt();
            int latestDocumentId = 0;

            // iterate through all postings for the term
            for (int i = 0; i < postingsSize; ++i) {
                ArrayList<Integer> positions = new ArrayList<>();
                // first document ID is as-is; the rest are gaps
                int currentDocumentId = randomAccessPosting.readInt() + latestDocumentId;
                latestDocumentId = currentDocumentId - latestDocumentId;
                int positionsSize = randomAccessPosting.readInt();
                // skip the other position bytes
                randomAccessPosting.skipBytes(positionsSize * Integer.BYTES);

                // add empty positions
                for (int j = 0; j < positionsSize; ++j) {
                    positions.add(0);
                }

                Posting newPosting = new Posting(currentDocumentId, positions);
                resultPostings.add(newPosting);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // bTree.get(term) returning null means that the term does not exist in the vocabulary
            return new ArrayList<>();
        }

        return resultPostings;
    }

    @Override
    public List<String> getVocabulary() {
        // remember to return a sorted vocabulary
        List<String> vocabulary = new ArrayList<>(biwordIndex.keySet().stream().toList());
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
            List<Posting> existingPostings = biwordIndex.get(finalTerm);
            //term doesn't exist in the vocabulary yet, so will now need to add it.
            if (existingPostings == null) {
                ArrayList<Posting> newPostings = new ArrayList<>(){
                    {add(new Posting(docId, new ArrayList<>(){{}}));}};
                biwordIndex.put(finalTerm, newPostings);

            } else {
                //get the last index of the existing postings
                int latestIndex = existingPostings.size() - 1;
                int latestDocumentId = existingPostings.get(latestIndex).getDocumentId();

                //if the document ID is not in the index, then add the doc ID to the term
                if (latestDocumentId != docId) {
                    existingPostings.add(new Posting(docId, new ArrayList<>(){{}}));
                }

            }
            lastToken = term;
        }
    }


}