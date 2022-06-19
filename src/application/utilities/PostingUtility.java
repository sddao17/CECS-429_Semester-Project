
package application.utilities;

import application.documents.DirectoryCorpus;
import application.documents.Document;
import application.indexes.Posting;
import application.text.EnglishTokenStream;

import java.io.Reader;
import java.util.*;

public class PostingUtility {

    public static Map<String, String> createIndexPathsMap(String directoryString) {
        String pathToIndexDirectory = directoryString + PathSuffix.INDEX_DIRECTORY.getLabel();

        return new HashMap<>() {{
            put("indexDirectory", pathToIndexDirectory);
            put("docWeightsBin", pathToIndexDirectory + PathSuffix.DOC_WEIGHTS_FILE.getLabel());
            put("postingsBin", pathToIndexDirectory + PathSuffix.POSTINGS_FILE.getLabel());
            put("bTreeBin", pathToIndexDirectory + PathSuffix.BTREE_FILE.getLabel());
            put("kGramsBin", pathToIndexDirectory + PathSuffix.KGRAMS_FILE.getLabel());
        }};
    }

    public static List<Posting> getDistinctPostings(List<Posting> postings) {
        List<Posting> distinctPostings = new ArrayList<>();
        List<Integer> distinctDocumentIds = new ArrayList<>();

        for (Posting currentPosting : postings) {
            int currentDocumentId = currentPosting.getDocumentId();

            if (!distinctDocumentIds.contains(currentDocumentId)) {
                distinctPostings.add(currentPosting);
                distinctDocumentIds.add(currentDocumentId);
            }
        }

        return distinctPostings;
    }

    public static void displayPostings(DirectoryCorpus corpus, List<Posting> resultPostings) {
        // 3(a, ii, A). Output the names of the documents returned from the query, one per line.
        for (Posting posting : resultPostings) {
            int currentDocumentId = posting.getDocumentId();

            System.out.println("- " + corpus.getDocument(currentDocumentId).getTitle() +
                    " (ID: " + currentDocumentId + ")");
        }

        // 3(a, ii, B). Output the number of documents returned from the query, after the document names.
        System.out.println("Found " + resultPostings.size() + " documents.");
    }

    public static void promptForDocumentContent(Scanner in, DirectoryCorpus corpus) {
        System.out.print("Enter the document ID to view its contents (any other input to exit):\n >> ");
        String query = in.nextLine();

        // since error handling is not a priority requirement, use a try/catch for now
        try {
            Document document = corpus.getDocument(Integer.parseInt(query));
            Reader documentContent = document.getContent();
            EnglishTokenStream stream = new EnglishTokenStream(documentContent);

            // print the tokens to the console without processing them
            stream.getTokens().forEach(token -> System.out.print(token + " "));
            System.out.println();
            documentContent.close();
        } catch (Exception ignored) {}
    }
}
