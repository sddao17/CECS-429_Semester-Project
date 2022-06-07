package application.UI;

import application.documents.DirectoryCorpus;
import application.indexes.Index;
import application.indexes.KGramIndex;
import application.indexes.Posting;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Path;

/***
 * Search will display the search engine UI to the users, including the
 * special cases that the user can do.
 */
public class Search {
    /* Initialization of the UI components */
    private static JButton submit= new JButton("Submit");
    private static JLabel title = new JLabel();
    private static JLabel kIndex = new JLabel();
    private static JLabel indexComplete = new JLabel();
    private static JLabel showTime = new JLabel();
    private static JPanel panel = new JPanel();
    private static JTextField input = new JTextField();
    private static JRadioButton index = new JRadioButton("Index");
    private static JRadioButton stem = new JRadioButton("Stem");
    private static JRadioButton vocab = new JRadioButton("Vocab");
    private static JFrame frame;

    //takes in the users input for the query
    private static String query;
    //the directory path for indexing a set of documents
    private static String directoryPath;
    // takes in the time that has elapsed
    private static double timeElapsed;
    //vocabulary size
    private static final int VOCABULARY_PRINT_SIZE = 1_000;  // number of vocabulary terms to print
    private static DirectoryCorpus corpus;  // we need only one corpus,
    private static Index<String, Posting> indexList;  // and one PositionalInvertedIndex
    private static CorpusSelection cSelect;
    private static KGramIndex KGram;
    private static QueryResult r = new QueryResult();
    private static StemUI stemming = new StemUI();
    private static IndexingUI indexing = new IndexingUI();
    private static VocabularyUI v = new VocabularyUI();


    /*sets the time that has elapsed that was calculated
    * in the Corpus Selection class */
    public void setTime(double time){
        timeElapsed = time;
    }

    public void setFrame(JFrame window){ frame = window; }

    public void setIndex(Index<String, Posting> index) { indexList = index; }

    public void setKGramIndex(KGramIndex k){ KGram = k; }

    public Component SearchUI(){
        //sets the layout for the panel
        BoxLayout box = new BoxLayout(panel, BoxLayout.Y_AXIS);
        panel.setLayout(box);
        //sets text for several of the labels
        title.setText("Search");
        indexComplete.setText("Indexing complete");
        kIndex.setText("Distinct k-grams: "+ KGram.getDistinctKGrams().size());
        showTime.setText("Time elapsed: " + timeElapsed + " seconds \n\n");

        //sets preferred size for the text field
        input.setPreferredSize(new Dimension(1000,30));
        //sets maximum size for the text field
        input.setMaximumSize(input.getPreferredSize());
        //adds components to the panel
        panel.add(title);
        panel.add(indexComplete);
        panel.add(kIndex);
        panel.add(showTime);
        panel.add(input);
        panel.add(stem);
        panel.add(index);
        panel.add(vocab);
        panel.add(submit);
        panel.setVisible(true);

        //action when the user will click the submit button.
        submit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                query = input.getText();
                String[] splitQuery = query.split(" ");


                if (splitQuery.length > 0) {
                    String parameter = splitQuery[0];
                    if (splitQuery.length > 1) {
                        parameter = splitQuery[0];
                    }

                    // 3(a, i). If it is a special query, perform that action.
                        if( index.isSelected() ) {
                            panel.setVisible(false);
                            directoryPath = query.toLowerCase();
                            cSelect.initializeComponents(Path.of(directoryPath));
                            indexing.setFrame(frame);
                            frame.add(indexing.indexUI());
                        }
                        else if ( stem.isSelected() ) {
                            panel.setVisible(false);
                            stemming.setParameter(parameter);
                            stemming.setFrame(frame);
                            frame.add(stemming.stemUI());
                        }
                        else if ( vocab.isSelected() ) {
                            panel.setVisible(false);
                            v.setIndex(indexList);
                            v.setFrame(frame);
                            v.vocabularyUI();
                        }
                        else{
                           panel.setVisible(false);
                           r.setQuery(query);
                           r.setIndex(indexList);
                           r.setFrame(frame);
                           r.resultsUI();
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