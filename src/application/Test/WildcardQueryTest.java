
package application.Test;

import application.Application;
import application.documents.DirectoryCorpus;
import application.documents.Document;
import application.indexes.Index;
import application.indexes.Posting;
import application.queries.BooleanQueryParser;
import application.queries.QueryComponent;
import application.text.*;
import org.junit.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class WildcardQueryTest {


    Path directoryPath = Path.of("./corpus/parks-test");
    DirectoryCorpus testCorpus = DirectoryCorpus.loadDirectory(directoryPath);
    Index<String, Posting> index = Application.indexCorpus(testCorpus);
    TokenProcessor processor = new VocabularyTokenProcessor();
    BooleanQueryParser parser = new BooleanQueryParser();

    public List<String> scanTextBody(String query, String queryType) {
        List<String> documentTitles = new ArrayList<>();

        // parse and scan the text body to see if the parsed query exists within it
        for (Document document : testCorpus.getDocuments()) {
            StringBuilder parsedTextBody = new StringBuilder();
            TokenStream stream = new EnglishTokenStream(document.getContent());
            Iterable<String> content = stream.getTokens();

            for (String token : content) {
                parsedTextBody.append(processor.processToken(token)).append(" ");
            }

            String parsedContent = parsedTextBody.toString();
            parsedContent = parsedContent.replaceAll("\\[", "").replaceAll("]", "");
            String[] splitQuery = query.split(" ");

            switch (queryType) {
                case ("SINGLE") -> {
                    String currentTitle = document.getTitle();

                    if (queryMatchesTextBody(parsedContent, query) && !documentTitles.contains(currentTitle)) {
                        documentTitles.add(document.getTitle());
                    }
                }
                case ("OR") -> {
                    for (String subQuery : splitQuery) {
                        if (queryMatchesTextBody(parsedContent, subQuery) && !documentTitles.contains(document.getTitle())) {
                            documentTitles.add(document.getTitle());
                        }
                    }
                }
                case ("AND") -> {
                    boolean allExist = true;

                    for (String subQuery : splitQuery) {

                        if (!queryMatchesTextBody(parsedContent, subQuery)) {
                            allExist = false;
                        }
                    }

                    if (allExist && !documentTitles.contains(document.getTitle())) {
                        documentTitles.add(document.getTitle());
                    }
                }
                case ("PHRASE") -> {
                        if (queryMatchesTextBody(parsedContent, query) && !documentTitles.contains(document.getTitle())) {
                            documentTitles.add(document.getTitle());
                        }
                }
            }
        }

        return documentTitles;
    }

    public boolean queryMatchesTextBody(String text, String query) {
        String[] splitText = text.split(" ");
        String[] splitQuery = text.split("\\*");

        for (String token : splitText) {
            boolean found = false;

            for (String subQuery : splitQuery) {
                if (token.matches(subQuery)) {
                    found = true;
                } else {
                    found = false;
                }
            }

            if (found) {
                return true;
            }
        }

        return false;
    }

    public List<String> findTitles(String query) {
        QueryComponent parsedQuery = parser.parseQuery(query);
        List<Posting> resultPostings = parsedQuery.getPostings(index);
        List<String> resultTitles = new ArrayList<>();

        for (Posting posting : resultPostings) {
            int currentDocumentId = posting.getDocumentId();

            resultTitles.add(testCorpus.getDocument(currentDocumentId).getTitle());
        }
        return resultTitles;
    }

    @Test
    public void singleOrQuery() {
        String query = "washington + visitor";
        query = processor.processToken(query).get(0);

        List<String> resultTitles = findTitles(query);
        List<String> expectedTitles = scanTextBody(query, "OR");

        boolean titlesMatch = resultTitles.containsAll(expectedTitles) && expectedTitles.containsAll(resultTitles);

        assertTrue("The list of document titles should match:\nActual: " +
                        resultTitles + "\nExpected: " + expectedTitles, titlesMatch);
    }

    @Test
    public void andQueryTest() {

    }

    @Test
    public void orQueryTest() {

    }

    @Test
    public void phraseQueryTest() {

    }
}
