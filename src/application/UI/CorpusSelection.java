package application.UI;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.JPanel;


public class CorpusSelection {

    //Initialization of the frame, button, label, and panel for the UI
    JFrame frame = new JFrame();
    JButton submitResult = new JButton("Submit");
    JLabel title = new JLabel();
    JPanel panel = new JPanel();

    public void guiSettings(){
        //call to the corpusMenu() method to get the components for the user to select
        corpusMenu();
        //adds the panel that contains the corpus menu into the frame
        frame.add(panel);
        //sets the initial size of the window
        frame.setSize(700, 500);
        //sets the maximum size of the window
        frame.setMaximumSize(new Dimension(700,800));
        //sets the location of the window
        frame.setLocationRelativeTo(null);
        //initializes the exit button on the window
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //makes the window visible when the program runs
        frame.setVisible(true);
    }

    public void corpusMenu(){

        //sets the flow of the panel to be displayed on the screen
        panel.setLayout(new FlowLayout());
        //sets the text of the title label
        title.setText("Select a Corpus");
        //adds the title label to the panel
        panel.add(title);
        //array of the selection of corpus's for the user to choose from
        String[] corpus = {"Parks", "Moby Dick", "Parks Test", "Kanye Test"};
        //initialization of the combo box for the user to select the corpus
        JComboBox userSelection = new JComboBox(corpus);
        //adds the combo box to the panel
        panel.add(userSelection);
        //adds the submit button to the panel
        panel.add(submitResult);
        
    }

}
