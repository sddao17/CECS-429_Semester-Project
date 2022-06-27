
package application.classifications;

import application.documents.DirectoryCorpus;
import application.documents.Document;
import application.documents.DocumentWeightScorer;
import application.indexes.DiskIndexReader;
import application.indexes.Index;
import application.indexes.Posting;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

public class KnnClassification implements TextClassification {
    private final String rootDirectoryPath;
    // for each directory folder, get their respective indexes / vocabularies and map them to their directory paths
    private final Map<String, DirectoryCorpus> corpora;
    private final Map<String, Index<String, Posting>> allIndexes;
    // directory map of document ids with their term frequency vectors
    private final Map<String, Map<Integer, Map<String, Double>>> allWeightVectors;

    public KnnClassification(String inputRootDirectory, Map<String, DirectoryCorpus> inputCorpora,
                                 Map<String, Index<String, Posting>> inputIndexes) {
        rootDirectoryPath = inputRootDirectory;
        corpora = inputCorpora;
        allIndexes = inputIndexes;
        allWeightVectors = new HashMap<>();


        initializeVectors();
        calculateWeightVectors();

    }
    /**
     * Initializes vectors with empty maps / lists for future access when calculating weights and centroids.
     */
    private void initializeVectors() {
        // get the vocabulary of all directories, effectively combining all distinct vocabulary terms
        List<String> vocabulary = allIndexes.get(rootDirectoryPath).getVocabulary();

        // iterate through each corpus index's directories
        for (String directoryPath : allIndexes.keySet()) {
            // skip the root directory since it combines all documents
            if (!directoryPath.equals(rootDirectoryPath)) {
                DirectoryCorpus currentCorpus = corpora.get(directoryPath);

                // if the weight vector does not have an entry for the current directory path, add it with an empty map
                allWeightVectors.putIfAbsent(directoryPath, new HashMap<>());
                Map<Integer, Map<String, Double>> currentWeightVector = allWeightVectors.get(directoryPath);

                /* for each document in the current corpus, add its document ID with an empty list initialized
                  with zeros for each term in the vocabulary */
                for (Document document : currentCorpus.getDocuments()) {
                    int documentId = document.getId();
                    Map<String, Double> currentWeights = new LinkedHashMap<>();
                    currentWeightVector.put(documentId, currentWeights);

                    for (String term : vocabulary) {
                        currentWeights.put(term, 0.0);
                    }
                }
            }
        }
    }
    /**
     * Calculates the document weight vectors and uses the total vocabulary set as the vector length for each document
     * weight. Skips calculating the document weights for the root directory since it is irrelevant to our information
     * need, and allows us to avoid making unnecessary calculations.
     */
    private void calculateWeightVectors() {
        List<String> vocabulary = allIndexes.get(rootDirectoryPath).getVocabulary();

        for (Map.Entry<String, Index<String, Posting>> entry : allIndexes.entrySet()) {
            String directoryPath = entry.getKey();

            // skip the root directory, since it contains all documents of all directories
            if (!directoryPath.equals(rootDirectoryPath)) {
                Index<String, Posting> currentIndex = entry.getValue();
                Map<Integer, Map<String, Double>> currentWeightVector = allWeightVectors.get(directoryPath);

                // iterate through the vocabulary for each index
                for (String term : vocabulary) {
                    List<Posting> postings = currentIndex.getPostings(term);

                    // for each posting, calculate the w(d, t) values and accumulate them into our weight vectors
                    for (Posting currentPosting : postings) {
                        int documentId = currentPosting.getDocumentId();
                        int tftd = currentPosting.getPositions().size();

                        // update the old accumulating w(d, t) values with the new ones
                        double wdt = DocumentWeightScorer.calculateWdt(tftd);
                        double oldAccumulator = currentWeightVector.get(documentId).get(term);

                        currentWeightVector.get(documentId).replace(term, oldAccumulator + wdt);
                    }
                }

                // divide each individual w(d, t) value by their respective L(d)
                for (Map.Entry<Integer, Map<String, Double>> vectorEntry : currentWeightVector.entrySet()) {
                    int documentId = vectorEntry.getKey();
                    Map<String, Double> currentVector = vectorEntry.getValue();

                    try (RandomAccessFile randomAccessor = new RandomAccessFile(directoryPath +
                            "/index/docWeights.bin", "rw")) {
                        double currentLd = DiskIndexReader.readLd(randomAccessor, documentId);

                        currentVector.replaceAll((term, accumulator) -> accumulator / currentLd);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Calculates the Euclidean distance between two sets of points, where
     * <code>|x, y| = sqrt( sum of all( (ys - xs)^2 ) )</code>.
     * @param xs the list of points represented as mathematical x's, ie. x1, x2, x3, ...
     * @param ys the list of points represented as mathematical y's, ie. y1, y2, y3, ...
     * @return the Euclidean distance between the two sets of points
     */
    public static double calculateDistance(List<Double> xs, List<Double> ys) {
        double sum = 0;

        // |x, y| = sqrt( sum of all( (ys - xs)^2 ) )
        for (int i = 0; i < xs.size(); ++i) {
            sum += Math.pow((ys.get(i) - xs.get(i)), 2);
        }

        return Math.sqrt(sum);
    }
    @Override
    public Map.Entry<String, Double> classifyDocument(String directoryPath, int documentId) {
        return null;
    }

    @Override
    public List<String> getVocabulary(String directoryPath) {
        return null;
    }
}