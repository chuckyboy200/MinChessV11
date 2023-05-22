package com.cb200.minchess.main;

import javax.swing.SwingUtilities;

import com.cb200.minchess.board.Board;
import com.cb200.minchess.gui.GameWindow;

public class App {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(() -> {
            GameWindow gameWindow = new GameWindow();
            gameWindow.update(Board.startingPosition());
        });
    }
}
