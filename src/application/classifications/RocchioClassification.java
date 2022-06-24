
package application.classifications;

import application.documents.DocumentWeightScorer;
import application.indexes.Index;
import application.indexes.Posting;
import application.utilities.IndexUtility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RocchioClassification implements Classification {

    private final String rootDirectoryPath;
    // for each directory folder, get their respective indexes / vocabularies and map them to their directory paths
    private final Map<String, Index<String, Posting>> allIndexes;
    private final Map<String, Map<Integer, List<Double>>> allWeightVectors;
    private final Map<String, List<Double>> centroids;

    public RocchioClassification(String inputRootDirectory, Map<String, Index<String, Posting>> inputIndexes) {
        rootDirectoryPath = inputRootDirectory;
        allIndexes = inputIndexes;
        allWeightVectors = new HashMap<>();
        centroids = new HashMap<>();

        calculateWeightVectors();
        calculateCentroids();
    }

    public void calculateWeightVectors() {
        for (Map.Entry<String, Index<String, Posting>> entry : allIndexes.entrySet()) {
            String directoryPath = entry.getKey();
            Index<String, Posting> currentIndex = entry.getValue();
            List<String> vocabulary = currentIndex.getVocabulary();

            Map<Integer, List<Double>> weightVectors = new HashMap<>();
            List<Integer> tftds = new ArrayList<>();

            for (String term : vocabulary) {
                List<Posting> postings = currentIndex.getPostings(term);
                List<Double> currentVector = new ArrayList<>();

                for (Posting currentPosting : postings) {
                    int tftd = currentPosting.getPositions().size();
                    tftds.add(tftd);

                    double wdt = DocumentWeightScorer.calculateWdt(tftd);

                    int documentId = currentPosting.getDocumentId();
                    currentVector.add(wdt);
                    weightVectors.put(documentId, currentVector);
                }
            }

            double ld = DocumentWeightScorer.calculateLd(tftds);
            for (List<Double> vector : weightVectors.values()) {
                vector.forEach(wdt -> wdt /= ld);
            }
            allWeightVectors.put(directoryPath, weightVectors);
        }
    }

    public void calculateCentroids() {

    }

    public List<String> getVocabulary(String directoryPath) {
        return allIndexes.get(directoryPath).getVocabulary();
    }

    @Override
    public String classifyDocument(String directoryPath, int documentId) {
        List<String> subdirectoryPaths = IndexUtility.getAllDirectories(rootDirectoryPath);

        return "";
    }

    @Override
    public List<Double> getVector(String directoryPath, int documentId) {
        return allWeightVectors.get(directoryPath).get(documentId);
    }
}
