package com.cb200.minchess.gui.views;

import com.cb200.minchess.board.Board;
import com.cb200.minchess.gui.components.GameWindow;

public class Gui {
    
    private static GameWindow gameWindow;

    private Gui() {}

    public static final void init() {
        gameWindow = new GameWindow();
    }

    public static final void init(String fen) {
        gameWindow = new GameWindow(fen);
    }

    public static final void println(String text, boolean upper) {
        print(text + "\n", upper);
    }

    public static final void print(String text, boolean upper) {
        gameWindow.print(text, upper);
    }

    public static final void outputFEN() {
        String fen = Board.toFenString(gameWindow.getBoard());
        println(fen, false);
        System.out.println(fen);
    }

}
