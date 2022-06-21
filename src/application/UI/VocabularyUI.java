package application.UI;

import application.indexes.Index;
import application.indexes.Posting;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;


public class VocabularyUI {
    private static final int VOCABULARY_PRINT_SIZE = 1_000;
    private static Index<String, Posting> indexList;

    //initialization of UI components
    private static JButton returnToSearch = new JButton("Return to Search");
    private static JFrame frame;

    public void setFrame(JFrame window){ frame = window; }

    public void setIndex(Index<String, Posting> index) { indexList = index; }

    public Component vocabularyUI(){
        //initialization of a new panel
        JPanel content = new JPanel();
        //sets the flow of the panel
        BoxLayout box = new BoxLayout(content, BoxLayout.Y_AXIS);
        content.setLayout(box);


        List<String> vocabulary = indexList.getVocabulary();
        int vocabularyPrintSize = Math.min(vocabulary.size(), VOCABULARY_PRINT_SIZE);
        JLabel size = new JLabel();
        //displays the total number of terms
        size.setText("Found " + vocabulary.size() + " terms.");
        content.add(size);

        for (int i = 0; i < vocabularyPrintSize; ++i) {
            //gets the term, adds it to a label, and adds the label to the panel
            JLabel vocab = new JLabel(vocabulary.get(i));
            content.add(vocab);
        }
        if (vocabulary.size() > VOCABULARY_PRINT_SIZE) {
            //label to notify users that they've reached the end of the vocab list.
            JLabel vocab = new JLabel("...");
            content.add(vocab);
        }
        //adds the return to search button to the panel
        content.add(returnToSearch);
        //sets max. size of the panel
        content.setMaximumSize(new Dimension(500,500));
        //initialization of a scroll pane
        JScrollPane scroll = new JScrollPane(content);
        //sets the horizontal scroll policy for the pane
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        //sets the vertical scroll policy for the pane
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        //sets the bounds for the scroll pane
        scroll.setBounds(20,20,660,630);
        //creation of a new panel
        JPanel contentPanel = new JPanel(null);
        //sets the preferred size for the contentPanel panel
        contentPanel.setPreferredSize(new Dimension(500,500));
        //adds the scroll pane to the new contentPanel panel
        contentPanel.add(scroll);
        //sets maximum size for the panel
        contentPanel.setMaximumSize(new Dimension(500,500));
        //adds panel to the frame
        frame.add(contentPanel);

        //action listener that will be enacted if the user clicks on the return to search button
        returnToSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                content.setVisible(false);
                contentPanel.setVisible(false);
                frame.remove(contentPanel);
                Search search = new Search();
                search.setFrame(frame);
                frame.add(search.SearchUI());
            }
        });
        return contentPanel;
    }
}

