package application.UI;

import application.documents.DirectoryCorpus;
import application.documents.Document;
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

/*Handles the results of the search or requests */
public class QueryResult {
    private static final int VOCABULARY_PRINT_SIZE = 1_000;
    private static Index<String, Posting> indexList;
    private static DirectoryCorpus corpus;
    private static String query;

    JButton returnToSearch = new JButton("Return to Search");
    JFrame frame;

    public void setFrame(JFrame window){ frame = window; }

    public void setIndex(Index<String, Posting> index) { indexList = index; }

    public void setCorpus(DirectoryCorpus currCorpus){ corpus = currCorpus; }

    public void setQuery(String userInput){query = userInput;}

    /* Will display the results of the query to the user*/
    public Component resultsUI(){
        JPanel content = new JPanel();
        BoxLayout box = new BoxLayout(content, BoxLayout.Y_AXIS);
        //sets the flow of the panel to be displayed on the screen
        content.setLayout(box);

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

            doc.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {

                        super.mouseClicked(e);
                        content.setVisible(false);
                        JPanel contentDisplay = new JPanel();
                        Document document = corpus.getDocument(currentDocumentId);
                        EnglishTokenStream stream = new EnglishTokenStream(document.getContent());

                        // print the tokens to the console without processing them
                        stream.getTokens().forEach(c -> {
                            JLabel output = new JLabel(c + " ");
                            contentDisplay.add(output);
                        });
                        contentDisplay.add(returnToSearch);
                        contentDisplay.setVisible(true);
                        frame.add(contentDisplay);

                        returnToSearch.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                contentDisplay.setVisible(false);
                                Search search = new Search();
                                search.setFrame(frame);
                                frame.add(search.SearchUI());
                            }
                        });
                    }
                
            });
            content.add(doc);
            content.add(returnToSearch);
        }

        content.setMaximumSize(new Dimension(500,500));
        JScrollPane scrollDoc = new JScrollPane(content);
        scrollDoc.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollDoc.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollDoc.setBounds(20,20,660,630);
        JPanel contentPanel = new JPanel(null);
        contentPanel.setPreferredSize(new Dimension(500,500));
        contentPanel.add(scrollDoc);
        contentPanel.setMaximumSize(new Dimension(500,500));
        frame.add(contentPanel);

        returnToSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                content.setVisible(false);
                contentPanel.setVisible(false);
                Search search = new Search();
                search.setFrame(frame);
                frame.add(search.SearchUI());
            }
        });
        return contentPanel;

    }

    /* If a user selects the document then the contents will display*/
    public Component displayContent(){
       JPanel content = new JPanel();

       return content;
    }

}