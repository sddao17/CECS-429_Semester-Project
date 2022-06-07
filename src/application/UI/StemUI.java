package application.UI;

import application.text.TokenStemmer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StemUI {
    JButton returnToSearch = new JButton("Return to Search");
    JFrame frame;
    String parameter;

    public void setFrame(JFrame window){ frame = window; }

    public void setParameter(String str){ parameter = str; }

    public Component stemUI(){
        TokenStemmer stemmer = new TokenStemmer();
        System.out.println(stemmer.processToken(parameter).get(0));
        String result = (stemmer.processToken(parameter).get(0));

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
}