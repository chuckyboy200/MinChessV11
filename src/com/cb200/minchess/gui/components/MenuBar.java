package com.cb200.minchess.gui.components;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingWorker;

import com.cb200.minchess.perft.Perft;

public class MenuBar extends JMenuBar {
    
    public MenuBar() {
        JMenu fileMenu = new JMenu("File");
        JMenuItem fileMenuPlaceholder = new JMenuItem("Quit");
        fileMenu.add(fileMenuPlaceholder);
        fileMenuPlaceholder.addActionListener(e -> System.exit(0));

        JMenu editMenu = new JMenu("Edit");
        JMenuItem editMenuPlaceholder = new JMenuItem("Placeholder");
        editMenu.add(editMenuPlaceholder);
        editMenuPlaceholder.addActionListener(e -> System.out.println("Edit -> Placeholder"));

        JMenu toolsMenu = new JMenu("Tools");
        JMenuItem toolsMenuPlaceholder = new JMenuItem("Perft");
        toolsMenu.add(toolsMenuPlaceholder);
        toolsMenuPlaceholder.addActionListener(e -> {
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    Perft.all();
                    return null;
                }
            };
            worker.execute();
        });

        this.add(fileMenu);
        this.add(editMenu);
        this.add(toolsMenu);

    }

}
