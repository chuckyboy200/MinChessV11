package com.cb200.minchess.gui.components;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingWorker;

import com.cb200.minchess.board.Board;
import com.cb200.minchess.gui.views.Gui;
import com.cb200.minchess.perft.Perft;

public class MenuBar extends JMenuBar {
    
    public MenuBar() {
        JMenu fileMenu = new JMenu("File");
        JMenuItem fileMenuPlaceholder = new JMenuItem("Quit");
        fileMenu.add(fileMenuPlaceholder);
        fileMenuPlaceholder.addActionListener(e -> System.exit(0));

        JMenu editMenu = new JMenu("Players");
        JMenuItem editMenuWhitePlayer = new JMenuItem("White Player");
        JMenuItem editMenuBlackPlayer = new JMenuItem("Black Player");
        JMenuItem editMenuSelfPlay = new JMenuItem("Self Play");
        editMenu.add(editMenuWhitePlayer);
        editMenu.add(editMenuBlackPlayer);
        editMenu.add(editMenuSelfPlay);
        editMenuWhitePlayer.addActionListener(e -> System.out.println("Edit -> White Player"));
        editMenuBlackPlayer.addActionListener(e -> System.out.println("Edit -> Black Player"));
        editMenuSelfPlay.addActionListener(e -> System.out.println("Edit -> Self Play"));

        JMenu gameplayMenu = new JMenu("Gameplay");
        JMenuItem gameplayMenuNewGame = new JMenuItem("New Game");
        JMenuItem gameplayMenuUndoMove = new JMenuItem("Undo Move");
        JMenuItem gameplayMenuRedoMove = new JMenuItem("Redo Move");
        gameplayMenu.add(gameplayMenuNewGame);
        gameplayMenu.add(gameplayMenuUndoMove);
        gameplayMenu.add(gameplayMenuRedoMove);

        JMenu toolsMenu = new JMenu("Tools");
        JMenuItem toolsMenuAnalysis = new JMenuItem("Analysis");
        JMenuItem toolsMenuPerft = new JMenuItem("Perft");
        JMenuItem toolsMenuLoadFen = new JMenuItem("Load FEN string");
        toolsMenu.add(toolsMenuAnalysis);
        toolsMenu.add(toolsMenuPerft);
        toolsMenu.add(toolsMenuLoadFen);
        toolsMenuAnalysis.addActionListener(e -> System.out.println("Tools -> Analysis"));
        toolsMenuPerft.addActionListener(e -> {
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    Perft.all();
                    return null;
                }
            };
            worker.execute();
        });
        toolsMenuLoadFen.addActionListener(e -> System.out.println("Tools -> Load FEN string"));

        JMenu actionsMenu = new JMenu("Actions");
        JMenuItem actionsMenuFEN = new JMenuItem("Output FEN String");
        actionsMenu.add(actionsMenuFEN);
        actionsMenuFEN.addActionListener(e -> {
            //System.out.println("Before SwingWorker");
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        //System.out.println("doInBackground");
                        Gui.outputFEN();
                        //System.out.println("Outputting FEN"); 
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };
            worker.execute();
        });

        this.add(fileMenu);
        this.add(editMenu);
        this.add(toolsMenu);
        this.add(actionsMenu);
    }

}
