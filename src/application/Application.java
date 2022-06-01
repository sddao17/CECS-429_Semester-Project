
package application;

import application.documents.DirectoryCorpus;
import application.documents.Document;
import application.documents.DocumentCorpus;
import application.indexes.Index;
import application.indexes.PositionalInvertedIndex;
import application.indexes.Posting;
import application.text.BasicTokenProcessor;
import application.text.EnglishTokenStream;

import java.nio.file.Paths;
import java.util.*;

public class Application {

    public static void main(String[] args) {
        // change these as needed
        String directoryPathString = "./all-nps-sites-extracted";
        String extensionType = ".json";

        // Create a DocumentCorpus to load .json documents from the project directory.
        DocumentCorpus corpus = DirectoryCorpus.loadJsonDirectory(
                Paths.get(directoryPathString).toAbsolutePath(), extensionType);
        // Index the documents of the corpus.
        Index index = indexCorpus(corpus);

        Scanner in = new Scanner(System.in);

        System.out.print("Enter the search term:\n >> ");
        // We aren't ready to use a full query parser; for now, we'll only support single-term queries.
        String query = in.nextLine().toLowerCase();

        for (Posting posting : index.getPostings(query)) {
            System.out.println("Document " + corpus.getDocument(posting.getDocumentId()).getTitle());
        }
    }

    private static Index indexCorpus(DocumentCorpus corpus) {
        BasicTokenProcessor processor = new BasicTokenProcessor();
        PositionalInvertedIndex index = new PositionalInvertedIndex();

        // scan all documents and process each token into terms of our vocabulary
        for (Document document : corpus.getDocuments()) {
            EnglishTokenStream stream = new EnglishTokenStream(document.getContent());
            Iterable<String> tokens = stream.getTokens();
            // at the beginning of each document reading, the position always starts at 1
            int currentPosition = 1;

            for (String token : tokens) {
                // process the token before evaluating whether it exists within our matrix
                String term = processor.processToken(token);
                index.addTerm(term, document.getId(), currentPosition);
                // after each token, update the position count
                ++currentPosition;
            }
        }

        return index;
    }
}
