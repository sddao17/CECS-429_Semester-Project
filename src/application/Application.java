
package application;

import application.documents.DirectoryCorpus;
import application.documents.Document;
import application.documents.DocumentCorpus;
import application.indexes.Index;
import application.indexes.PositionalInvertedIndex;
import application.indexes.Posting;
import application.queries.BooleanQueryParser;
import application.queries.QueryComponent;
import application.text.EnglishTokenStream;
import application.text.TokenStemmer;
import application.text.TrimSplitTokenProcessor;

import java.nio.file.Paths;
import java.util.*;

public class Application {

    // global constants - change as needed
    public static final int VOCABULARY_PRINT_SIZE = 1000;
    // global variables
    private static DocumentCorpus corpus;
    private static Index index;

    public static void main(String[] args) {
        startApplication();
    }

    private static void startApplication() {
        /*
         TODO:
         1. At startup, ask the user for the name of a directory that they would like to index,
            and construct a DirectoryCorpus from that directory.
         */
        System.out.print("\nEnter the path of the directory corpus:\n >> ");

        Scanner in = new Scanner(System.in);
        String directoryPath = in.nextLine().toLowerCase();

        initializeComponents(directoryPath);

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

    private static void initializeComponents(String directoryPath) {
        // store the corpus and index as global member variables so the garbage collector can remove past instances
        corpus = createCorpus(directoryPath);
        index = indexCorpus(corpus);
    }

    private static DocumentCorpus createCorpus(String directoryPath) {
        // Create a DocumentCorpus to load documents from the project directory.
        return DirectoryCorpus.loadJsonDirectory(Paths.get(directoryPath).toAbsolutePath(), ".json");
    }

    private static Index indexCorpus(DocumentCorpus corpus) {
        /*
         TODO:
         2. Index all documents in the corpus to build a positional inverted index.
            Print to the screen how long (in seconds) this process takes.
         */
        System.out.println("\nIndexing ...");
        long startTime = System.nanoTime();

        TrimSplitTokenProcessor processor = new TrimSplitTokenProcessor();
        PositionalInvertedIndex index = new PositionalInvertedIndex();

        // scan all documents and process each token into terms of our vocabulary
        for (Document document : corpus.getDocuments()) {
            EnglishTokenStream stream = new EnglishTokenStream(document.getContent());
            Iterable<String> tokens = stream.getTokens();
            // at the beginning of each document reading, the position always starts at 1
            int currentPosition = 1;

            for (String token : tokens) {
                // process the token before evaluating whether it exists within our matrix
                ArrayList<String> terms = processor.processToken(token);

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
                "\nElapsed time: " + elapsedTimeInSeconds + " seconds");

        return index;
    }

    private static void startQueryLoop(Scanner in) {
        String query;

        do {
            /*
             TODO:
             3a. Ask for a search query.
            */
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

                /*
                 TODO:
                 3(a, i). If it is a special query, perform that action.
                */
                switch (potentialCommand) {
                    case ":index" -> initializeComponents(parameter);
                    case ":stem" -> {
                        TokenStemmer stemmer = new TokenStemmer();
                        System.out.println(stemmer.processToken(parameter).get(0));
                    }
                    case ":vocab" -> {
                        List<String> vocabulary = index.getVocabulary();
                        int vocabularyPrintSize = Math.min(vocabulary.size(), VOCABULARY_PRINT_SIZE);
                        for (int i = 0; i < vocabularyPrintSize; ++i) {
                            System.out.println(vocabulary.get(i));
                        }
                        if (vocabulary.size() > VOCABULARY_PRINT_SIZE) {
                            System.out.println("...");
                        }
                        System.out.println("Found " + vocabulary.size() + " terms.");
                    }
                    case ":q" -> {}
                    default -> {
                        /*
                         TODO:
                         3(a, ii). If it isn't a special query, then parse the query and retrieve its postings.
                        */
                        BooleanQueryParser parser = new BooleanQueryParser();
                        QueryComponent parsedQuery = parser.parseQuery(query);
                        List<Posting> resultPostings = parsedQuery.getPostings(index);

                        displayPostings(corpus, resultPostings, in);
                    }
                }
            }
        } while (!query.equals(":q"));
    }

    private static void displayPostings(DocumentCorpus corpus, List<Posting> resultPostings, Scanner in) {
        /*
         TODO:
         3(a, ii, A). Output the names of the documents returned from the query, one per line.
        */
        for (Posting posting : resultPostings) {
            int currentDocumentId = posting.getDocumentId();

            System.out.println("- " + corpus.getDocument(currentDocumentId).getTitle() +
                    " (ID: " + currentDocumentId + ")");
        }
        /*
         TODO:
         3(a, ii, B). Output the number of documents returned from the query, after the document names.
        */
        System.out.println("Found " + resultPostings.size() + " documents.");

        /*
         TODO:
         3(a, ii, C). Ask the user if they would like to select a document to view.
                      If the user selects a document to view, print the entire content of the document to the screen.
        */
        if (resultPostings.size() > 0) {
            System.out.print("Would you like to view a document? (`y` to proceed)\n >> ");
            String query = in.nextLine().toLowerCase();

            if (query.equals("y")) {
                System.out.print("Enter the document ID to view:\n >> ");
                query = in.nextLine();
                Document document = corpus.getDocument(Integer.parseInt(query));
                EnglishTokenStream stream = new EnglishTokenStream(document.getContent());

                // print the tokens to the user without processing them
                stream.getTokens().forEach(c -> System.out.print(c + " "));
                System.out.println();
            }
        }
    }
}
