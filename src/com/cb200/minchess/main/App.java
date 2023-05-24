package com.cb200.minchess.main;

import javax.swing.SwingUtilities;

import com.cb200.minchess.perft.Perft;

public class App {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(() -> {
            //Perft2.fen("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1", "g1h1 b2a1qd1a1 ", 1);
            Perft.all();
        });
    }
}
