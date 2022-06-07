package application.UI;

import application.documents.DirectoryCorpus;
import application.documents.*;
import application.indexes.Index;
import application.indexes.PositionalInvertedIndex;
import application.indexes.Posting;
import application.text.EnglishTokenStream;
import application.indexes.KGramIndex;
import application.text.VocabularyTokenProcessor;
import application.text.WildcardTokenProcessor;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.List;
import javax.swing.JPanel;

/***
 * CorpusSelection allows for the user to select the corpus they want at the start of the program
 */

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
    //time that the indexing took
    private static double elapsedTimeInSeconds;
    //KGramIndex to display to users after completion of indexing
    private static KGramIndex kGramIndex;

    public static Index<String, String> getKGramIndex() {
        return kGramIndex;
    }

    public void CorpusSelectionUI(){
        //call to the corpusMenu() method to get the components for the user to select
        corpusMenu();
        //adds the panel that contains the corpus menu into the frame
        frame.add(panel);
        //sets the initial size of the window
        frame.setPreferredSize(new Dimension(700,700));
        //sets the window to the preferred size
        frame.pack();
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
                //removal of the corpus selection panel
                frame.remove(panel);

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
        panel.add(title);
        //array of the selection of corpus's for the user to choose from
        String[] corpus = {"Parks", "Moby Dick", "Parks Test", "Kanye Test"};
        //adding the array as the list of options to the combo box.
        userSelection.setModel(new DefaultComboBoxModel(corpus));
        //adds the combo box to the panel
        panel.add(userSelection);
        //adds the submit button to the panel
        panel.add(submitResult);
    }

    public static void initializeComponents(Path directoryPath) {
        corpus = DirectoryCorpus.loadDirectory(directoryPath);
        // by default, our `k` value for k-gram indexes will be set to 3
        kGramIndex = new KGramIndex();
        index = indexCorpus(corpus);
       // passing the corpus to use for queries
        QueryResult r = new QueryResult();
        r.setCorpus(corpus);
    }

    public static Index<String, Posting> indexCorpus(DocumentCorpus corpus) {
        /* 2. Index all documents in the corpus to build a positional inverted index.
          Print to the screen how long (in seconds) this process takes. */

        long startTime = System.nanoTime();

        //TrimSplitTokenProcessor processor = new TrimSplitTokenProcessor();
        VocabularyTokenProcessor processor = new VocabularyTokenProcessor();
        PositionalInvertedIndex index = new PositionalInvertedIndex();

        // scan all documents and process each token into terms of our vocabulary
        for (Document document : corpus.getDocuments()) {
            EnglishTokenStream stream = new EnglishTokenStream(document.getContent());
            Iterable<String> tokens = stream.getTokens();
            // at the beginning of each document reading, the position always starts at 1
            int currentPosition = 1;

            for (String token : tokens) {
                // before we normalize the token, add it to a minimally processed vocabulary for wildcards
                WildcardTokenProcessor wildcardProcessor = new WildcardTokenProcessor();
                String wildcardToken = wildcardProcessor.processToken(token).get(0);

                // add each unprocessed token to our k-gram index as we traverse through the documents
                kGramIndex.addToken(wildcardToken, 3);

                // process the token before evaluating whether it exists within our index
                List<String> terms = processor.processToken(token);

                // since each token can produce multiple terms, add all terms using the same documentID and position
                for (String term : terms) {
                    index.addTerm(term, document.getId(), currentPosition);
                }
                // after each token addition, update the position count
                ++currentPosition;
            }
        }

        //calculation of elapsed time
        long endTime = System.nanoTime();
        elapsedTimeInSeconds = (double) (endTime - startTime) / 1_000_000_000;
        //passing to the search class to be displayed when on the search engine component
        search.setTime(elapsedTimeInSeconds);
        search.setFrame(frame);
        search.setIndex(index);
        search.setKGramIndex(kGramIndex);

        return index;
    }
}