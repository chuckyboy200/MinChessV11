package com.cb200.minchess.gui.components;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

public class InfoPanel extends JPanel {
    
    JScrollPane upperPanel;
    JTextArea upperTextArea;
    JScrollPane lowerPanel;
    JTextArea lowerTextArea;

    public InfoPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        upperTextArea = new JTextArea();
        upperTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        DefaultCaret caret = (DefaultCaret) upperTextArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        upperTextArea.setLineWrap(false);
        upperTextArea.setWrapStyleWord(true);
        upperTextArea.setEditable(false);
        upperPanel = new JScrollPane(upperTextArea);
        lowerTextArea = new JTextArea();
        lowerTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        caret = (DefaultCaret) lowerTextArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        lowerTextArea.setLineWrap(false);
        lowerTextArea.setWrapStyleWord(true);
        lowerTextArea.setEditable(false);
        lowerPanel = new JScrollPane(lowerTextArea);

        // Make move history panel take up 1/3 of the height and make it scrollable
        upperPanel.setPreferredSize(new Dimension(400, 200));
        lowerPanel.setPreferredSize(new Dimension(400, 600));
        this.add(upperPanel);
        this.add(lowerPanel);
    }

    public void print(String text, boolean upper) {
        if(upper) { upperTextArea.append(text); }
        else { lowerTextArea.append(text); }
    }

    public void println(String text, boolean upper) {
        print(text + "\n", upper);
    }

}
