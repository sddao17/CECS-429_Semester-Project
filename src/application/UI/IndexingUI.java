package application.UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class IndexingUI {
    JButton returnToSearch = new JButton("Return to Search");
    JFrame frame;

    public void setFrame(JFrame window){ frame = window; }

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
