
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
    TokenProcessor processor = new WildcardTokenProcessor();
    BooleanQueryParser parser = new BooleanQueryParser();

    public List<String> scanDocuments(List<String> splitQuery, String queryType) {
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
            //System.out.println(parsedContent);

            switch (queryType) {
                case ("OR") -> {
                    for (String subQuery : splitQuery) {
                        String[] splitSubQueries = subQuery.split(" \\+ ");

                        for (String splitSubQuery : splitSubQueries) {
                            if (queryMatchesTextBody(parsedContent, splitSubQuery) &&
                                    !documentTitles.contains(document.getTitle())) {
                                documentTitles.add(document.getTitle());
                            }
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
                        if (queryMatchesTextBody(parsedContent, String.join(" ", splitQuery)) &&
                                !documentTitles.contains(document.getTitle())) {
                            documentTitles.add(document.getTitle());
                        }
                }
            }
        }

        return documentTitles;
    }

    public boolean queryMatchesTextBody(String text, String query) {
        String[] splitText = text.split(" ");
        if (query.length() == 1) {
            query = query.replaceAll("\\+", "");
        }

        for (String token : splitText) {
            if (token.matches(query)) {
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
    public void singleTrailingWildcardQuery() {
        String query = "washing*";
        List<String> splitQuery = processor.processToken(query);

        List<String> resultTitles = findTitles(query);
        //List<String> expectedTitles = scanDocuments(splitQuery, "OR");
        List<String> expectedTitles = new ArrayList<>(){{add("George Washington Carver National Monument: Cooperating Association");}};


        boolean titlesMatch = resultTitles.containsAll(expectedTitles) && expectedTitles.containsAll(resultTitles);

        assertTrue("The list of document titles should match:\nActual: " +
                resultTitles + "\nExpected: " + expectedTitles, titlesMatch);
    }

    @Test
    public void singleLeadingWildcardQuery() {
        String query = "*nal";
        List<String> splitQuery = processor.processToken(query);

        List<String> resultTitles = findTitles(query);
        //List<String> expectedTitles = scanDocuments(splitQuery, "OR");
        List<String> expectedTitles = new ArrayList<>(){{add("Sand Creek Massacre National Historic Site: Photo Gallery"); add("Kalaupapa National Historical Park: Joseph Dutton"); add("Kaloko-Honokōhau National Historical Park: Coral Reef Activities and Staff"); add("Kaloko-Honokōhau National Historical Park: Coral Reef Studies and Products"); add("Santa Fe National Historic Trail: Site Identification - Entrance"); add("Tallgrass Prairie National Preserve: Support Your Park"); add("George Washington Carver National Monument: Cooperating Association"); add("Tallgrass Prairie National Preserve: Work With Us"); add("Tallgrass Prairie National Preserve: Planning"); add("George Washington Carver National Monument: Support Your Park");}};

        boolean titlesMatch = resultTitles.containsAll(expectedTitles) && expectedTitles.containsAll(resultTitles);

        assertTrue("The list of document titles should match:\nActual: " +
                resultTitles + "\nExpected: " + expectedTitles, titlesMatch);
    }

    @Test
    public void singleOrWildcardQuery() {
        String query = "washing* + visitor";
        List<String> splitQuery = processor.processToken(query);

        List<String> resultTitles = findTitles(query);
        //List<String> expectedTitles = scanDocuments(splitQuery, "OR");
        List<String> expectedTitles = new ArrayList<>(){{add("Tallgrass Prairie National Preserve: Support Your Park"); add("George Washington Carver National Monument: Cooperating Association");}};

        boolean titlesMatch = resultTitles.containsAll(expectedTitles) && expectedTitles.containsAll(resultTitles);

        assertTrue("The list of document titles should match:\nActual: " +
                        resultTitles + "\nExpected: " + expectedTitles, titlesMatch);
    }

    @Test
    public void longOrWildcardQuery() {
        String query = "i* + th*s + wash* + *ad";
        List<String> splitQuery = processor.processToken(query);

        List<String> resultTitles = findTitles(query);
        //List<String> expectedTitles = scanDocuments(splitQuery, "OR");
        List<String> expectedTitles = new ArrayList<>(){{add("Sand Creek Massacre National Historic Site: Photo Gallery"); add("Kalaupapa National Historical Park: Joseph Dutton"); add("Kaloko-Honokōhau National Historical Park: Coral Reef Activities and Staff"); add("Kaloko-Honokōhau National Historical Park: Coral Reef Studies and Products"); add("Santa Fe National Historic Trail: Site Identification - Entrance"); add("Tallgrass Prairie National Preserve: Support Your Park"); add("George Washington Carver National Monument: Cooperating Association"); add("Tallgrass Prairie National Preserve: Work With Us"); add("Tallgrass Prairie National Preserve: Planning"); add("George Washington Carver National Monument: Support Your Park");}};

        boolean titlesMatch = resultTitles.containsAll(expectedTitles) && expectedTitles.containsAll(resultTitles);

        assertTrue("The list of document titles should match:\nActual: " +
                resultTitles + "\nExpected: " + expectedTitles, titlesMatch);
    }

    @Test
    public void singleAndWildcardQueryTest() {
        String query = "mass*re hist*";
        List<String> splitQuery = processor.processToken(query);

        List<String> resultTitles = findTitles(query);
        //List<String> expectedTitles = scanDocuments(splitQuery, "AND");
        List<String> expectedTitles = new ArrayList<>(){{add("Sand Creek Massacre National Historic Site: Photo Gallery");}};

        boolean titlesMatch = resultTitles.containsAll(expectedTitles) && expectedTitles.containsAll(resultTitles);

        assertTrue("The list of document titles should match:\nActual: " +
                resultTitles + "\nExpected: " + expectedTitles, titlesMatch);
    }

    @Test
    public void longAndWildcardQueryTest() {
        String query = "nat*a*l *ark an* *he";
        List<String> splitQuery = processor.processToken(query);

        List<String> resultTitles = findTitles(query);
        //List<String> expectedTitles = scanDocuments(splitQuery, "AND");
        List<String> expectedTitles = new ArrayList<>(){{add("Sand Creek Massacre National Historic Site: Photo Gallery"); add("Kalaupapa National Historical Park: Joseph Dutton"); add("Kaloko-Honokōhau National Historical Park: Coral Reef Studies and Products"); add("Tallgrass Prairie National Preserve: Support Your Park"); add("Tallgrass Prairie National Preserve: Planning");}};

        boolean titlesMatch = resultTitles.containsAll(expectedTitles) && expectedTitles.containsAll(resultTitles);

        assertTrue("The list of document titles should match:\nActual: " +
                resultTitles + "\nExpected: " + expectedTitles, titlesMatch);
    }

    @Test
    public void singlePhraseWildcardQueryTest() {
        String query = "\"nation* *ark\"";
        List<String> splitQuery = processor.processToken(query);

        List<String> resultTitles = findTitles(query);
        //List<String> expectedTitles = scanDocuments(splitQuery, "AND");
        List<String> expectedTitles = new ArrayList<>(){{add("Sand Creek Massacre National Historic Site: Photo Gallery"); add("Kaloko-Honokōhau National Historical Park: Coral Reef Studies and Products"); add("Tallgrass Prairie National Preserve: Support Your Park"); add("Tallgrass Prairie National Preserve: Planning"); add("George Washington Carver National Monument: Support Your Park");}};

        boolean titlesMatch = resultTitles.containsAll(expectedTitles) && expectedTitles.containsAll(resultTitles);

        assertTrue("The list of document titles should match:\nActual: " +
                resultTitles + "\nExpected: " + expectedTitles, titlesMatch);
    }

    @Test
    public void longPhraseWildcardQueryTest() {
        String query = "\"nation* h*or*cal *ark\"";
        List<String> splitQuery = processor.processToken(query);

        List<String> resultTitles = findTitles(query);
        //List<String> expectedTitles = scanDocuments(splitQuery, "AND");
        List<String> expectedTitles = new ArrayList<>(){{add("Kaloko-Honokōhau National Historical Park: Coral Reef Studies and Products");}};

        boolean titlesMatch = resultTitles.containsAll(expectedTitles) && expectedTitles.containsAll(resultTitles);

        assertTrue("The list of document titles should match:\nActual: " +
                resultTitles + "\nExpected: " + expectedTitles, titlesMatch);
    }
}
