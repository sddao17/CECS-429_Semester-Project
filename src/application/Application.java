
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
        String directoryPathString = "./corpus/test-sites";
        String extensionType = ".json";

        // Create a DocumentCorpus to load .json documents from the project directory.
        DocumentCorpus corpus = DirectoryCorpus.loadJsonDirectory(
                Paths.get(directoryPathString).toAbsolutePath(), extensionType);
        // Index the documents of the corpus.
        Index index = indexCorpus(corpus);

        System.out.println("Vocabulary:\n" + index.getVocabulary());

        Scanner in = new Scanner(System.in);
        String query = "";

        do {
            System.out.print("Enter the query:\n >> ");
            query = in.nextLine();

            BooleanQueryParser parser = new BooleanQueryParser();
            QueryComponent parsedQuery = parser.parseQuery(query);

            for (Posting posting : parsedQuery.getPostings(index)) {
                System.out.println(corpus.getDocument(posting.getDocumentId()).getTitle());
                System.out.println(" - Query: " + parsedQuery + ", doc id: " + posting.getDocumentId() +
                        ", positions: " + posting.getPositions());
            }
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
