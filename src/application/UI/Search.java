package application.UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Path;

public class Search {

    //Initialization of the frame, button, label, and panel for the UI

    JButton submit= new JButton("Submit");
    JLabel title = new JLabel();
    JLabel indexComplete = new JLabel();
    JLabel showTime = new JLabel();
    JPanel panel = new JPanel();
    JTextField input = new JTextField();
    JRadioButton index = new JRadioButton();
    JRadioButton stem = new JRadioButton();
    JRadioButton vocab = new JRadioButton();
    JRadioButton search = new JRadioButton();


    private double timeElapsed;

    public void Search(double time){
        timeElapsed = time;
    }

    public Component SearchUI(){

        title.setText("Search");
        indexComplete.setText("Indexing complete");
        showTime.setText("Time elapsed: " + timeElapsed);

        panel.add(title);
        panel.add(indexComplete);
        panel.add(showTime);
        panel.add(input);
        panel.add(submit);
        panel.add(stem);
        panel.add(index);
        panel.add(vocab);
        panel.setVisible(true);

        submit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(index.isSelected()){

                }
                else if (stem.isSelected())
                {

                }
                else if (vocab.isSelected()){

                }
                else{

                }
            }
        });

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