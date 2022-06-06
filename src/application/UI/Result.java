package application.UI;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.*;

/*Handles the results of the search or requests */
public class Result {

    JButton returnToSearch = new JButton("Return to Search");
    JFrame frame;

    public void setFrame(JFrame window){ frame = window; }

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
        return content;
    }

    public Component indexUI(){
        JPanel content = new JPanel();
        return content;
    }
}
