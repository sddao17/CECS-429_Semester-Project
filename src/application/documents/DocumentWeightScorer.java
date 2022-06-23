
package application.documents;

import application.Application;
import application.indexes.DiskIndexReader;
import application.indexes.Index;
import application.indexes.KGramIndex;
import application.indexes.Posting;
import application.queries.WildcardLiteral;
import application.text.VocabularyTokenProcessor;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

/**
 * Calculates document weights and their relative scores for ranked retrieval queries.
 */
public class DocumentWeightScorer implements Closeable {

    private static RandomAccessFile randomAccessor;
    private final Map<Integer, Double> finalAccumulators;

    public DocumentWeightScorer(String inputFilePath) {
        finalAccumulators = new HashMap<>();

        try {
            randomAccessor = new RandomAccessFile(inputFilePath, "rw");
        } catch (FileNotFoundException e) {
            System.err.println("The `docWeights.bin` file could not be found.");
        }
    }

    public void storeTermAtATimeDocuments(Index<String, Posting> index, String query) {
        VocabularyTokenProcessor processor = new VocabularyTokenProcessor();
        String[] splitQuery = query.split(" ");
        List<String> queryTerms = new ArrayList<>();

        for (String token : splitQuery) {
            // if the token has a wildcard, allow all vocabulary types that match the pattern to accumulate points
            if (token.contains("*")) {
                accumulateWildcards(index, token);
            } else {
                List<String> splitTerms = processor.processToken(token);

                // error handling - handle empty / fully non-alphanumeric tokens
                if (splitTerms.size() > 0) {
                    queryTerms.add(splitTerms.get(0));
                }
            }
        }

        accumulateTermAtATime(index, queryTerms);
        normalizeAccumulators();
    }

    public void accumulateWildcards(Index<String, Posting> index, String wildcard) {
        KGramIndex kGramIndex = Application.getKGramIndexes().get(Application.getCurrentDirectory() + "/index/kGrams.bin");

        for (String type : kGramIndex.getVocabulary()) {
            if (type.matches(WildcardLiteral.wildcardToRegex(wildcard))) {
                VocabularyTokenProcessor processor = new VocabularyTokenProcessor();
                List<String> terms = processor.processToken(type);

                if (terms.size() > 0) {
                    accumulateTermAtATime(index, new ArrayList<>(){{add(terms.get(0));}});
                }
            }
        }
    }

    public void accumulateTermAtATime(Index<String, Posting> index, List<String> queryTerms) {
        DirectoryCorpus corpus = Application.getCorpora().get(Application.getCurrentDirectory());

        // implement the "term at a time" algorithm from lecture;
        // 1. For each term t in the query:
        for (String term : queryTerms) {
            // N = total number of documents in the corpus
            int n = corpus.getCorpusSize();
            List<Posting> postings = index.getPositionlessPostings(term);
            // df(t) = number of documents the term has appeared in
            int dft = postings.size();

            // 1a. Calculate w(q,t) = ln(1 + N/df(t)).
            double wqt = calculateWqt(n, dft);

            // debugging log
            if (Application.enabledLogs) {
                System.out.println("--------------------------------------------------------------------------------" +
                        "\n`" + term + "`" +
                        "\n---> df(t) -- " + dft +
                        "\n---> w(q, t) -- " + wqt + "\n");
            }

            // 1b. For each document d in t's postings list:
            for (Posting currentPosting : postings) {
                // 1 (b, i). Acquire an accumulator value A(d) (the design of this system is up to you).
                acquireAccumulator(currentPosting, wqt);
            }

            // debugging log
            if (Application.enabledLogs) {
                System.out.println("--------------------------------------------------------------------------------");
            }
        }
    }

    public void acquireAccumulator(Posting posting, double wqt) {
        int documentId = posting.getDocumentId();
        int tftd = posting.getPositions().size();

        // 1 (b, ii). Calculate w(d,t) = 1 + ln(tf(t,d)).
        double wdt = calculateWdt(tftd);

        // debugging log
        if (Application.enabledLogs) {
            DirectoryCorpus corpus = Application.getCorpora().get(Application.getCurrentDirectory());
            System.out.println(
                    corpus.getDocument(documentId).getTitle() + " (ID: " + documentId + ")" +
                            "\n---> tf(t, d) -- " + tftd +
                            "\n---> w(d, t) -- " + wdt +
                            "\n---> L(d) -- " + DiskIndexReader.readLdFromBinFile(randomAccessor, documentId));
        }

        // 1 (b, iii). Increase A(d) by wd,t × wq,t.
        double newWeight = wdt * wqt;

        if (finalAccumulators.get(documentId) == null) {
            finalAccumulators.put(documentId, newWeight);
        } else {
            double oldAccumulator = finalAccumulators.get(documentId);
            finalAccumulators.replace(documentId, oldAccumulator + newWeight);
        }
    }

    public void normalizeAccumulators() {
        // iterate through all entries
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

    public static double calculateWdt(int tftd) {
        // w(d, t) = 1 + ln(tf(t, d))
        return (1 + Math.log(tftd));
    }

    public static double calculateWqt(int n, int dft) {
        // w(q, t) = ln(1 + (N / df(t)))
        return (Math.log(1 + ((double) n / dft)));
    }

    public static double calculateLd(Map<String, Integer> tftds) {
        double sum = 0;

        // L(d) = sqrt( sum of all(w(d, t)^2) )
        for (Integer tftd : tftds.values()) {
            double wdt = calculateWdt(tftd);
            sum += Math.pow(wdt, 2);
        }

        return Math.sqrt(sum);
    }

    @Override
    public void close() {
        try {
            randomAccessor.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
