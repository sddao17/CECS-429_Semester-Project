package application.UI;

import application.documents.DirectoryCorpus;
import application.documents.Document;
import application.text.EnglishTokenStream;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * ContentUI will display the content of the document to the user
 * after they have selected it from the list of documents.
 */
public class ContentUI {

    private static DirectoryCorpus corpus;
    private static int idNum;
    private static JButton returnToSearch = new JButton("Return to Search");
    private static JFrame frame;

    public void setCorpus(DirectoryCorpus currCorpus){ corpus = currCorpus; }

    public void setCurrentDocumentId(Integer documentId){ idNum = documentId;}

    public void setFrame(JFrame window){ frame = window; }

    /*frame component that the user will be able to view */
    public Component contentUI(){
        //initialization of a new panel to display the components on
        JPanel content = new JPanel();
        //initialization of the layout, and applying it to the panel
        FlowLayout flow = new FlowLayout();
        content.setLayout(flow);
        //setting the preferred size for the panel
        content.setPreferredSize(new Dimension(700,700));
        //retrieving the document based on that was selected by the user by retrieving the documentID
        Document document = corpus.getDocument(idNum);
        //initialization of a EnglishTokenStream to get the contents of the document
        EnglishTokenStream stream = new EnglishTokenStream(document.getContent());
        //adding the return to search button to the panel
        content.add(returnToSearch);
        // gets the contents of the document, sets them as a label, and adds the label to the panel
        stream.getTokens().forEach(c -> {
            JLabel output = new JLabel(c + " ");
            content.add(output);
        });

        //initialization of a scroll pane
        JScrollPane scroll = new JScrollPane(content);
        //sets the horizontal scroll policy for the pane
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        //sets the vertical scroll policy for the pane
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        //sets the bounds for the scroll pane
        scroll.setBounds(20,20,700,700);
        //creation of a new panel
        JPanel contentPanel = new JPanel(null);
        //sets the preferred size for the contentPanel panel
        contentPanel.setPreferredSize(new Dimension(700,700));
        //adds the scroll pane to the new contentPanel panel
        contentPanel.add(scroll);
        //adds the content panel to the frame
        frame.add(contentPanel);

        //action listener that will be enacted if the user clicks on the return to search button
        returnToSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //hides the panels that were used to view the documents contents
                content.setVisible(false);
                contentPanel.setVisible(false);
                //frame.remove(contentPanel);
                //calls the search class and adds the Search UI to frame to revert back.
                Search search = new Search();
                search.setFrame(frame);
                frame.add(search.SearchUI());
            }
        });

        return contentPanel;

    }
}
