
package application;

import application.UI.CorpusSelection;
import application.classifications.RocchioClassification;
import application.documents.*;
import application.indexes.*;
import application.queries.*;
import application.text.*;
import application.utilities.Menu;
import application.utilities.IndexUtility;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Search engine term project for CECS-429.
 * Date: May 24, 2022
 * @author Caitlin Martinez
 * @author Miguel Zavala
 * @author Steven Dao
 */
public class Application {

    private static final int VOCABULARY_PRINT_SIZE = 1_000; // number of vocabulary terms to print
    private static final int MAX_DISPLAYED_RANKED_ENTRIES = 10;  // the maximum number of ranked entries to display
    private static final int SPELLING_CORRECTION_THRESHOLD = 10; // the posting size trigger to suggest corrections

    private static CorpusSelection cSelect;
    private static String currentDirectory; // the user's current directory to use for queries, initially set to root
    private static List<String> allDirectoryPaths = new ArrayList<>();

    private static final Map<String, DirectoryCorpus> corpora = new HashMap<>();
    private static final Map<String, Index<String, Posting>> corpusIndexes = new HashMap<>();
    private static final Map<String, Index<String, Posting>> biwordIndexes = new HashMap<>();
    private static final Map<String, KGramIndex> kGramIndexes = new HashMap<>();
    private static final Map<String, List<Double>> lds = new HashMap<>();

    public static boolean enabledLogs = false;
    public static final List<Closeable> closeables = new ArrayList<>(); // considers all cases of indexing

    public static void main(String[] args) {
        System.out.printf("""
                %nCopy/paste for testing:
                ./corpus/parks
                ./corpus/federalist-papers
                ./corpus/combined-test
                ./corpus/kanye-test
                ./corpus/moby-dick
                ./corpus/parks-test%n""");
        startApplication();

        //cSelect = new CorpusSelection();
        //cSelect.CorpusSelectionUI();
    }

    public static void startApplication() {
        Scanner in = new Scanner(System.in);
        closeables.add(in);

        String input = Menu.showBuildOrQueryIndexMenu(in);

        /* 1. At startup, ask the user for the name of a directory that they would like to index,
          and construct a DirectoryCorpus from that directory. */
        String directoryString = promptCorpusDirectory(in);
        currentDirectory = directoryString;
        allDirectoryPaths = IndexUtility.getAllDirectories(directoryString);

        // depending on the user's input, either build the index from scratch or read from an on-disk index
        switch (input) {
            case "1" -> initializeComponents(allDirectoryPaths);
            case "2" -> readFromComponents(allDirectoryPaths);
            default -> throw new RuntimeException("Unexpected input: " + input);
        }

        input = Menu.showQueryMenu(in);

        String queryMode = switch (input) {
            case "1" -> "boolean";
            case "2" -> "ranked";
            default -> "";
        };

        if (Integer.parseInt(input) < 3) {
            startQueryLoop(in, queryMode);
        } else {
            queryMode = Menu.showClassificationMenu(in);

            switch (queryMode) {
                case "1" -> startBayesianLoop(in, currentDirectory);
                case "2" -> startRocchioLoop(in, currentDirectory);
                case "3" -> startKNNLoop(in, currentDirectory);
            }
        }
        closeOpenFiles();
    }

    private static String promptCorpusDirectory(Scanner in) {
        System.out.print("\nEnter the path of the directory corpus:\n >> ");
        return in.nextLine();
    }

    private static void initializeComponents(List<String> allDirectoryPaths) {
        for (String directoryPath : allDirectoryPaths) {
            Map<String, String> indexPaths = IndexUtility.createIndexPathsMap(directoryPath);
            Path path = Path.of(directoryPath);
            boolean isRoot = (directoryPath.equals(currentDirectory));

            DirectoryCorpus corpus = DirectoryCorpus.loadDirectory(path, isRoot);
            Index<String, Posting> corpusIndex = indexCorpus(corpus, indexPaths);
            Index<String, Posting> biwordIndex = biwordIndexes.get(indexPaths.get("biwordBin"));
            KGramIndex kGramIndex = kGramIndexes.get(indexPaths.get("kGramsBin"));

            corpora.put(indexPaths.get("root"), corpus);
            corpusIndexes.put(indexPaths.get("root"), corpusIndex);

            DiskIndexWriter.createIndexDirectory(indexPaths.get("indexDirectory"));
            System.out.println("\nWriting files to index directory...");

            // write the documents weights to disk
            DiskIndexWriter.writeLds(indexPaths.get("docWeightsBin"), lds.get(indexPaths.get("docWeightsBin")));
            System.out.println("Document weights written to `" + indexPaths.get("docWeightsBin") + "` successfully.");

            // write the postings using the corpus index to disk
            List<Integer> positionalBytePositions = DiskIndexWriter.writeIndex(indexPaths.get("postingsBin"), corpusIndex);
            System.out.println("Postings written to `" + indexPaths.get("postingsBin") + "` successfully.");

            // write the B+ tree mappings of term -> byte positions to disk
            DiskIndexWriter.writeBTree(indexPaths.get("bTreeBin"), corpusIndex.getVocabulary(), positionalBytePositions);
            System.out.println("B+ Tree written to `" + indexPaths.get("bTreeBin") + "` successfully.");

            List<Integer> biwordBytePositions = DiskIndexWriter.writeBiword(indexPaths.get("biwordBin"), biwordIndex);
            System.out.println("Biword index written to `" + indexPaths.get("biwordBin") + " successfully.");

            DiskIndexWriter.writeBTree(indexPaths.get("biwordBTreeBin"), biwordIndex.getVocabulary(), biwordBytePositions);
            System.out.println("Biword B+ tree written to `" + indexPaths.get("biwordBTreeBin") + "` successfully.");

            // write the k-grams to disk
            DiskIndexWriter.writeKGrams(indexPaths.get("kGramsBin"), kGramIndex);
            System.out.println("K-Grams written to `" + indexPaths.get("kGramsBin") + "` successfully.");
        }

        // after writing the components to disk, we can terminate the program
        System.exit(0);
    }

    private static void readFromComponents(List<String> allDirectoryPaths) {
        for (String directoryPath : allDirectoryPaths) {
            Map<String, String> indexPaths = IndexUtility.createIndexPathsMap(directoryPath);
            Path path = Path.of(directoryPath);
            boolean isRoot = (directoryPath.equals(currentDirectory));

            DirectoryCorpus corpus = DirectoryCorpus.loadDirectory(path, isRoot);
            System.out.println("\nReading index from  `" + indexPaths.get("root") + "`...");

            corpora.put(indexPaths.get("root"), corpus);
            // initialize the DiskPositionalIndex and k-grams using pre-constructed indexes on disk
            corpusIndexes.put(indexPaths.get("root"),
                    new DiskPositionalIndex(DiskIndexReader.readBTree(indexPaths.get("bTreeBin")),
                    indexPaths.get("bTreeBin"), indexPaths.get("postingsBin")));
            biwordIndexes.put(indexPaths.get("biwordBTreeBin"),
                    new DiskBiwordIndex(DiskIndexReader.readBTree(indexPaths.get("biwordBTreeBin")),
                    indexPaths.get("biwordBTreeBin"), indexPaths.get("biwordBin")));
            kGramIndexes.put(indexPaths.get("kGramsBin"), DiskIndexReader.readKGrams(indexPaths.get("kGramsBin")));

            Index<String, Posting> corpusIndex = corpusIndexes.get(indexPaths.get("bTreeBin"));
            Index<String, Posting> biwordIndex = biwordIndexes.get(indexPaths.get("biwordBin"));

            // if we're reading from disk, then we know it is Closeable
            closeables.add((Closeable) corpusIndex);
            closeables.add((Closeable) biwordIndex);

            System.out.printf("""
                    Reading complete.
                                    
                    Found %s documents.
                    Distinct k-grams: %s
                    """, corpus.getCorpusSize(),
                    kGramIndexes.get(indexPaths.get("kGramsBin")).getDistinctKGrams().size());
        }
    }

    public static Index<String, Posting> indexCorpus(DocumentCorpus corpus, Map<String, String> indexPaths) {
        /* 2. Index all documents in the corpus to build a positional inverted index.
          Print to the screen how long (in seconds) this process takes. */
        System.out.println("\nIndexing `" + indexPaths.get("root") + "`...");
        long startTime = System.nanoTime();

        PositionalInvertedIndex index = new PositionalInvertedIndex();
        BiwordIndex biwordIndex = new BiwordIndex();
        KGramIndex kGramIndex = new KGramIndex();
        List<Double> currentLds = new ArrayList<>();
        VocabularyTokenProcessor vocabProcessor = new VocabularyTokenProcessor();
        WildcardTokenProcessor wildcardProcessor = new WildcardTokenProcessor();

        // scan all documents and process each token into terms of our vocabulary
        for (Document document : corpus.getDocuments()) {
            // at the beginning of each document reading, the position always starts at 1
            int currentPosition = 1;
            Map<String, Integer> tftds = new HashMap<>();

            try (Reader documentContent = document.getContent();
                 EnglishTokenStream stream = new EnglishTokenStream(documentContent)) {
                Iterable<String> tokens = stream.getTokens();

                for (String token : tokens) {
                    // before we normalize the token, add it to a minimally processed vocabulary for wildcards
                    List<String> wildcardTokens = wildcardProcessor.processToken(token);

                    // add each unprocessed token to our k-gram index as we traverse through the documents
                    kGramIndex.buildKGramIndex(wildcardTokens, 3);

                    // process the vocabulary token before evaluating whether it exists within our index
                    List<String> terms = vocabProcessor.processToken(token);

                    // since each token can produce multiple terms, add all terms using the same documentID and position
                    for (String term : terms) {
                        index.addTerm(term, document.getId(), currentPosition);
                        biwordIndex.addTerm(term, document.getId());
                        // build up L(d) for the current document
                        if (tftds.get(term) == null) {
                            tftds.put(term, 1);
                        } else {
                            int oldTftd = tftds.get(term);
                            tftds.replace(term, oldTftd + 1);
                        }
                    }
                    // after each token addition, update the position count
                    ++currentPosition;
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // after processing all tokens into terms, calculate L(d) for the document and add it to our list
            currentLds.add(DocumentWeightScorer.calculateLd(new ArrayList<>(tftds.values())));
        }
        kGramIndexes.put(indexPaths.get("kGramsBin"), kGramIndex);
        biwordIndexes.put(indexPaths.get("biwordBin"), biwordIndex);
        lds.put(indexPaths.get("docWeightsBin"), currentLds);

        long endTime = System.nanoTime();
        double timeElapsedInSeconds = (double) (endTime - startTime) / 1_000_000_000;
        System.out.printf("""
                Indexing complete.
                Time elapsed: %s seconds
                
                Found %s documents.
                Distinct k-grams: %s
                """, timeElapsedInSeconds, corpus.getCorpusSize(),
                kGramIndexes.get(indexPaths.get("kGramsBin")).getDistinctKGrams().size());

        return index;
    }

    private static void startQueryLoop(Scanner in, String queryMode) {
        String query;

        do {
            /* unless the user otherwise specifies, the default corpus and indexes will be set to those associated
              with the root directory; else, it will be set to the new current directory */
            DirectoryCorpus corpus = corpora.get(currentDirectory);
            Index<String, Posting> corpusIndex = corpusIndexes.get(currentDirectory);
            KGramIndex kGramIndex = kGramIndexes.get(currentDirectory + "/index/kGrams.bin");

            // 3a. Ask for a search query.
            System.out.print("\nEnter the query (`:?` for help):\n >> ");
            query = in.nextLine();
            String[] splitQuery = query.toLowerCase().split(" ");

            // skip empty input
            if (splitQuery.length > 0) {
                String potentialCommand = splitQuery[0];
                String parameter = "";
                if (splitQuery.length > 1) {
                    parameter = splitQuery[1];

                    // check if the user enabled printing logs to console
                    if (splitQuery[splitQuery.length - 1].equals("--log")) {
                        enabledLogs = true;
                        query = query.substring(0, query.lastIndexOf(" --log"));
                    }
                }

                // 3(a, i). If it is a special query, perform that action.
                switch (potentialCommand) {
                    case ":set" -> {
                        currentDirectory = parameter;
                        System.out.println("Corpus set to `" + parameter + "`.");
                    }
                    case ":index" -> initializeComponents(IndexUtility.getAllDirectories(parameter));
                    case ":stem" -> {
                        TokenStemmer stemmer = new TokenStemmer();
                        System.out.println(parameter + " -> " + stemmer.processToken(parameter).get(0));
                    }
                    case ":vocab" -> {
                        List<String> vocabulary = corpusIndex.getVocabulary();
                        int vocabularyPrintSize = Math.min(vocabulary.size(), VOCABULARY_PRINT_SIZE);

                        for (int i = 0; i < vocabularyPrintSize; ++i) {
                            System.out.println(vocabulary.get(i));
                        }
                        if (vocabulary.size() > VOCABULARY_PRINT_SIZE) {
                            System.out.println("...");
                        }
                        System.out.println("Found " + vocabulary.size() + " terms.");
                    }
                    case ":kgrams" -> {
                        List<String> vocabulary = kGramIndex.getVocabulary();
                        int vocabularyPrintSize = Math.min(vocabulary.size(), VOCABULARY_PRINT_SIZE);

                        for (int i = 0; i < vocabularyPrintSize; ++i) {
                            String currentType = vocabulary.get(i);
                            System.out.println(currentType + " -> " + kGramIndex.getPositionlessPostings(currentType));
                        }
                        if (vocabulary.size() > VOCABULARY_PRINT_SIZE) {
                            System.out.println("...");
                        }
                        System.out.println("Found " + vocabulary.size() + " types.");
                    }
                    case ":?" -> Menu.showHelpMenu(VOCABULARY_PRINT_SIZE);
                    case ":q", "" -> {}
                    default -> {
                        try {
                            int numOfResults;
                            switch (queryMode) {
                                case "boolean" -> numOfResults = displayBooleanResults(query);
                                case "ranked" -> numOfResults = displayRankedResults(query);
                                default -> throw new RuntimeException("Unexpected input: " + queryMode);
                            }

                            /* if a term does not meet the posting size threshold,
                              suggest a modified query including a spelling suggestion */
                            boolean corrected = trySpellingSuggestion(in, query, queryMode);

                            if (numOfResults > 0 || corrected) {
                                IndexUtility.promptForDocumentContent(in, corpus);
                            }
                        } catch (NullPointerException e) {
                            System.err.println("The current corpus directory is not valid; " +
                                    "change it via the `:set` command.");
                        }
                    }
                }
            }
            enabledLogs = false;
        } while (!query.equals(":q"));
    }

    private static int displayBooleanResults(String query) {
        DirectoryCorpus corpus = corpora.get(currentDirectory);
        Index<String, Posting> corpusIndex = corpusIndexes.get(currentDirectory);
        List<Posting> resultPostings;
        // 3(a, ii). If it isn't a special query, then parse the query and retrieve its postings.
        BooleanQueryParser parser = new BooleanQueryParser();
        QueryComponent parsedQuery = parser.parseQuery(query);
        TokenProcessor processor;

        if (parsedQuery instanceof PhraseLiteral) {
            processor = new QueryTokenProcessor();
            resultPostings = parsedQuery.getPostings(corpusIndex, processor);
        } else {
            processor = new VocabularyTokenProcessor();
            resultPostings = parsedQuery.getPositionlessPostings(corpusIndex, processor);
        }

        // in case the query contains wildcards, only display each unique posting once
        resultPostings = IndexUtility.getDistinctPostings(resultPostings);
        IndexUtility.displayPostings(corpus, resultPostings);
        return resultPostings.size();
    }

    private static int displayRankedResults(String query) {
        DirectoryCorpus corpus = corpora.get(currentDirectory);
        Index<String, Posting> corpusIndex = corpusIndexes.get(currentDirectory);
        DocumentWeightScorer documentScorer = new DocumentWeightScorer(currentDirectory + "/index/docWeights.bin");
        closeables.add(documentScorer);

        documentScorer.storeTermAtATimeDocuments(corpusIndex, query);
        List<Map.Entry<Integer, Double>> rankedEntries = documentScorer.getRankedEntries(MAX_DISPLAYED_RANKED_ENTRIES);

        if (rankedEntries.size() > 0) {
            for (Map.Entry<Integer, Double> entry : rankedEntries) {
                int currentDocumentId = entry.getKey();
                double score = entry.getValue();

                DecimalFormat decimalFormat = new DecimalFormat("###.######");
                System.out.printf("- " + corpus.getDocument(currentDocumentId).getTitle() +
                        " (ID: " + currentDocumentId + ") -- " + decimalFormat.format(score) + "\n");
            }
        } else {
            System.out.println("Found 0 documents that matched the query.");
        }

        return rankedEntries.size();
    }

    public static boolean trySpellingSuggestion(Scanner in, String query, String queryMode) {
        Index<String, Posting> corpusIndex = corpusIndexes.get(currentDirectory);
        KGramIndex kGramIndex = kGramIndexes.get(currentDirectory + "/index/kGrams.bin");

        SpellingSuggestion spellingCheck = new SpellingSuggestion(corpusIndex, kGramIndex);
        String[] splitQuery = query.replace(" + ", " ").split(" ");
        StringBuilder newQuery = new StringBuilder();
        List<String> currentQuery = new ArrayList<>();
        boolean meetsThreshold = false;

        for (int i = 0; i < splitQuery.length; ++i) {
            VocabularyTokenProcessor processor = new VocabularyTokenProcessor();
            String currentToken = splitQuery[i];
            List<String> terms = processor.processToken(currentToken);
            int dft = 0;

            if (terms.size() > 0) {
                dft = corpusIndex.getPositionlessPostings(terms.get(0)).size();
            }

            String replacementType;

            /* verify that each term meets the threshold requirement for postings sizes;
               if it does, suggest a correction, or if it doesn't, use the original query type, */
            if (dft > SPELLING_CORRECTION_THRESHOLD || currentToken.contains("*")) {
                replacementType = currentToken;
                currentQuery.add(currentToken);
            } else {
                replacementType = spellingCheck.suggestCorrection(currentToken);
                meetsThreshold = true;
            }

            newQuery.append(replacementType);
            if (i < splitQuery.length - 1) {
                newQuery.append(" ");
            }
        }

        // only proceed if we made a suggestion to the original query
        if (meetsThreshold && !newQuery.toString().equals(query) && !query.contains(" + ")) {
            if (currentQuery.size() > 0) {
                System.out.print("Results shown for `");
                for (int i = 0; i < currentQuery.size(); ++i) {
                    String currentType = currentQuery.get(i);
                    System.out.print(((i < currentQuery.size() - 1) ? currentType + " " : currentType + "`.\n"));
                }
            }

            System.out.print("Did you mean `" + newQuery + "`? (`y` to proceed)\n >> ");
            query = in.nextLine();

            if (query.equals("y")) {
                System.out.println("Showing results for `" + newQuery + "`:");
                switch (queryMode) {
                    case "boolean" -> displayBooleanResults(newQuery.toString());
                    case "ranked" -> displayRankedResults(newQuery.toString());
                    default -> throw new RuntimeException("Unexpected input: " + query);
                }
                return true;
            }
        }
        return false;
    }

    private static void startBayesianLoop(Scanner in, String rootDirectoryPath) {

    }

    private static void startRocchioLoop(Scanner in, String rootDirectoryPath) {
        System.out.println("\nCalculating...");
        RocchioClassification rocchio = new RocchioClassification(rootDirectoryPath, corpora, corpusIndexes);
        System.out.println("Calculations complete.");
        String input;

        do {
            input = Menu.showRocchioMenu(in);

            switch (input) {
                // classify a document
                case "1" -> {
                    System.out.println("x");
                } // classify all disputed documents
                case "2" -> {
                    System.out.println("xx");
                } // get a centroid vector
                case "3" -> {
                    System.out.println("xxx");
                } // get a document weight vector
                case "4" -> {
                    System.out.print("Enter the directory's subfolder:\n >> ");
                    String subfolder = in.nextLine();
                    System.out.print("Enter the document ID:\n >> ");
                    int documentID = Integer.parseInt(in.nextLine());

                    System.out.println(rocchio.getVector(currentDirectory + subfolder, documentID));
                } // get a vocabulary list
                case "5" -> {
                    System.out.print("Enter the directory's subfolder:\n >> ");
                    String subfolder = in.nextLine();

                    System.out.println(rocchio.getVocabulary(currentDirectory + subfolder));
                }
            }
        } while (!input.equals("0"));
    }

    private static void closeOpenFiles() {
        // close all open file resources case-by-case
        for (Closeable stream : closeables) {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void startKNNLoop(Scanner in, String rootDirectoryPath) {

    }

    public static Map<String, DirectoryCorpus> getCorpora() {
        return corpora;
    }

    public static Map<String, Index<String, Posting>> getBiwordIndexes() {
        return biwordIndexes;
    }

    public static Map<String, KGramIndex> getKGramIndexes() {
        return kGramIndexes;
    }

    public static String getCurrentDirectory() {
        return currentDirectory;
    }

    public static List<String> getAllDirectoryPaths() {
        List<String> sortedPaths = new ArrayList<>(allDirectoryPaths);
        Collections.sort(sortedPaths);
        return sortedPaths;
    }
}
