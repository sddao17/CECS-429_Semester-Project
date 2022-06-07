package application.UI;

import application.documents.DirectoryCorpus;
import application.indexes.Index;
import application.indexes.Posting;
import application.queries.BooleanQueryParser;
import application.queries.QueryComponent;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.*;
import java.util.List;

/***
 * QueryResult will display the lists of postings that correlate
 * to the users search and will navigate to the contents of that
 * document if the user clicks on it.
 */
public class QueryResult {
    private static final int VOCABULARY_PRINT_SIZE = 1_000;
    private static Index<String, Posting> indexList;
    private static DirectoryCorpus corpus;
    private static String query;
    private static JButton returnToSearch = new JButton("Return to Search");
    private static JPanel content = new JPanel();
    private static JFrame frame;

    public void setFrame(JFrame window){ frame = window; }

    public void setIndex(Index<String, Posting> index) { indexList = index; }

    public void setCorpus(DirectoryCorpus currCorpus){ corpus = currCorpus; }

    public void setQuery(String userInput){query = userInput;}

    /* Will display the results of the query to the user*/
    public Component resultsUI(){

        BoxLayout box = new BoxLayout(content, BoxLayout.Y_AXIS);
        //sets the flow of the panel
        content.setLayout(box);
        //sets the max. size of the panel
        content.setMaximumSize(new Dimension(500,500));
        //initialization of a new panel
        JPanel contentPanel = new JPanel(null);
        //initialization of a scroll pane
        JScrollPane scrollDoc = new JScrollPane(content);
        //sets the horizontal scroll policy for the pan
        scrollDoc.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        //sets the vertical scroll policy for the pane
        scrollDoc.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        //sets the bounds for the scroll pane
        scrollDoc.setBounds(20,20,660,630);

        //will retrieve the documents that correlate to the search
        BooleanQueryParser parser = new BooleanQueryParser();
        QueryComponent parsedQuery = parser.parseQuery(query);
        List<Posting> resultPostings = parsedQuery.getPostings(indexList);

        JLabel totalDocs = new JLabel("Found " + resultPostings.size() + " documents.");
        //adds the documents found to the panel
        content.add(totalDocs);

        //will input each document into a label to be displayed to the user
        for (Posting posting : resultPostings) {
            int currentDocumentId = posting.getDocumentId();
            JLabel doc = new JLabel("- " + corpus.getDocument(currentDocumentId).getTitle() +
                    " (ID: " + currentDocumentId + ")");
            doc.setCursor(new Cursor(Cursor.HAND_CURSOR));
            //when the user clicks on the document, it will redirect them to the contents
            doc.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                        if(e.getClickCount() > 1) {
                            content.setVisible(false);
                            contentPanel.setVisible(false);
                            ContentUI viewDoc = new ContentUI();
                            viewDoc.setCorpus(corpus);
                            viewDoc.setFrame(frame);
                            viewDoc.setCurrentDocumentId(currentDocumentId);
                            viewDoc.contentUI();
                        }
                    }
            });
            //adds the documents to the panel
            content.add(doc);
        }
        //adds the return to search button to the panel
        content.add(returnToSearch);
        //sets the preferred size for the contentPanel panel
        contentPanel.setPreferredSize(new Dimension(500,500));
        //adds the scroll pane to the new contentPanel panel
        contentPanel.add(scrollDoc);
        //sets max. size of the panel
        contentPanel.setMaximumSize(new Dimension(500,500));
        //adds the content panel to the frame
        frame.add(contentPanel);

        //action listener that will be enacted if the user clicks on the return to search button
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
}