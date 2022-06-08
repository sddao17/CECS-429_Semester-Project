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

    /**
     * Since our test corpus is relatively small, we can scan the documents one at a time and see if each token
     * matches our regex; if it does, then we can add it to our list of expected titles.
     */
    public List<String> scanDocuments(String query, String queryType) {
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

            switch (queryType) {
                case ("OR") -> {
                    String[] splitQuery = query.split(" \\+ ");
                    for (String subQuery : splitQuery) {
                        if (textMatchesAndOrQuery(parsedContent, subQuery) &&
                                !documentTitles.contains(document.getTitle())) {
                            documentTitles.add(document.getTitle());
                        }
                    }
                }
                case ("AND") -> {
                    String[] splitQuery = query.split(" ");
                    boolean allExist = true;

                    for (String subQuery : splitQuery) {
                        if (!textMatchesAndOrQuery(parsedContent, subQuery)) {
                            allExist = false;
                            break;
                        }
                    }

                    if (allExist && !documentTitles.contains(document.getTitle())) {
                        documentTitles.add(document.getTitle());
                    }
                }
                case ("PHRASE") -> {
                    if (textMatchesWildcardQuery(parsedContent, query) &&
                            !documentTitles.contains(document.getTitle())) {
                        documentTitles.add(document.getTitle());
                    }
                }
            }
        }

        return documentTitles;
    }

    public boolean textMatchesAndOrQuery(String text, String query) {
        String[] candidateTokens = text.split(" ");
        for (String candidateToken : candidateTokens) {
            /* if we've matched the number of split tokens (separated by asterisks) within the original token
              then the token fulfills the pattern */
            if (wildcardExists(candidateToken, query)) {
                return true;
            }
        }

        return false;
    }

    public boolean textMatchesWildcardQuery(String text, String fullQuery) {
        String[] candidateTokens = text.split(" ");
        String[] splitQuery = fullQuery.split(" ");
        for (String candidateToken : candidateTokens) {
            int consecutiveCount = 0;

            for (String query : splitQuery) {
                /* if we've matched the number of split tokens (separated by asterisks) within the original token
                  then the token fulfills the pattern */
                if (wildcardExists(candidateToken, query)) {
                    consecutiveCount += 1;
                } else {
                    break;
                }
            }

            if (consecutiveCount == splitQuery.length) {
                return true;
            }
        }

        return false;
    }

    public boolean wildcardExists(String candidateToken, String query) {
        int startIndex = 0;
        int endIndex = 0;
        int textIndex = 0;

        /* traverse through the original `query`; for every substring leading to an asterisk, verify
          that the original token's substrings exists in the same order within the candidate token */
        while (startIndex < query.length()) {
            char currentChar = query.charAt(endIndex);

            // stop incrementing the right bound when reached either an asterisk or the last character
            while (currentChar != '*' && endIndex < query.length() - 1) {
                ++endIndex;
                currentChar = query.charAt(endIndex);
            }
            if (endIndex == query.length() - 1 && currentChar != '*') {
                ++endIndex;
            }

            // if there is an asterisk at the first index, skip to the next substring
            if (query.charAt(startIndex) != '*') {
                String tokenSubString = query.substring(startIndex, endIndex);
                int currentCandidateIndex = candidateToken.indexOf(tokenSubString, textIndex);

                if (currentCandidateIndex < 0) {
                    return false;
                } else {
                    textIndex = currentCandidateIndex + tokenSubString.length();
                }
            }
            // increment towards the end of the string
            endIndex += 1;
            startIndex = endIndex;
        }

        return true;
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

        List<String> resultTitles = findTitles(query);
        List<String> expectedTitles = scanDocuments(query, "OR");
        System.out.println("Result size: " + resultTitles.size());
        System.out.println("Expected size: " + expectedTitles.size());

        boolean titlesMatch = resultTitles.containsAll(expectedTitles) && expectedTitles.containsAll(resultTitles);

        assertTrue("The list of document titles should match:\nActual: " +
                resultTitles + "\nExpected: " + expectedTitles, titlesMatch);
    }

    @Test
    public void singleLeadingWildcardQuery() {
        String query = "*nal";

        List<String> resultTitles = findTitles(query);
        List<String> expectedTitles = new ArrayList<>(){{add("Sand Creek Massacre National Historic Site: Photo Gallery"); add("Kalaupapa National Historical Park: Joseph Dutton"); add("Kaloko-Honokōhau National Historical Park: Coral Reef Activities and Staff"); add("Kaloko-Honokōhau National Historical Park: Coral Reef Studies and Products"); add("Santa Fe National Historic Trail: Site Identification - Entrance"); add("Tallgrass Prairie National Preserve: Support Your Park"); add("George Washington Carver National Monument: Cooperating Association"); add("Tallgrass Prairie National Preserve: Work With Us"); add("Tallgrass Prairie National Preserve: Planning"); add("George Washington Carver National Monument: Support Your Park");}};
        System.out.println("Result size: " + resultTitles.size());
        System.out.println("Expected size: " + expectedTitles.size());

        boolean titlesMatch = resultTitles.containsAll(expectedTitles) && expectedTitles.containsAll(resultTitles);

        assertTrue("The list of document titles should match:\nActual: " +
                resultTitles + "\nExpected: " + expectedTitles, titlesMatch);
    }

    @Test
    public void singleOrWildcardQuery() {
        String query = "washing* + visitor";

        List<String> resultTitles = findTitles(query);
        List<String> expectedTitles = scanDocuments(query, "OR");
        System.out.println("Result size: " + resultTitles.size());
        System.out.println("Expected size: " + expectedTitles.size());

        boolean titlesMatch = resultTitles.containsAll(expectedTitles) && expectedTitles.containsAll(resultTitles);

        assertTrue("The list of document titles should match:\nActual: " +
                resultTitles + "\nExpected: " + expectedTitles, titlesMatch);
    }

    @Test
    public void longOrWildcardQuery() {
        String query = "i* + th*s + wash* + *ad";

        List<String> resultTitles = findTitles(query);
        List<String> expectedTitles = scanDocuments(query, "OR");
        System.out.println("Result size: " + resultTitles.size());
        System.out.println("Expected size: " + expectedTitles.size());

        boolean titlesMatch = resultTitles.containsAll(expectedTitles) && expectedTitles.containsAll(resultTitles);

        assertTrue("The list of document titles should match:\nActual: " +
                resultTitles + "\nExpected: " + expectedTitles, titlesMatch);
    }

    @Test
    public void singleAndWildcardQueryTest() {
        String query = "mass*re hist*";

        List<String> resultTitles = findTitles(query);
        List<String> expectedTitles = scanDocuments(query, "AND");
        System.out.println("Result size: " + resultTitles.size());
        System.out.println("Expected size: " + expectedTitles.size());

        boolean titlesMatch = resultTitles.containsAll(expectedTitles) && expectedTitles.containsAll(resultTitles);

        assertTrue("The list of document titles should match:\nActual: " +
                resultTitles + "\nExpected: " + expectedTitles, titlesMatch);
    }

    @Test
    public void longAndWildcardQueryTest() {
        String query = "na*t*l *ark an* *he";

        List<String> resultTitles = findTitles(query);
        List<String> expectedTitles = new ArrayList<>(){{add("Sand Creek Massacre National Historic Site: Photo Gallery"); add("Kalaupapa National Historical Park: Joseph Dutton"); add("Kaloko-Honokōhau National Historical Park: Coral Reef Studies and Products"); add("Tallgrass Prairie National Preserve: Support Your Park"); add("Tallgrass Prairie National Preserve: Planning");}};
        System.out.println("Result size: " + resultTitles.size());
        System.out.println("Expected size: " + expectedTitles.size());

        boolean titlesMatch = resultTitles.containsAll(expectedTitles) && expectedTitles.containsAll(resultTitles);

        assertTrue("The list of document titles should match:\nActual: " +
                resultTitles + "\nExpected: " + expectedTitles, titlesMatch);
    }

    @Test
    public void singlePhraseWildcardQueryTest() {
        String query = "\"nation* *ark\"";

        List<String> resultTitles = findTitles(query);
        List<String> expectedTitles = new ArrayList<>(){{add("Sand Creek Massacre National Historic Site: Photo Gallery"); add("Kaloko-Honokōhau National Historical Park: Coral Reef Studies and Products"); add("Tallgrass Prairie National Preserve: Support Your Park"); add("Tallgrass Prairie National Preserve: Planning"); add("George Washington Carver National Monument: Support Your Park");}};
        System.out.println("Result size: " + resultTitles.size());
        System.out.println("Expected size: " + expectedTitles.size());

        boolean titlesMatch = resultTitles.containsAll(expectedTitles) && expectedTitles.containsAll(resultTitles);

        assertTrue("The list of document titles should match:\nActual: " +
                resultTitles + "\nExpected: " + expectedTitles, titlesMatch);
    }

    @Test
    public void longPhraseWildcardQueryTest() {
        String query = "\"nation* h*or*cal *ark\"";

        List<String> resultTitles = findTitles(query);
        List<String> expectedTitles = new ArrayList<>(){{add("Kaloko-Honokōhau National Historical Park: Coral Reef Studies and Products");}};
        System.out.println("Result size: " + resultTitles.size());
        System.out.println("Expected size: " + expectedTitles.size());

        boolean titlesMatch = resultTitles.containsAll(expectedTitles) && expectedTitles.containsAll(resultTitles);

        assertTrue("The list of document titles should match:\nActual: " +
                resultTitles + "\nExpected: " + expectedTitles, titlesMatch);
    }
}
