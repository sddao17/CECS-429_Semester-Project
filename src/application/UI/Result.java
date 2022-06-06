package application.UI;

import application.indexes.Index;
import application.indexes.Posting;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.*;
import java.util.List;

/*Handles the results of the search or requests */
public class Result {

    JButton returnToSearch = new JButton("Return to Search");
    JFrame frame;

    private static final int VOCABULARY_PRINT_SIZE = 1_000;
    private static Index<String, Posting> indexList;

    public void setFrame(JFrame window){ frame = window; }

    public void setIndex(Index<String, Posting> index) { indexList = index; }

    /* Will display the results of the query to the user*/
    public Component resultsUI(){
        JPanel panel = new JPanel();

        return panel;
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
        for (int i = 0; i < vocabularyPrintSize; ++i) {
            JLabel vocab = new JLabel(vocabulary.get(i));
            content.add(vocab);
        }
        if (vocabulary.size() > VOCABULARY_PRINT_SIZE) {
            System.out.println("...");
        }

        JLabel size = new JLabel();
        size.setText("Found " + vocabulary.size() + " terms.");
        content.add(size);
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
