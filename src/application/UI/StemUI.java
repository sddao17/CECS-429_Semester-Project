package application.UI;

import application.text.TokenStemmer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StemUI {
    //initialization of UI components
    private static JButton returnToSearch = new JButton("Return to Search");
    private static JFrame frame;
    private static String parameter;

    public void setFrame(JFrame window){ frame = window; }

    public void setParameter(String str){ parameter = str; }

    public Component stemUI(){
        //takes the user input and processes it through the token stemmer
        TokenStemmer stemmer = new TokenStemmer();
        System.out.println(stemmer.processToken(parameter).get(0));
        String result = (stemmer.processToken(parameter).get(0));
        //initialization of a new panel
        JPanel content = new JPanel();
        //initialization of a new label
        JLabel label = new JLabel("Stemming result: " + result);
        //adds the label to the panel
        content.add(label);
        //adds return to search button to the panel
        content.add(returnToSearch);
        //makes the panel visible
        content.setVisible(true);

        //action listener that will be enacted if the user clicks on the return to search button
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