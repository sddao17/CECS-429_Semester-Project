
package application.documents;

import application.Application;
import application.indexes.Index;
import application.indexes.Posting;
import application.text.VocabularyTokenProcessor;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for calculating document weights in ranked retrieval.
 */
public class DocumentWeightCalculator {

    private static RandomAccessFile randomAccessor;

    public static void setRandomAccessFile(RandomAccessFile newRandomAccessor) {
        randomAccessor = newRandomAccessor;
    }

    public static double calculateWdt(int tftd) {
        // w(d, t) = 1 + ln(tf(t, d))
        return (1 + Math.log(tftd));
    }

    public static double calculateWqt(int n, int dft) {
        // w(q, t) = 1 + ln(N / df(t))
        return (1 + Math.log((double) n / (double) dft));
    }

    public static double calculateLd(List<Double> wdts) {
        int sum = 0;

        // L(d) = sqrt( sum of all(w(d, t)^2) )
        for (Double wdt : wdts) {
            sum += Math.pow(wdt, 2);
        }

        return Math.sqrt(sum);
    }

    public static List<Integer> getTermAtATimeDocuments(Index<String, Posting> index, String query, int k) {
        VocabularyTokenProcessor processor = new VocabularyTokenProcessor();
        String[] splitTokens = query.split(" ");
        List<String> queryTerms = new ArrayList<>();

        for (String token : splitTokens) {
            queryTerms.add(processor.processToken(token).get(0));
        }

        Map<Integer, Double> finalAccumulators = new HashMap<>();

        // implement the "term at a time" algorithm from lecture;
        // 1. For each term t in the query:
        for (String term : queryTerms) {
            DirectoryCorpus corpus = Application.getCorpus();
            List<Posting> postings = index.getPostings(term);
            // 1a. Calculate w(q,t) = ln(1 + N/df(t)).
            double wqt = calculateWqt(corpus.getCorpusSize(), postings.size());

            // 1b. For each document d in t's postings list:
            for (Posting currentPosting : postings) {
                // 1 (b, ii). Calculate w(d,t) = 1 + ln(tf(t,d)).
                double wdt = calculateWdt(currentPosting.getPositions().size());

                /* 1 (b, i). Acquire an accumulator value A(d) (the design of this system is up to you).
                   1 (b, iii). Increase A(d) by w(d,t) × w(q,t). */
                accumulate(finalAccumulators, acquireAccumulators(index, term, wdt, wqt));
            }
        }
        List<Integer> accumulatorKeys = finalAccumulators.keySet().stream().toList();

        // iterate through all document Ids
        for (Integer documentId : accumulatorKeys) {
            double currentAd = finalAccumulators.get(documentId);

            // 2. For each non-zero A(d), divide A(d) by L(d), where L(d) is read from the `docWeights.bin` file.
            if (currentAd > 0) {
                double ld = readLdFromBinFile(documentId);
                finalAccumulators.put(documentId, currentAd / ld);
            }
        }

        List<Integer> finalDocumentIds = new ArrayList<>();

        /* 3. Select and return the top K = 10 documents by largest A(d) value.
          (Use a binary heap priority queue to select the largest results; do not sort the accumulators.) */


        return finalDocumentIds;
    }

    public static Map<Integer, Double> acquireAccumulators(Index<String, Posting> index, String term,
                                                            double wdt, double wqt) {
        Map<Integer, Double> accumulators = new HashMap<>();

        // implement the accumulator algorithm from lecture
        List<Posting> postings = index.getPostings(term);

        for (Posting posting : postings) {
            int currentDocumentId = posting.getDocumentId();
            int currentWdt = posting.getPositions().size();
            double newWeight = currentWdt + (wdt * wqt);

            // if the map doesn't contain the key, add the new key / value pair
            if (!accumulators.containsKey(currentDocumentId)) {
                accumulators.put(currentDocumentId, newWeight);
            } else {
                // update the accumulator
                double oldAccumulator = accumulators.get(currentDocumentId);
                accumulators.put(currentDocumentId, oldAccumulator + newWeight);
            }
        }

        return accumulators;
    }

    public static void accumulate(Map<Integer, Double> finalMap, Map<Integer, Double> newMap) {
        List<Integer> newMapKeys = newMap.keySet().stream().toList();

        for (Integer newMapKey : newMapKeys) {
            double newMapValue = newMap.get(newMapKey);

            // if the map doesn't contain the key, add the new key / value pair
            if (!finalMap.containsKey(newMapKey)) {
                finalMap.put(newMapKey, newMapValue);
            } else {
                // update the accumulator
                double oldFinalMapValue = finalMap.get(newMapKey);
                finalMap.put(newMapKey, oldFinalMapValue + newMapValue);
            }
        }
    }

    public static double readLdFromBinFile(int documentId) {
        int bytePosition = documentId * Double.BYTES;
        double documentWeight = 0;

        try {
            randomAccessor.seek(bytePosition);
            documentWeight = randomAccessor.readDouble();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return documentWeight;
    }
}
