
package application.documents;

import application.Application;
import application.indexes.DiskIndexReader;
import application.indexes.Index;
import application.indexes.Posting;
import application.text.VocabularyTokenProcessor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

/**
 * Calculates document weights and their relative scores for ranked retrieval queries.
 */
public class DocumentWeightScorer {

    public static RandomAccessFile randomAccessor;
    private final Map<Integer, Double> finalAccumulators;

    public DocumentWeightScorer(String filePath) {
        try {
            randomAccessor = new RandomAccessFile(filePath, "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        finalAccumulators = new HashMap<>();
    }

    public static void setRandomAccessor(RandomAccessFile newRandomAccessor) {
        randomAccessor = newRandomAccessor;
    }

    public static void closeRandomAccessor() {
        try {
            randomAccessor.close();

        } catch (IOException | NullPointerException ignored) {}
    }

    public void storeTermAtATimeDocuments(Index<String, Posting> index, String query) {
        VocabularyTokenProcessor processor = new VocabularyTokenProcessor();
        String[] splitTokens = query.split(" ");
        List<String> queryTerms = new ArrayList<>();

        for (String token : splitTokens) {
            queryTerms.add(processor.processToken(token).get(0));
        }

        // implement the "term at a time" algorithm from lecture;
        // 1. For each term t in the query:
        for (String term : queryTerms) {
            // N = total number of documents in the corpus
            int n = Application.getCorpus().getCorpusSize();
            List<Posting> postings = index.getPostings(term);
            // 1a. Calculate w(q,t) = ln(1 + N/df(t)).
            double wqt = calculateWqt(n, postings.size());

            // 1b. For each document d in t's postings list:
            for (Posting currentPosting : postings) {
                // 1 (b, ii). Calculate w(d,t) = 1 + ln(tf(t,d)).
                double wdt = calculateWdt(currentPosting.getPositions().size());

                /* 1 (b, i). Acquire an accumulator value A(d) (the design of this system is up to you).
                   1 (b, iii). Increase A(d) by w(d,t) × w(q,t). */
                accumulate(finalAccumulators, acquireAccumulators(index, term, wdt, wqt));
            }
        };

        // iterate through all document IDs
        for (Map.Entry<Integer, Double> entry : finalAccumulators.entrySet()) {
            int currentDocumentId = entry.getKey();
            double currentAd = entry.getValue();

            // 2. For each non-zero A(d), divide A(d) by L(d), where L(d) is read from the `docWeights.bin` file.
            if (currentAd > 0) {
                double ld = DiskIndexReader.readLdFromBinFile(randomAccessor, currentDocumentId);
                finalAccumulators.replace(currentDocumentId, currentAd / ld);
            }
        }
    }

    public List<Map.Entry<Integer, Double>> getRankedEntries(int k) {
        // error handling: if there are less document IDs than what is requested, instead use the existing size
        if (k > finalAccumulators.size()) {
            k = finalAccumulators.size();
        }

        List<Map.Entry<Integer, Double>> rankedEntries = new ArrayList<>();
        /* 3. Select and return the top K = 10 documents by largest A(d) value.
          (Use a binary heap priority queue to select the largest results; do not sort the accumulators.) */
        Queue<Map.Entry<Integer, Double>> priorityQueue = new PriorityQueue<>(
                Map.Entry.comparingByValue(Comparator.reverseOrder()));
        priorityQueue.addAll(finalAccumulators.entrySet());

        for (int i = 0; i < k; ++i) {
            rankedEntries.add(priorityQueue.poll());
        }

        return rankedEntries;
    }

    public Map<Integer, Double> acquireAccumulators(Index<String, Posting> index, String term,
                                                            double wdt, double wqt) {
        Map<Integer, Double> accumulators = new HashMap<>();

        // implement the accumulator algorithm from lecture
        List<Posting> postings = index.getPostings(term);

        for (Posting posting : postings) {
            int currentDocumentId = posting.getDocumentId();
            double newWeight = (wdt * wqt);

            // if the map doesn't contain the key, add the new key / value pair
            if (!accumulators.containsKey(currentDocumentId)) {
                accumulators.put(currentDocumentId, newWeight);
            } else {
                // update the accumulator
                double oldAccumulator = accumulators.get(currentDocumentId);
                accumulators.replace(currentDocumentId, oldAccumulator + newWeight);
            }
        }

        return accumulators;
    }

    public void accumulate(Map<Integer, Double> finalMap, Map<Integer, Double> newMap) {
        List<Integer> newMapKeys = newMap.keySet().stream().toList();

        for (Integer newMapKey : newMapKeys) {
            double newMapValue = newMap.get(newMapKey);

            // if the map doesn't contain the key, add the new key / value pair
            if (!finalMap.containsKey(newMapKey)) {
                finalMap.put(newMapKey, newMapValue);
            } else {
                // update the accumulator
                double oldFinalMapValue = finalMap.get(newMapKey);
                finalMap.replace(newMapKey, oldFinalMapValue + newMapValue);
            }
        }
    }

    public static double calculateWdt(int tftd) {
        // w(d, t) = 1 + ln(tf(t, d))
        return (1 + Math.log(tftd));
    }

    public static double calculateWqt(int n, int dft) {
        // w(q, t) = 1 + ln(N / df(t))
        return (1 + Math.log((double) n / (double) dft));
    }

    public static double calculateLd(Map<String, Integer> tftds) {
        int sum = 0;

        // L(d) = sqrt( sum of all(w(d, t)^2) )
        for (Integer tftd : tftds.values()) {
            double wdt = calculateWdt(tftd);
            sum += Math.pow(wdt, 2);
        }

        return Math.sqrt(sum);
    }
}
