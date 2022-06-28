
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

public class BayesianClassification implements TextClassification {

    private final String rootDirectoryPath;
    // for each directory folder, get their respective indexes / vocabularies and map them to their directory paths
    private final Map<String, DirectoryCorpus> corpora;
    private final Map<String, Index<String, Posting>> allIndexes;
    // directory map of document ids with their term frequency vectors
    private final Map<String, Map<String, int[][]>> vocabularyTables;

    /**
     * Constructs a Rocchio classification instance of a root directory containing subdirectories.
     * Vectors are initialized with empty maps / lists for access when calculating weights and centroids.
     * @param inputRootDirectory the root directory of all subdirectories
     * @param inputCorpora the corpora of all directories
     * @param inputIndexes the indexes of all directories
     */
    public BayesianClassification(String inputRootDirectory, Map<String, DirectoryCorpus> inputCorpora,
                                 Map<String, Index<String, Posting>> inputIndexes) {
        rootDirectoryPath = inputRootDirectory;
        corpora = inputCorpora;
        allIndexes = inputIndexes;
        vocabularyTables = new HashMap<>();

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
                // if the map does not have an entry for the current directory path, add it with an empty map
                vocabularyTables.putIfAbsent(directoryPath, new HashMap<>());
                Map<String, int[][]> currentTermTable = vocabularyTables.get(directoryPath);

                /* for each term in the vocabulary, add the term mapped to an initialized 2x2 grid representing
                  n11, n10, n01, and n00 for feature selection */
                for (String term : vocabulary) {
                    currentTermTable.putIfAbsent(term, new int[2][2]);
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
                Map<String, int[][]> currentTermTable = vocabularyTables.get(directoryPath);

                // iterate through the vocabulary for each index
                for (String term : vocabulary) {
                    List<Posting> postings = currentIndex.getPositionlessPostings(term);


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

    /**
     * Classifies the document using Rocchio Classification (according to the centroid of its closest class).
     * @param directoryPath the path of the subdirectory to the document
     * @param documentId the document ID of the document
     * @return the classification of the document in the form of <code>(subdirectory, distance)<code/>
     */
    @Override
    public Map.Entry<String, Double> classifyDocument(String directoryPath, int documentId) {
        Map<String, Double> candidateDistances = getCandidateDistances(directoryPath, documentId);

        // once all the distances are calculated, return the directory of the lowest distance
        PriorityQueue<Map.Entry<String, Double>> priorityQueue = new PriorityQueue<>(Map.Entry.comparingByValue());
        priorityQueue.addAll(candidateDistances.entrySet());

        return priorityQueue.poll();
    }

    /**
     * Classifies each document within the set of documents within a subdirectory using Rocchio Classification
     * (according to the centroid of its closest class).
     * @param directoryPath the path of the subdirectory to the document
     * @return the classification of the documents in the form of <code>List<(subdirectory, distance)><code/>
     */
    public List<Map.Entry<String, Double>> classifyDocuments(String directoryPath) {
        List<Map.Entry<String, Double>> classifications = new ArrayList<>();
        DirectoryCorpus corpus = corpora.get(directoryPath);

        for (Document document : corpus.getDocuments()) {
            classifications.add(classifyDocument(directoryPath, document.getId()));
        }

        return classifications;
    }

    /**
     * Calculates the distances from the document to the centroid of each training set and includes them within a Map.
     * @param directoryPath the path of the subdirectory to the document
     * @param documentId the document ID of the document
     * @return the distances from the document to the centroid of each training set within a Map
     */
    public Map<String, Double> getCandidateDistances(String directoryPath, int documentId) {
        Map<String, Double> candidateDistances = new HashMap<>();

        return candidateDistances;
    }

    /**
     * Returns the list of vocabulary terms of the specified directory.
     * @param directoryPath the path of the directory
     * @return the list of vocabulary terms of the directory
     */
    @Override
    public List<String> getVocabulary(String directoryPath) {
        return allIndexes.get(directoryPath).getVocabulary();
    }

    private void getDiscriminatingTerms() {
        Map<String, Map<String, int[][]>> vocabularyTables = new HashMap<>();


    }

    public Map<String, Double> getMutualInfoMap() {


        return new HashMap<>();
    }

    /**
     * Represents the mutual information calculation formula for feature selection.
     * The formula is written as: <code>( (N11 / N) * log2( (N * N11) / (N1x * Nx1) ) +
     * ( (N10 / N) * log2( (N * N10) / (N1x * Nx0) ) + ( (N01 / N) * log2( (N * N01) / (N0x * Nx1) ) +
     * ( (N00 / N) * log2( (N * N00) / (N0x * Nx0) )</code>.
     * @param n11 the set of documents inside the specified class where the term exists
     * @param n10 the set of documents inside all other class where the term exists
     * @param n01 the set of documents inside the specified class where the term does not exist
     * @param n00 the set of documents inside all other classes where the term does not exist
     * @return the mutual information calculation
     */
    public static double calculateMutualInfo(int n11, int n10, int n01, int n00) {
        int n = n11 + n10 + n01 + n00;
        double calc1 = ((double) n11 / n) * calculateLog2( ((double) n * n11) / ((double) (n11 + n10) * (n01 + n11)));
        double calc2 = ((double) n10 / n) * calculateLog2( ((double) n * n10) / ((double) (n11 + n10) * (n10 + n00)));
        double calc3 = ((double) n01 / n) * calculateLog2( ((double) n * n01) / ((double) (n00 + n01) * (n11 + n01)));
        double calc4 = ((double) n00 / n) * calculateLog2( ((double) n * n00) / ((double) (n00 + n01) * (n10 + n00)));

        return (calc1 + calc2 + calc3 + calc4);
    }

    private static double calculateLog2(double num) {
        return (Math.log(num) / Math.log(2));
    }

    public static void main(String[] args) {
        System.out.println(calculateLog2(1024));
        System.out.println(calculateMutualInfo(49, 27652, 141, 774106));
    }
}
