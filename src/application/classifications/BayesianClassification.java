
package application.classifications;

import application.documents.DirectoryCorpus;
import application.documents.Document;
import application.indexes.DiskIndexReader;
import application.indexes.Index;
import application.indexes.Posting;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class BayesianClassification implements TextClassification {

    private final String rootDirectoryPath;
    // for each directory folder, get their respective indexes / vocabularies and map them to their directory paths
    private final Map<String, DirectoryCorpus> corpora;
    private final Map<String, Index<String, Posting>> allIndexes;
    // directory map of document ids with their term frequency vectors
    private final Map<String, Map<String, int[][]>> vocabularyTables;
    private final Map<String, Map<String, Double>> mutualInfo;
    private final Map<String, Map<String, Double>> classifiers;

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
        mutualInfo = new HashMap<>();
        classifiers = new HashMap<>();

        initializeVectors();
        calculateVocabTables();
        storeMutualInfo();
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
    private void calculateVocabTables() {
        List<String> vocabulary = allIndexes.get(rootDirectoryPath).getVocabulary();

        for (Map.Entry<String, Index<String, Posting>> entry : allIndexes.entrySet()) {
            String directoryPath = entry.getKey();

            // skip the root directory, since it contains all documents of all directories
            if (!directoryPath.equals(rootDirectoryPath) && !directoryPath.endsWith("/disputed")) {
                Map<String, int[][]> currentTermTable = vocabularyTables.get(directoryPath);

                // iterate through the vocabulary for each index
                for (String term : vocabulary) {
                    insertTableValues(currentTermTable, directoryPath, term);
                }
            }
        }
    }

    private void insertTableValues(Map<String, int[][]> termTable, String currentDirectory, String term) {
        for (Map.Entry<String, Index<String, Posting>> entry : allIndexes.entrySet()) {
            String directoryPath = entry.getKey();

            // skip the root directory, since it contains all documents of all directories
            if (!directoryPath.equals(rootDirectoryPath) && !directoryPath.endsWith("/disputed")) {
                Index<String, Posting> currentIndex = entry.getValue();
                DirectoryCorpus currentCorpus = corpora.get(directoryPath);
                List<Posting> postings = currentIndex.getPositionlessPostings(term);

                // if checking within the same class, update Nx1 values; update Nx0 values otherwise
                if (directoryPath.equals(currentDirectory)) {
                    // setting N11
                    termTable.get(term)[1][1] = postings.size();
                    // setting N01
                    termTable.get(term)[0][1] = currentCorpus.getCorpusSize() - postings.size();
                } else {
                    // updating N10
                    termTable.get(term)[1][0] += postings.size();
                    // updating N00
                    termTable.get(term)[0][0] += currentCorpus.getCorpusSize() - postings.size();
                }
                //System.out.println(term + " " + termTable.get(term)[1][1] + " " + termTable.get(term)[0][1] + " " +
                //        termTable.get(term)[1][0] + " " + termTable.get(term)[0][0]);
            }
        }
    }

    private void storeMutualInfo() {
        for (Map.Entry<String, Map<String, int[][]>> entry : vocabularyTables.entrySet()) {
            String directoryPath = entry.getKey();
            Map<String, int[][]> currentVocabTable = entry.getValue();
            List<String> terms = currentVocabTable.keySet().stream().toList();
            mutualInfo.put(directoryPath, new HashMap<>());

            for (String term : terms) {
                Map<String, Double> currentVector = mutualInfo.get(directoryPath);
                int[][] table = currentVocabTable.get(term);
                double result = calculateMutualInfo(table[1][1], table[1][0],table[0][1], table[0][0]);
                if (Double.isNaN(result)) {
                    result = 0;
                }

                currentVector.put(term, result);
            }
        }
    }

    public List<Map.Entry<String, Double>> getTopDiscriminating(int k) {
        // add all discriminating terms from all relevant classes to a priority queue
        PriorityQueue<Map.Entry<String, Double>> priorityQueue = new PriorityQueue<>(
                Map.Entry.comparingByValue(Comparator.reverseOrder()));

        for (String directoryPath : corpora.keySet()) {
            // skip the root and disputed directory
            if (!directoryPath.equals(rootDirectoryPath) && !directoryPath.endsWith("/disputed")) {
                Map<String, Double> currentEntry = mutualInfo.get(directoryPath);
                priorityQueue.addAll(currentEntry.entrySet());
            }
        }
        // error handling: if the requested number of results exceeds the max, set it to the max
        k = Math.min(k, priorityQueue.size());
        List<Map.Entry<String, Double>> rankedEntries = new ArrayList<>();
        List<String> addedTerms = new ArrayList<>();

        for (int i = 0; i < k; ++i) {
            Map.Entry<String, Double> nextEntry = priorityQueue.poll();
            assert nextEntry != null;
            String nextTerm = nextEntry.getKey();

            if (!addedTerms.contains(nextTerm)) {
                rankedEntries.add(nextEntry);
                addedTerms.add(nextTerm);
            } else {
                --i;
            }
        }

        return rankedEntries;
    }

    public void storeClassifiers(String directoryPath, List<Double> ptics) {
        List<String> vocabulary = allIndexes.get(rootDirectoryPath).getVocabulary();

        classifiers.put(directoryPath, new HashMap<>());
        Map<String, Double> currentClassifier = classifiers.get(directoryPath);

        for (int i = 0 ; i < vocabulary.size(); ++i) {
            currentClassifier.put(vocabulary.get(i), ptics.get(i));
        }
    }

    public Map<String, Double> getCmaps(String directoryPath, int documentId) {
        Map<String, Double> cmaps = new HashMap<>();
        List<String> vocabulary = getTermsInDocument(directoryPath, documentId);
        int rootCorpusSize = corpora.get(rootDirectoryPath).getCorpusSize();
        int disputedCorpusSize = corpora.get(directoryPath).getCorpusSize();

        for (String currentDirectoryPath : corpora.keySet()) {
            double sum = 0;
            // skip the root and disputed directory
            if (!currentDirectoryPath.equals(rootDirectoryPath) && !currentDirectoryPath.endsWith("/disputed")) {
                for (String term : vocabulary) {
                    sum += Math.log(classifiers.get(currentDirectoryPath).get(term));
                }

                // p(c) = number of documents in class `c` / total number of documents
                double pc = (double) corpora.get(currentDirectoryPath).getCorpusSize() /
                        (rootCorpusSize - disputedCorpusSize);
                sum = calculateCmap(pc, sum);
                //System.out.println(currentDirectoryPath + ": " + pc + ", " + sum);
                cmaps.put(currentDirectoryPath, sum);
            }
        }

        return cmaps;
    }

    private List<String> getTermsInDocument(String directoryPath, int documentId) {
        List<String> documentTerms = new ArrayList<>();
        List<String> vocabulary = allIndexes.get(rootDirectoryPath).getVocabulary();
        Index<String, Posting> index = allIndexes.get(directoryPath);

        for (String term : vocabulary) {
            List<Posting> postings = index.getPostings(term);

            if (postingContainsDocumentId(directoryPath, postings, documentId)) {
                documentTerms.add(term);
            }
        }
        return documentTerms;
    }

    private boolean postingContainsDocumentId(String directoryPath, List<Posting> postings, int documentId) {
        DirectoryCorpus corpus = corpora.get(directoryPath);
        for (Posting currentPosting: postings) {
            Document currentDocument = corpus.getDocument(currentPosting.getDocumentId());
            if (currentDocument != null && currentDocument.getId() == documentId) {
                return true;
            }
        }
        return false;
    }

    /**
     * Classifies the document using Rocchio Classification (according to the centroid of its closest class).
     * @param directoryPath the path of the subdirectory to the document
     * @param documentId the document ID of the document
     * @return the classification of the document in the form of <code>(subdirectory, distance)<code/>
     */
    @Override
    public Map.Entry<String, Double> classifyDocument(String directoryPath, int documentId) {
        Map<String, Double> cmaps = getCmaps(directoryPath, documentId);

        // once all the distances are calculated, return the directory of the lowest distance
        PriorityQueue<Map.Entry<String, Double>> priorityQueue = new PriorityQueue<>(
                Map.Entry.comparingByValue(Comparator.reverseOrder()));
        priorityQueue.addAll(cmaps.entrySet());

        return priorityQueue.poll();
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


    /**
     * Calculates the probability of a term appearing in a class; uses Laplace Smoothing to ensure new terms
     * being inserted into the set do not have a probability of zero.
     * @param ftc the number of documents that the term `t` appears in the training set
     * @param trainingSetFtc the number of documents in the training set
     * @param tSize the size of the discriminating terms set `T*`
     */
    public static double calculatePtic(int ftc, int trainingSetFtc, int tSize) {
        return ( (double) (ftc + 1) / (trainingSetFtc + tSize) );
    }

    /**
     * Calculates the classifier of a document given `p(c)` (the probability that the document belongs to the class)
     * and the list of `p(ti | c)` (the probability of the terms appearing in the class); we use logarithms to prevent
     * floating point underflow.
     * @param pc the probability that the document belongs to the class
     * @param classifierSum the probabilities of the terms appearing in the class
     * @return the classifier of the document
     */
    public static double calculateCmap(double pc, double classifierSum) {
        return (Math.log(pc) + classifierSum);
    }
}
