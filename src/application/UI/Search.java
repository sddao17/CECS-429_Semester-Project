package application.UI;

import application.documents.DirectoryCorpus;
import application.indexes.Index;
import application.indexes.Posting;
import application.text.TokenStemmer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Path;
import java.util.List;

public class Search {
    /* Initialization of the UI components */
    JButton submit= new JButton("Submit");
    JLabel title = new JLabel();
    JLabel indexComplete = new JLabel();
    JLabel showTime = new JLabel();
    JPanel panel = new JPanel();
    JTextField input = new JTextField();
    JRadioButton index = new JRadioButton("Index");
    JRadioButton stem = new JRadioButton("Stem");
    JRadioButton vocab = new JRadioButton("Vocab");
    JRadioButton search = new JRadioButton();

    // takes in the time that has elapsed
    private static double timeElapsed;

    private static final int VOCABULARY_PRINT_SIZE = 1_000;  // number of vocabulary terms to print
    private static DirectoryCorpus corpus;  // we need only one corpus,
    private static Index<String, Posting> index1;  // and one PositionalInvertedIndex
    private static CorpusSelection cSelect;

    /*sets the time that has elapsed that was calculated
    * in the Corpus Selection class */
    public void setTime(double time){
        timeElapsed = time;
    }

    public Component SearchUI(){
        BoxLayout box = new BoxLayout(panel, BoxLayout.Y_AXIS);
        title.setText("Search");
        indexComplete.setText("Indexing complete");
        showTime.setText("Time elapsed: " + timeElapsed + " seconds \n\n");

        panel.setLayout(box);
        input.setPreferredSize(new Dimension(1000,30));
        input.setMaximumSize(input.getPreferredSize());

        panel.add(title);
        panel.add(indexComplete);
        panel.add(showTime);
        panel.add(input);
        panel.add(submit);
        panel.add(stem);
        panel.add(index);
        panel.add(vocab);
        panel.setVisible(true);

        //action when the user will click the submission button.
        submit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String query = input.getText();
                String[] splitQuery = query.split(" ");

                // skip empty input
                if (splitQuery.length > 0) {
                    String potentialCommand = splitQuery[0];
                    String parameter = "";
                    if (splitQuery.length > 1) {
                        parameter = splitQuery[1];
                    }
                    if (index.isSelected()) {
                        cSelect.initializeComponents(Path.of(parameter));
                    } else if (stem.isSelected()) {
                        TokenStemmer stemmer = new TokenStemmer();
                        System.out.println(stemmer.processToken(parameter).get(0));
                    } else if (vocab.isSelected()) {
                        List<String> vocabulary = index1.getVocabulary();
                        int vocabularyPrintSize = Math.min(vocabulary.size(), VOCABULARY_PRINT_SIZE);
                        for (int i = 0; i < vocabularyPrintSize; ++i) {
                            System.out.println(vocabulary.get(i));
                        }
                        if (vocabulary.size() > VOCABULARY_PRINT_SIZE) {
                            System.out.println("...");
                        }
                        System.out.println("Found " + vocabulary.size() + " terms.");
                    } else {

                    }
                }
            }
        });

        /*when the user clicks on the radio button the others will be disabled,
        * once un clicked the others will be enabled again */
        stem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (stem.isSelected()){
                    vocab.setEnabled(false);
                    index.setEnabled(false);
                }
                else {
                    vocab.setEnabled(true);
                    index.setEnabled(true);
                }
            }
        });
        /*when the user clicks on the radio button the others will be disabled,
         * once un clicked the others will be enabled again */
        vocab.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (vocab.isSelected()){
                    stem.setEnabled(false);
                    index.setEnabled(false);
                }
                else {
                    stem.setEnabled(true);
                    index.setEnabled(true);
                }
            }
        });
        /*when the user clicks on the radio button the others will be disabled,
         * once un clicked the others will be enabled again */
        index.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (index.isSelected()){
                    vocab.setEnabled(false);
                    stem.setEnabled(false);
                }
                else {
                    vocab.setEnabled(true);
                    stem.setEnabled(true);
                }
            }
        });

        return panel;
    }



}