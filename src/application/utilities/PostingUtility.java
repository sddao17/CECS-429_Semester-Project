
package application.utilities;

import application.indexes.Posting;

import java.util.*;

public class PostingUtility {

    public static Map<String, String> createIndexPathsMap(String directoryString) {
        String pathToIndexDirectory = directoryString + PathSuffix.INDEX_DIRECTORY.getLabel();

        return new HashMap<>() {{
            put("indexDirectory", pathToIndexDirectory);
            put("docWeightsBin", pathToIndexDirectory + PathSuffix.DOC_WEIGHTS_FILE.getLabel());
            put("postingsBin", pathToIndexDirectory + PathSuffix.POSTINGS_FILE.getLabel());
            put("bTreeBin", pathToIndexDirectory + PathSuffix.BTREE_FILE.getLabel());
            put("kGramsBin", pathToIndexDirectory + PathSuffix.KGRAMS_FILE.getLabel());
        }};
    }

    public static List<Posting> getDistinctPostings(List<Posting> postings) {
        List<Posting> distinctPostings = new ArrayList<>();
        List<Integer> distinctDocumentIds = new ArrayList<>();

        for (Posting currentPosting : postings) {
            int currentDocumentId = currentPosting.getDocumentId();

            if (Collections.binarySearch(distinctDocumentIds, currentDocumentId) < 0) {
                distinctPostings.add(currentPosting);
                distinctDocumentIds.add(currentDocumentId);
            }
        }

        return distinctPostings;
    }
}
