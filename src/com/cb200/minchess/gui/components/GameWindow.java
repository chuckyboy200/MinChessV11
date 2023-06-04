package com.cb200.minchess.gui.components;

import javax.swing.JFrame;

import com.cb200.minchess.board.Board;

import java.awt.BorderLayout;
import java.awt.Dimension;

public class GameWindow extends JFrame {

    private static final int WINDOW_WIDTH = 1600;
    private static final int WINDOW_HEIGHT = 900;

    private ChessBoard chessBoard;
    private ControlPanel controlPanel;

    public GameWindow() {
        super("MinChess");
        setLayout(new BorderLayout());
        setSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        long[] board = Board.startingPosition();
        chessBoard = new ChessBoard(board);
        this.getContentPane().add(chessBoard, BorderLayout.WEST);
        controlPanel = new ControlPanel();
        this.getContentPane().add(controlPanel, BorderLayout.NORTH);
        this.getContentPane().add(controlPanel, BorderLayout.EAST);
        this.setJMenuBar(new MenuBar());
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }

    public void println(String text, boolean upper) {
        this.controlPanel.println(text + "\n", upper);
    }

    public void print(String text, boolean upper) {
        this.controlPanel.print(text, upper);
    }

}
