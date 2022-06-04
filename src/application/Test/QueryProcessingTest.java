package application.Test;

import application.Application;
import application.documents.DirectoryCorpus;
import application.documents.DocumentCorpus;
import application.indexes.Index;
import application.indexes.Posting;
import application.queries.BooleanQueryParser;
import application.queries.QueryComponent;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static application.Application.indexCorpus;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QueryProcessingTest {


    String directoryPathString = "./corpus/kanye-test";
    String extensionType = ".txt";
    DocumentCorpus testCorpus = DirectoryCorpus.loadTextDirectory(
            Paths.get(directoryPathString).toAbsolutePath(), extensionType);
    Index index = Application.indexCorpus(testCorpus, 3);


    @Test
    public void singleQueryTest(){
        String query = "yeezi";
    }

    @Test
    public void andQueryTest(){
        BooleanQueryParser parser = new BooleanQueryParser();
        String query = "yeezi 350";
        QueryComponent parsedQuery = parser.parseQuery(query);
        List<Posting> resultPostings = parsedQuery.getPostings(index);
        int queryDocumentId = resultPostings.get(0).getDocumentId();
        assertEquals("The Document titles should be the same.",
                "two.txt", testCorpus.getDocument(queryDocumentId).getTitle());
    }

    @Test
    public void orQueryTest(){
        BooleanQueryParser parser = new BooleanQueryParser();
        String query = "la + west";
        QueryComponent parsedQuery = parser.parseQuery(query);
        List<Posting> resultPostings = parsedQuery.getPostings(index);
        List<String> resultTitles = new ArrayList<>();

        for (Posting posting : resultPostings) {
            int currentDocumentId = posting.getDocumentId();

            resultTitles.add(testCorpus.getDocument(currentDocumentId).getTitle());
        }

        List<String> expectedTitles = new ArrayList<>(){{
            add("one.txt");
            add("four.txt");
        }};
        boolean titlesMatch = resultTitles.containsAll(expectedTitles) && expectedTitles.containsAll(resultTitles);

        assertTrue("The list of document titles should match.", titlesMatch);
    }

    @Test
    public void phraseQueryTest(){
        //write tests once phraseQuery is done
    }
}
