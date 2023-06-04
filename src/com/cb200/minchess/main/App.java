package com.cb200.minchess.main;

import javax.swing.SwingUtilities;

import com.cb200.minchess.gui.views.Gui;

public class App {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(() -> {
            Gui.init();
        });
    }
}
