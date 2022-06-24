
package application.classifications;

import application.documents.DirectoryCorpus;
import application.documents.Document;
import application.documents.DocumentWeightScorer;
import application.indexes.DiskIndexReader;
import application.indexes.Index;
import application.indexes.Posting;
import application.utilities.IndexUtility;

import javax.print.Doc;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RocchioClassification implements Classification {

    private final String rootDirectoryPath;
    // for each directory folder, get their respective indexes / vocabularies and map them to their directory paths
    private final Map<String, DirectoryCorpus> corpora;
    private final Map<String, Index<String, Posting>> allIndexes;
    // directory map of document ids with their term frequency vectors
    private final Map<String, Map<Integer, List<Double>>> allWeightVectors;
    private final Map<String, List<Double>> centroids;

    public RocchioClassification(String inputRootDirectory, Map<String, DirectoryCorpus> inputCorpora,
                                 Map<String, Index<String, Posting>> inputIndexes) {
        rootDirectoryPath = inputRootDirectory;
        corpora = inputCorpora;
        allIndexes = inputIndexes;
        allWeightVectors = new HashMap<>();
        centroids = new HashMap<>();
        allIndexes.get(rootDirectoryPath).getVocabulary();

        initializeWeightVectors();
        calculateWeightVectors();
        calculateCentroids();
    }

    public void initializeWeightVectors() {
        List<String> vocabulary = allIndexes.get(rootDirectoryPath).getVocabulary();

        // iterate through each corpus index
        for (Map.Entry<String, Index<String, Posting>> entry : allIndexes.entrySet()) {
            String directoryPath = entry.getKey();

            // skip the root directory since it combines all documents
            if (!directoryPath.equals(rootDirectoryPath)) {
                Index<String, Posting> currentIndex = entry.getValue();
                DirectoryCorpus currentCorpus = corpora.get(directoryPath);

                // if the weight vector does not have an entry for the current directory path, add it with an empty map
                allWeightVectors.putIfAbsent(directoryPath, new HashMap<>());
                Map<Integer, List<Double>> currentWeightVector = allWeightVectors.get(directoryPath);

                /* for each document in the current corpora, add its document ID with an empty list initialized
                  with zeros for each term in the vocabulary */
                currentCorpus.getDocuments().forEach(document -> {
                    List<Double> tftds = new ArrayList<>();
                    vocabulary.forEach(term -> tftds.add(0.0));
                    currentWeightVector.put(document.getId(), tftds);
                });
            }
        }
    }

    public void calculateWeightVectors() {
        List<String> vocabulary = allIndexes.get(rootDirectoryPath).getVocabulary();

        for (Map.Entry<String, Index<String, Posting>> entry : allIndexes.entrySet()) {
            String directoryPath = entry.getKey();

            if (!directoryPath.equals(rootDirectoryPath)) {
                Index<String, Posting> currentIndex = entry.getValue();
                DirectoryCorpus currentCorpus = corpora.get(directoryPath);
                Map<Integer, List<Double>> currentWeightVector = allWeightVectors.get(directoryPath);

                // iterate through the vocabulary for each index
                for (String term : vocabulary) {
                    List<Posting> postings = currentIndex.getPostings(term);

                    for (Posting currentPosting : postings) {
                        int documentId = currentPosting.getDocumentId();
                        int tftd = currentPosting.getPositions().size();

                        double wdt = DocumentWeightScorer.calculateWdt(tftd);
                        // update the old accumulating w(d, t) values with the new ones
                        double oldAccumulator = currentWeightVector.get(documentId).get(vocabulary.indexOf(term));
                        currentWeightVector.get(documentId).set(vocabulary.indexOf(term), oldAccumulator + wdt);
                    }
                }

                // divide each individual w(d, t) value by their respective L(d)
                for (Map.Entry<Integer, List<Double>> vectorEntry : currentWeightVector.entrySet()) {
                    int documentId = vectorEntry.getKey();
                    List<Double> currentVector = vectorEntry.getValue();

                    try (RandomAccessFile randomAccessor = new RandomAccessFile(directoryPath +
                            "/index/docWeights.bin", "rw")) {
                        double currentLd = DiskIndexReader.readLd(randomAccessor, documentId);
                        currentVector.replaceAll(accumulator ->
                                accumulator / currentLd);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
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

        return "./hamilton";
    }

    @Override
    public List<Double> getVector(String directoryPath, int documentId) {
        return allWeightVectors.get(directoryPath).get(documentId);
    }
}
