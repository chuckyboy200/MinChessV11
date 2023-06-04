package com.cb200.minchess.gui.components;

import javax.swing.JPanel;

import java.awt.BorderLayout;

public class ControlPanel extends JPanel {
    
    private InfoPanel infoPanel;

    public ControlPanel() {
        setLayout(new BorderLayout());

        // Action panel at the top
        //JPanel actionPanel = new ActionPanel();
        //this.add(actionPanel, BorderLayout.NORTH);

        this.infoPanel = new InfoPanel();
        this.add(infoPanel, BorderLayout.CENTER);
    }

    public void println(String text, boolean upper) {
        print(text + "\n", upper);
    }

    public void print(String text, boolean upper) {
        this.infoPanel.print(text, upper);
    }

}
