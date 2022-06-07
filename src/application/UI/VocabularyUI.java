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

    JButton returnToSearch = new JButton("Return to Search");
    JFrame frame;

    public void setFrame(JFrame window){ frame = window; }

    public void setIndex(Index<String, Posting> index) { indexList = index; }

    public Component vocabularyUI(){
        JPanel content = new JPanel();
        //content.setPreferredSize(new Dimension(700,500));
        //JScrollPane scrollPane = new JScrollPane((content, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        BoxLayout box = new BoxLayout(content, BoxLayout.Y_AXIS);
        //sets the flow of the panel to be displayed on the screen
        content.setLayout(box);

        List<String> vocabulary = indexList.getVocabulary();
        int vocabularyPrintSize = Math.min(vocabulary.size(), VOCABULARY_PRINT_SIZE);
        JLabel size = new JLabel();
        size.setText("Found " + vocabulary.size() + " terms.");
        content.add(size);

        for (int i = 0; i < vocabularyPrintSize; ++i) {
            JLabel vocab = new JLabel(vocabulary.get(i));
            content.add(vocab);
            //vocab.setLocation(50,0);
        }
        if (vocabulary.size() > VOCABULARY_PRINT_SIZE) {
            System.out.println("...");
        }

        content.add(returnToSearch);
        content.setMaximumSize(new Dimension(500,500));
        JScrollPane scroll = new JScrollPane(content);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setBounds(20,20,660,630);
        JPanel contentPanel = new JPanel(null);
        contentPanel.setPreferredSize(new Dimension(500,500));
        contentPanel.add(scroll);
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


}
