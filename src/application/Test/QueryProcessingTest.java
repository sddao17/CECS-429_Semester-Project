
package application.Test;

import application.documents.DirectoryCorpus;
import application.documents.DocumentCorpus;
import application.indexes.Index;
import application.indexes.Posting;
import application.queries.BooleanQueryParser;
import application.queries.QueryComponent;
import application.text.QueryTokenProcessor;
import application.utilities.PostingUtility;
import org.junit.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static application.Application.indexCorpus;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QueryProcessingTest {


    String directoryPath = "./corpus/kanye-test";
    Map<String, String> indexPaths = PostingUtility.createIndexPathsMap(directoryPath);
    DocumentCorpus testCorpus = DirectoryCorpus.loadDirectory(Path.of(directoryPath), false);
    Index<String, Posting> index = indexCorpus(testCorpus, indexPaths);
    BooleanQueryParser parser = new BooleanQueryParser();

    public List<String> ResultTitles(String query){
        QueryComponent parsedQuery = parser.parseQuery(query);
        List<Posting> resultPostings = parsedQuery.getPostings(index, new QueryTokenProcessor());
        List<String> resultTitles = new ArrayList<>();
        for (Posting posting : resultPostings) {
            int currentDocumentId = posting.getDocumentId();

            resultTitles.add(testCorpus.getDocument(currentDocumentId).getTitle());
        }
        return resultTitles;

    }

    @Test
    public void singleQueryTest(){
        List<String> resultTitles = ResultTitles("yeezy");

        List<String> expectedTitles = new ArrayList<>(){{
            add("two.txt");
            add("three.txt");
            add("five.txt");
        }};
        boolean titlesMatch = resultTitles.containsAll(expectedTitles) && expectedTitles.containsAll(resultTitles);

        assertTrue("The list of document titles should match.", titlesMatch);
    }

    @Test
    public void andQueryTest(){
        String query = "yeezy 350";
        QueryComponent parsedQuery = parser.parseQuery(query);
        List<Posting> resultPostings = parsedQuery.getPostings(index, new QueryTokenProcessor());
        int queryDocumentId = resultPostings.get(0).getDocumentId();
        assertEquals("The Document titles should be the same.",
                "two.txt", testCorpus.getDocument(queryDocumentId).getTitle());
    }

    @Test
    public void orQueryTest(){
        List<String> resultTitles = ResultTitles("la + west");
        List<String> expectedTitles = new ArrayList<>(){{
            add("one.txt");
            add("four.txt");
        }};
        boolean titlesMatch = resultTitles.containsAll(expectedTitles) && expectedTitles.containsAll(resultTitles);

        assertTrue("The list of document titles should match.", titlesMatch);
    }

    @Test
    public void phraseQueryTest(){
        String query = "\"no more parties in la\"";
        List<String> resultTitles = ResultTitles(query);
        List<String> expectedTitles = new ArrayList<>(){{
            add("one.txt");
        }};
        boolean titlesMatch = resultTitles.containsAll(expectedTitles) && expectedTitles.containsAll(resultTitles);

        assertTrue("The list of document titles should match.", titlesMatch);
    }
}
