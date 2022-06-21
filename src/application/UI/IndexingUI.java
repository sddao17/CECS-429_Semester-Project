package application.UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/***
 * IndexingUI will display a new frame to the user once the indexing has completed if
 * they chose to change it after the initial indexing.
 */
public class IndexingUI {
    //initialization of UI components
    private static JButton returnToSearch = new JButton("Return to Search");
    private static JFrame frame;

    public void setFrame(JFrame window){ frame = window; }

    public Component indexUI(){
        //initialization of a new panel
        JPanel content = new JPanel();
        //initialization of a new label
        JLabel indexComplete = new JLabel();
        indexComplete.setText("Indexing Completed");
        //adding the label to the panel
        content.add(indexComplete);
        //adding the return to search button to the panel
        content.add(returnToSearch);
        //setting the content panel to be visible to the user
        content.setVisible(true);

        /* when the return to search button is clicked the user will be
        * returned to the search engine UI*/
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
