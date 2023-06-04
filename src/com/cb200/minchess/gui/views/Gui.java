package com.cb200.minchess.gui.views;

import com.cb200.minchess.gui.components.GameWindow;

public class Gui {
    
    private static GameWindow gameWindow;

    private Gui() {}

    public static final void init() {
        gameWindow = new GameWindow();
    }

    public static final void println(String text, boolean upper) {
        print(text + "\n", upper);
    }

    public static final void print(String text, boolean upper) {
        gameWindow.print(text, upper);
    }

}
