package com.cb200.minchess.main;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.cb200.minchess.gui.views.Gui;
import com.cb200.minchess.perft.Perft;

public class App {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(() -> {
            Gui.init();
            //performPerft();
        });
    }

    private static void performPerft() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1"
                Perft.fen("r3k2r/p1ppqpb1/bn2pnp1/3P4/1pN1P3/2N2Q1p/PPPBBPPP/R3K2R b KQkq - 0 1", 3);
                return null;
            }
        };
        worker.execute();
    }

}
