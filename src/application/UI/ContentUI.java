package application.UI;

import application.documents.DirectoryCorpus;
import application.documents.Document;
import application.text.EnglishTokenStream;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ContentUI {
    private static DirectoryCorpus corpus;
    private static int idNum;
    JButton returnToSearch = new JButton("Return to Search");
    JFrame frame;

    public void setCorpus(DirectoryCorpus currCorpus){ corpus = currCorpus; }

    public void setCurrentDocumentId(Integer documentId){ idNum = documentId;}

    public void setFrame(JFrame window){ frame = window; }

    public Component contentUI(){
        JPanel content = new JPanel();
        FlowLayout flow = new FlowLayout();
        content.setLayout(flow);

        content.setPreferredSize(new Dimension(700,700));
        //content.setMaximumSize(new Dimension(500,500));
        Document document = corpus.getDocument(idNum);
        EnglishTokenStream stream = new EnglishTokenStream(document.getContent());

        content.add(returnToSearch);
        // print the tokens to the console without processing them
        stream.getTokens().forEach(c -> {
            JLabel output = new JLabel(c + " ");
            content.add(output);

        });

        //content.setMaximumSize(new Dimension(400,400));
        JScrollPane scroll = new JScrollPane(content);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setBounds(20,20,700,700);
        JPanel contentPanel = new JPanel(null);
        contentPanel.setPreferredSize(new Dimension(700,700));
        contentPanel.add(scroll);
        //contentPanel.setMaximumSize(new Dimension(500,500));
        frame.add(contentPanel);

        returnToSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                content.setVisible(false);
                contentPanel.setVisible(false);
                Search search = new Search();
                search.setFrame(frame);
                frame.add(search.SearchUI());
            }
        });

        return contentPanel;

    }
}