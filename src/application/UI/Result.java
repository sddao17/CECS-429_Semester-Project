package application.UI;

import application.documents.DirectoryCorpus;
import application.documents.Document;
import application.documents.DocumentCorpus;
import application.indexes.Index;
import application.indexes.Posting;
import application.queries.BooleanQueryParser;
import application.queries.QueryComponent;
import application.text.EnglishTokenStream;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.*;
import java.util.List;
import java.util.Scanner;

/*Handles the results of the search or requests */
public class Result {

    JButton returnToSearch = new JButton("Return to Search");
    JFrame frame;

    private static final int VOCABULARY_PRINT_SIZE = 1_000;
    private static Index<String, Posting> indexList;
    private static DirectoryCorpus corpus;
    private static String query;

    public void setFrame(JFrame window){ frame = window; }

    public void setIndex(Index<String, Posting> index) { indexList = index; }

    public void setCorpus(DirectoryCorpus currCorpus){ corpus = currCorpus; }

    public void setQuery(String userInput){query = userInput;}

    /* Will display the results of the query to the user*/
    public Component resultsUI(){
        JPanel content = new JPanel();
        BooleanQueryParser parser = new BooleanQueryParser();
        QueryComponent parsedQuery = parser.parseQuery(query);
        List<Posting> resultPostings = parsedQuery.getPostings(indexList);

        JLabel totalDocs = new JLabel("Found " + resultPostings.size() + " documents.");
        content.add(totalDocs);

        for (Posting posting : resultPostings) {
            int currentDocumentId = posting.getDocumentId();
            JLabel doc = new JLabel("- " + corpus.getDocument(currentDocumentId).getTitle() +
                    " (ID: " + currentDocumentId + ")");
            doc.setCursor(new Cursor(Cursor.HAND_CURSOR));
            /*doc.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    content.setVisible(false);
                    JPanel contentDisplay = new JPanel();
                    Document document = corpus.getDocument(Integer.parseInt(query));
                    EnglishTokenStream stream = new EnglishTokenStream(document.getContent());
                    JLabel output = new JLabel();
                    // print the tokens to the console without processing them
                    stream.getTokens().forEach(c -> output.setText(c + " "));
                    contentDisplay.add(output);
                    frame.add(contentDisplay);
                    System.out.println();

                }
            });*/
            content.add(doc);
        }

        content.add(returnToSearch);
        content.setVisible(true);

        returnToSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                content.setVisible(false);
                Search search = new Search();
                search.setFrame(frame);
                frame.add(search.SearchUI());
            }
        });


        return content;


    }

    /* If a user selects the document then the contents will display*/
    public Component displayContent(){
       JPanel content = new JPanel();

       return content;
    }

    public Component stemUI(String stem){
        String result = stem;
        JPanel content = new JPanel();
        JLabel label = new JLabel("Stemming result: " + result);

        content.add(label);
        content.add(returnToSearch);
        content.setVisible(true);

        returnToSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                content.setVisible(false);
                Search search = new Search();
                search.setFrame(frame);
                frame.add(search.SearchUI());
            }
        });
        return content;
    }

    public Component vocabularyUI(){
        JPanel content = new JPanel();

        List<String> vocabulary = indexList.getVocabulary();
        int vocabularyPrintSize = Math.min(vocabulary.size(), VOCABULARY_PRINT_SIZE);
        JLabel size = new JLabel();
        size.setText("Found " + vocabulary.size() + " terms.");
        content.add(size);

        for (int i = 0; i < vocabularyPrintSize; ++i) {
            JLabel vocab = new JLabel(vocabulary.get(i));
            content.add(vocab);
        }
        if (vocabulary.size() > VOCABULARY_PRINT_SIZE) {
            System.out.println("...");
        }

        content.add(returnToSearch);
        JScrollPane scroll = new JScrollPane(content);
        content.setVisible(true);

        returnToSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                content.setVisible(false);
                Search search = new Search();
                search.setFrame(frame);
                frame.add(search.SearchUI());
            }
        });
        return content;
    }

    public Component indexUI(){
        JPanel content = new JPanel();
        JLabel indexComplete = new JLabel();
        indexComplete.setText("Indexing Completed");
        content.add(indexComplete);
        content.add(returnToSearch);
        content.setVisible(true);

        returnToSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                content.setVisible(false);
                Search search = new Search();
                search.setFrame(frame);
                frame.add(search.SearchUI());
            }
        });
        return content;
    }

}