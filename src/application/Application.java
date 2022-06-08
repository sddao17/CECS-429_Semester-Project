package application;

import application.UI.CorpusSelection;
import application.documents.*;
import application.indexes.Index;
import application.indexes.KGramIndex;
import application.indexes.PositionalInvertedIndex;
import application.indexes.Posting;
import application.queries.BooleanQueryParser;
import application.queries.QueryComponent;
import application.text.EnglishTokenStream;
import application.text.TokenStemmer;
import application.text.VocabularyTokenProcessor;
import application.text.WildcardTokenProcessor;

import java.nio.file.Path;
import java.util.*;

/**
 * Search engine term project for CECS-429.
 * Date: May 24, 2022
 * @author Caitlin Martinez, Miguel Zavala, Steven Dao
 */
public class Application {

    private static final int VOCABULARY_PRINT_SIZE = 1_000; // number of vocabulary terms to print
    private static DirectoryCorpus corpus;  // we need only one of each corpus and index active at a time,
    private static Index<String, Posting> corpusIndex;  // and multiple methods need access to them
    private static KGramIndex kGramIndex;
    private static CorpusSelection cSelect;

    public static void main(String[] args) {
        System.out.printf("""
                %nCopy/paste for testing:
                ./corpus/parks
                ./corpus/parks-test
                ./corpus/kanye-test
                ./corpus/moby-dick%n""");
        //startApplication();

        cSelect = new CorpusSelection();
        cSelect.CorpusSelectionUI();
    }

    private static void startApplication() {
        /* 1. At startup, ask the user for the name of a directory that they would like to index,
          and construct a DirectoryCorpus from that directory. */
        System.out.print("\nEnter the path of the directory corpus:\n >> ");
        Scanner in = new Scanner(System.in);
        String directoryPath = in.nextLine().toLowerCase();

        initializeComponents(Path.of(directoryPath));

        System.out.printf("""
                %nSpecial Commands:
                :index `directory-name`  --  Index the folder at the specified path.
                          :stem `token`  --  Stem, then print the token string.
                                 :vocab  --  Print the first %s terms in the vocabulary of the corpus,
                                             then print the total number of vocabulary terms.
                                     :q  --  Exit the program.
                """, VOCABULARY_PRINT_SIZE);

        startQueryLoop(in);
    }

    private static void initializeComponents(Path directoryPath) {
        corpus = DirectoryCorpus.loadDirectory(directoryPath);
        // by default, our `k` value for k-gram indexes will be set to 3
        corpusIndex = indexCorpus(corpus);
    }

    public static Index<String, Posting> indexCorpus(DocumentCorpus corpus) {
        /* 2. Index all documents in the corpus to build a positional inverted index.
          Print to the screen how long (in seconds) this process takes. */
        System.out.println("\nIndexing...");
        long startTime = System.nanoTime();

        kGramIndex = new KGramIndex();
        VocabularyTokenProcessor processor = new VocabularyTokenProcessor();
        PositionalInvertedIndex index = new PositionalInvertedIndex();

        // scan all documents and process each token into terms of our vocabulary
        for (Document document : corpus.getDocuments()) {
            EnglishTokenStream stream = new EnglishTokenStream(document.getContent());
            Iterable<String> tokens = stream.getTokens();
            // at the beginning of each document reading, the position always starts at 1
            int currentPosition = 1;

            for (String token : tokens) {
                // before we normalize the token, add it to a minimally processed vocabulary for wildcards
                WildcardTokenProcessor wildcardProcessor = new WildcardTokenProcessor();
                String wildcardToken = wildcardProcessor.processToken(token).get(0);

                // add each unprocessed token to our k-gram index as we traverse through the documents
                kGramIndex.addToken(wildcardToken, 3);

                // process the token before evaluating whether it exists within our index
                List<String> terms = processor.processToken(token);

                // since each token can produce multiple terms, add all terms using the same documentID and position
                for (String term : terms) {
                    index.addTerm(term, document.getId(), currentPosition);
                }
                // after each token addition, update the position count
                ++currentPosition;
            }
        }

        long endTime = System.nanoTime();
        double elapsedTimeInSeconds = (double) (endTime - startTime) / 1_000_000_000;
        System.out.println("Indexing complete." +
                "\nDistinct k-grams: " + kGramIndex.getDistinctKGrams().size() +
                "\nTime elapsed: " + elapsedTimeInSeconds + " seconds");

        return index;
    }

    private static void startQueryLoop(Scanner in) {
        String query;

        do {
            // 3a. Ask for a search query.
            System.out.print("\nEnter the query:\n >> ");
            query = in.nextLine();
            String[] splitQuery = query.split(" ");

            // skip empty input
            if (splitQuery.length > 0) {
                String potentialCommand = splitQuery[0];
                String parameter = "";
                if (splitQuery.length > 1) {
                    parameter = splitQuery[1];
                }

                // 3(a, i). If it is a special query, perform that action.
                switch (potentialCommand) {
                    case ":index" -> initializeComponents(Path.of(parameter));
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
                    case ":q", "" -> {}
                    default -> {
                        // 3(a, ii). If it isn't a special query, then parse the query and retrieve its postings.
                        BooleanQueryParser parser = new BooleanQueryParser();
                        QueryComponent parsedQuery = parser.parseQuery(query);
                        List<Posting> resultPostings = parsedQuery.getPostings(corpusIndex);

                        displayPostings(resultPostings, in);
                    }
                }
            }
        } while (!query.equals(":q"));
    }

    private static void displayPostings(List<Posting> resultPostings, Scanner in) {
        // 3(a, ii, A). Output the names of the documents returned from the query, one per line.
        for (Posting posting : resultPostings) {
            int currentDocumentId = posting.getDocumentId();

            System.out.println("- " + corpus.getDocument(currentDocumentId).getTitle() +
                    " (ID: " + currentDocumentId + ")");
        }

        // 3(a, ii, B). Output the number of documents returned from the query, after the document names.
        System.out.println("Found " + resultPostings.size() + " documents.");

        /* 3(a, ii, C). Ask the user if they would like to select a document to view.
          If the user selects a document to view, print the entire content of the document to the screen. */
        if (resultPostings.size() > 0) {
            System.out.print("Enter the document ID to view its contents (any other input to exit):\n >> ");
            String query = in.nextLine();
            // since error handling is not a priority requirement, use a try/catch for now
            try {
                Document document = corpus.getDocument(Integer.parseInt(query));
                EnglishTokenStream stream = new EnglishTokenStream(document.getContent());

                // print the tokens to the console without processing them
                stream.getTokens().forEach(token -> System.out.print(token + " "));
                System.out.println();
            } catch (Exception ignored) {}
        }
    }

    public static Index<String, String> getKGramIndex() {
        return kGramIndex;
    }
}
