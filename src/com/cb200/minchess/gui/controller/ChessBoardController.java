package com.cb200.minchess.gui.controller;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JLayeredPane;

import com.cb200.minchess.gui.model.ChessBoardModel;
import com.cb200.minchess.gui.views.ChessBoardView;

public class ChessBoardController {
    
    public ChessBoardController() {

    }

    public static MouseAdapter createMouseAdapter(JLabel pieceIcon, ChessBoardModel model, ChessBoardView view) {
        return new MouseAdapter() {
            Point origin;
            JLayeredPane layeredPane = view.getLayeredPane();
            JLabel dragged = view.getDragged();
            int startX;
            int startY;

            @Override
            public void mousePressed(MouseEvent e) {
                dragged = (JLabel) e.getSource();
                origin = new Point(e.getXOnScreen() - dragged.getLocationOnScreen().x, e.getYOnScreen() - dragged.getLocationOnScreen().y);
                startX = dragged.getX();
                startY = dragged.getY();
                layeredPane.moveToFront(dragged);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragged != null) {
                    int xOffset = e.getX() - origin.x + dragged.getLocation().x;
                    int yOffset = e.getY() - origin.y + dragged.getLocation().y;
                    Point position = new Point(xOffset, yOffset);
                    dragged.setLocation(position);
                    dragged.repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (dragged != null) {
                    int endX = e.getXOnScreen() - layeredPane.getLocationOnScreen().x;
                    int endY = e.getYOnScreen() - layeredPane.getLocationOnScreen().y;
                    int convertStartX = (startX / ChessBoardView.SQUARE_SIZE);
                    int convertStartY = 7 - (startY / ChessBoardView.SQUARE_SIZE);
                    int convertEndX = (endX / ChessBoardView.SQUARE_SIZE);
                    int convertEndY = 7 - (endY / ChessBoardView.SQUARE_SIZE);
                    int startSquare = convertStartY << 3 | convertStartX;
                    int targetSquare = convertEndY << 3 | convertEndX;
                    if (model.makeMove(startSquare, targetSquare)) {
                        view.updateBoard(model.getBoard());
                        layeredPane.repaint();
                        layeredPane.revalidate();
                    } else {
                        dragged.setLocation(startX, startY);
                        dragged.repaint();
                    }
                    dragged = null;
                }
            }

        };
    }

}
