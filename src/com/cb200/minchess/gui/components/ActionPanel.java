package com.cb200.minchess.gui.components;

import javax.swing.JButton;
import javax.swing.JPanel;

public class ActionPanel extends JPanel {
    
    JButton switchControlButton;
    JButton undoMoveButton;
    JButton redoMoveButton;
    JButton adjustSearchDepthButton;
    JButton haltSearchButton;

    public ActionPanel() {
        switchControlButton = new JButton("Switch Control");
        undoMoveButton = new JButton("Undo Move");
        redoMoveButton = new JButton("Redo Move");
        adjustSearchDepthButton = new JButton("Adjust Search Depth");
        haltSearchButton = new JButton("Halt Search");

        this.add(switchControlButton);
        this.add(undoMoveButton);
        this.add(redoMoveButton);
        this.add(adjustSearchDepthButton);
        this.add(haltSearchButton);
    }

}
