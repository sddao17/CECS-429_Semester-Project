package application.UI;

import application.documents.DirectoryCorpus;
import application.documents.*;
import application.indexes.Index;
import application.indexes.PositionalInvertedIndex;
import application.indexes.Posting;
import application.text.EnglishTokenStream;
import application.text.TrimSplitTokenProcessor;
import application.UI.Search;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import javax.swing.JPanel;

public class CorpusSelection {

    //Initialization of the frame, button, label, panel, and combo box for the UI
    static JFrame frame = new JFrame();
    JButton submitResult = new JButton("Submit");
    JLabel title = new JLabel();
    JPanel panel = new JPanel();
    JComboBox userSelection = new JComboBox();
    private static Search search = new Search();

    //only one corpus is needed
    private static DirectoryCorpus corpus;
    //and one PositionalInvertedIndex
    private static Index<String, Posting> index;
    private static double elapsedTimeInSeconds;


    public void CorpusSelection(){
        //call to the corpusMenu() method to get the components for the user to select
        corpusMenu();
        //adds the panel that contains the corpus menu into the frame
        frame.add(panel);
        //sets the initial size of the window
        frame.setPreferredSize(new Dimension(700,700));
        frame.setMaximumSize(new Dimension(700,700));

        frame.pack();
        //sets the maximum size of the window
        //frame.setMaximumSize(new Dimension(700,500));
        //sets the location of the window
        frame.setLocationRelativeTo(null);
        //initializes the exit button on the window
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //makes the window visible when the program runs
        frame.setVisible(true);


        //on click the frame will be redirected to the search panel after completion of the indexing.
        submitResult.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Sets the corpus selection panel to no longer be visible after indexing is completed
                panel.setVisible(false);
                frame.remove(panel);

                /* waiting panel
                JPanel p2 = new JPanel();
                JLabel label = new JLabel();
                label.setText("Indexing...");
                p2.add(label);
                p2.setVisible(true);
                frame.add(p2);
                */

                //retrieves the option the user selected from the combobox
                String result = userSelection.getSelectedItem().toString();
                result.toLowerCase();
                //initialization of the directoryPath string to translate next from the result
                String directoryPath;
                //will convert the string to the file path to start indexing
                if(result.equals("Parks")) {
                    directoryPath = "./corpus/parks";
                    initializeComponents(Path.of(directoryPath));
                }
                else if(result.equals("Moby Dick")) {
                    directoryPath = "./corpus/moby-dick";
                    initializeComponents(Path.of(directoryPath));
                }
                else if(result.equals("Parks Test")) {
                    directoryPath = "./corpus/parks-test";
                    initializeComponents(Path.of(directoryPath));
                }
                else {
                    directoryPath = "./corpus/kanye-test";
                    initializeComponents(Path.of(directoryPath));
                }

                //removal of the indexing loading screen
                //frame.remove(p2);

                //After Indexing is completed, the search engine will be displayed.
                frame.add(search.SearchUI());
            }
        });
    }

    public void corpusMenu(){
        BoxLayout box = new BoxLayout(panel, BoxLayout.Y_AXIS);
        //sets the flow of the panel to be displayed on the screen
        panel.setLayout(box);
        //sets the text of the title label
        title.setText("Select a Corpus");
        //adds the title label to the panel
        //title.setAlignmentX(10);
        panel.add(title);
        //array of the selection of corpus's for the user to choose from
        String[] corpus = {"Parks", "Moby Dick", "Parks Test", "Kanye Test"};
        //adding the array as the list of options to the combo box.
        userSelection.setModel(new DefaultComboBoxModel(corpus));
        //userSelection.setPreferredSize(new Dimension(600,40));
        //userSelection.setMaximumSize( userSelection.getPreferredSize());
        //userSelection.setAlignmentX(-1);
        //userSelection.setAlignmentY(100);
        //submitResult.setAlignmentX(200);
        //adds the combo box to the panel
        panel.add(userSelection);
        //adds the submit button to the panel

        panel.add(submitResult);
    }

    public static void initializeComponents(Path directoryPath) {
        corpus = DirectoryCorpus.loadDirectory(directoryPath);
        // by default, our `k` value for k-gram indexes will be set to 3
        index = indexCorpus(corpus);
       // passing the corpus to use for queries
        QueryResult r = new QueryResult();
        r.setCorpus(corpus);
    }

    public static Index<String, Posting> indexCorpus(DocumentCorpus corpus) {
        /* 2. Index all documents in the corpus to build a positional inverted index.
          Print to the screen how long (in seconds) this process takes. */

        long startTime = System.nanoTime();

        TrimSplitTokenProcessor processor = new TrimSplitTokenProcessor();
        PositionalInvertedIndex index = new PositionalInvertedIndex();

        // scan all documents and process each token into terms of our vocabulary
        for (Document document : corpus.getDocuments()) {
            EnglishTokenStream stream = new EnglishTokenStream(document.getContent());
            Iterable<String> tokens = stream.getTokens();
            // at the beginning of each document reading, the position always starts at 1
            int currentPosition = 1;

            for (String token : tokens) {
                // process the token before evaluating whether it exists within our matrix
                List<String> terms = processor.processToken(token);

                // since each token can produce multiple terms, add all terms using the same documentID and position
                for (String term : terms) {
                    index.addTerm(term, document.getId(), currentPosition);
                }
                // after each token addition, update the position count
                ++currentPosition;
            }
        }

        long endTime = System.nanoTime();
        elapsedTimeInSeconds = (double) (endTime - startTime) / 1_000_000_000;
        search.setTime(elapsedTimeInSeconds);
        search.setFrame(frame);
        search.setIndex(index);

        return index;
    }

}
