
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

        initializeVectors();
        calculateWeightVectors();
        calculateCentroids();
    }

    public void initializeVectors() {
        List<String> vocabulary = allIndexes.get(rootDirectoryPath).getVocabulary();

        // iterate through each corpus index
        for (Map.Entry<String, Index<String, Posting>> entry : allIndexes.entrySet()) {
            String directoryPath = entry.getKey();

            // skip the root directory since it combines all documents
            if (!directoryPath.equals(rootDirectoryPath)) {
                DirectoryCorpus currentCorpus = corpora.get(directoryPath);

                // if the weight vector does not have an entry for the current directory path, add it with an empty map
                allWeightVectors.putIfAbsent(directoryPath, new HashMap<>());
                Map<Integer, List<Double>> currentWeightVector = allWeightVectors.get(directoryPath);

                /* for each document in the current corpus, add its document ID with an empty list initialized
                  with zeros for each term in the vocabulary */
                currentCorpus.getDocuments().forEach(document -> {
                    List<Double> tftds = new ArrayList<>();
                    vocabulary.forEach(term -> tftds.add(0.0));
                    currentWeightVector.put(document.getId(), tftds);
                });

                // initialize the centroids with empty lists initialized with zeros for each term in the vocabulary
                List<Double> zeros = new ArrayList<>();
                vocabulary.forEach(term -> zeros.add(0.0));
                centroids.put(directoryPath, zeros);
            }
        }
    }

    public void calculateWeightVectors() {
        List<String> vocabulary = allIndexes.get(rootDirectoryPath).getVocabulary();

        for (Map.Entry<String, Index<String, Posting>> entry : allIndexes.entrySet()) {
            String directoryPath = entry.getKey();

            // skip the root directory, since it contains all documents of all directories
            if (!directoryPath.equals(rootDirectoryPath)) {
                Index<String, Posting> currentIndex = entry.getValue();
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
                        currentVector.replaceAll(accumulator -> accumulator / currentLd);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void calculateCentroids() {
        for (String directoryPath : allIndexes.keySet()) {
            // skip the root directory, since it contains all documents of all directories
            if (!directoryPath.equals(rootDirectoryPath)) {
                DirectoryCorpus currentCorpus = corpora.get(directoryPath);
                Map<Integer, List<Double>> currentWeightVector = allWeightVectors.get(directoryPath);
                List<Double> currentCentroid = centroids.get(directoryPath);

                // we only care about the vectors themselves, not the document IDs that they're mapped to
                for (List<Double> currentVector : currentWeightVector.values()) {
                    for (int i = 0; i < currentVector.size(); ++i) {
                        double oldAccumulator = currentCentroid.get(i);
                        double newWeight = currentVector.get(i);

                        currentCentroid.set(i, oldAccumulator + newWeight);
                    }
                }

                // divide each centroid value by the total number of documents in the set
                currentCentroid.replaceAll(value -> value / currentCorpus.getCorpusSize());
            }
        }
    }

    public static double calculateDistance(List<Double> xs, List<Double> ys) {
        double sum = 0;

        // |x, y| = sqrt( sum of all ( (ys - xs)^2 ) )
        for (int i = 0; i < xs.size(); ++i) {
            sum += Math.pow((xs.get(i) - ys.get(i)), 2);
        }

        return Math.sqrt(sum);
    }

    @Override
    public Map.Entry<String, Double> classifyDocument(String directoryPath, int documentId) {
        Map<String, Double> candidateDistances = getCandidateDistances(directoryPath, documentId);

        // once all the distances are calculated, return the directory of the lowest distance
        PriorityQueue<Map.Entry<String, Double>> priorityQueue = new PriorityQueue<>(Map.Entry.comparingByValue());
        priorityQueue.addAll(candidateDistances.entrySet());

        return priorityQueue.poll();
    }

    public List<Map.Entry<String, Double>> classifyDocuments(String directoryPath) {
        List<Map.Entry<String, Double>> classifications = new ArrayList<>();
        DirectoryCorpus corpus = corpora.get(directoryPath);

        for (Document document : corpus.getDocuments()) {
            classifications.add(classifyDocument(directoryPath, document.getId()));
        }

        return classifications;
    }

    public Map<String, Double> getCandidateDistances(String directoryPath, int documentId) {
        Map<String, Double> candidateDistances = new HashMap<>();
        List<Double> weightVector = allWeightVectors.get(directoryPath).get(documentId);

        for (String currentDirectory : allIndexes.keySet()) {
            // skip the root directory, since it contains all documents of all directories
            if (!currentDirectory.endsWith("/disputed") && !currentDirectory.equals(rootDirectoryPath)) {
                List<Double> currentCentroid = centroids.get(currentDirectory);

                candidateDistances.put(currentDirectory, calculateDistance(weightVector, currentCentroid));
            }
        }

        return candidateDistances;
    }

    public List<Double> getCentroid(String directoryPath) {
        return centroids.get(directoryPath);
    }

    @Override
    public List<String> getVocabulary(String directoryPath) {
        return allIndexes.get(directoryPath).getVocabulary();
    }

    @Override
    public List<Double> getVector(String directoryPath, int documentId) {
        return allWeightVectors.get(directoryPath).get(documentId);
    }
}
