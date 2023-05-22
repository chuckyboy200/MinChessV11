package com.cb200.minchess.gui;

import javax.swing.JFrame;

public class GameWindow {
    
    private JFrame frame;
    private BoardPanel boardPanel;

    public GameWindow() {
        this.frame = new JFrame("MinChess");
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setSize(800, 800);
        this.frame.setResizable(false);

        this.boardPanel = new BoardPanel();

        this.frame.add(this.boardPanel);
        this.frame.setVisible(true);
    }

    public void update(long[] board) {
        this.boardPanel.updateBoard(board);
    }   

}
