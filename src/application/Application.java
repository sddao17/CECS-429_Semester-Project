
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
import application.text.TrimSplitTokenProcessor;

import java.nio.file.Paths;
import java.util.*;

public class Application {

    public static void main(String[] args) {
        // change these as needed
        //String directoryPathString = "./corpus/parks";
        String directoryPathString = "./corpus/test-sites";
        //String directoryPathString = "./corpus/kanye-test";

        String extensionType = ".json";
        //String extensionType = ".txt";

        // Create a DocumentCorpus to load .json documents from the project directory.
        DocumentCorpus corpus = DirectoryCorpus.loadJsonDirectory(
                Paths.get(directoryPathString).toAbsolutePath(), extensionType);
        // Index the documents of the corpus.
        System.out.println("Indexing ...");
        long startTime = System.nanoTime();

        Index index = indexCorpus(corpus);

        long endTime = System.nanoTime();
        double elapsedTimeInSeconds = (double) (endTime - startTime) / 1_000_000_000;
        System.out.println("Indexing complete." +
                "\nElapsed time: " + elapsedTimeInSeconds + " seconds");

        System.out.println("\nVocabulary:\n" + index.getVocabulary());

        Scanner in = new Scanner(System.in);
        String query = "";

        do {
            System.out.print("\nEnter the query:\n >> ");
            query = in.nextLine();

            BooleanQueryParser parser = new BooleanQueryParser();
            QueryComponent parsedQuery = parser.parseQuery(query);
            List<Posting> resultPostings = parsedQuery.getPostings(index);

            for (Posting posting : resultPostings) {
                System.out.println("- " + corpus.getDocument(posting.getDocumentId()).getTitle() +
                        " (Doc ID: " + posting.getDocumentId() +
                        ", positions: " + posting.getPositions() + ")");
            }
            System.out.println("Found " + resultPostings.size() + " documents.");
        } while (!query.equals(""));
    }

    private static Index indexCorpus(DocumentCorpus corpus) {
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
                // after each token, update the position count
                ++currentPosition;
            }
        }

        return index;
    }
}
