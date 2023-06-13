package com.cb200.minchess.gui.components;

import java.awt.Image;
import java.net.URL;

import javax.swing.ImageIcon;

import com.cb200.chessutils.piece.Piece;
import com.cb200.minchess.gui.views.ChessBoardView;

public class ChessPiece {
    
    public static final ImageIcon[] pieceIcon;

    private static final int PIECE_SIZE = (int) (ChessBoardView.SQUARE_SIZE / 1.5);

    static {
        pieceIcon = new ImageIcon[15];
        String filename = "";
        for(int piece = 1; piece < 15; piece ++) {
            String pieceString = Piece.LONG_STRING[piece];
            if(pieceString.length() > 5) {
                filename = "/pieceImages/" + pieceString + ".png";
                URL url = ChessPiece.class.getResource(filename);
                if (url == null) {
                    System.out.println("Resource not found: " + filename);
                } else {
                    ImageIcon icon = new ImageIcon(url);
                    Image image = icon.getImage();
                    Image newimg = image.getScaledInstance(PIECE_SIZE, PIECE_SIZE,  java.awt.Image.SCALE_SMOOTH);
                    pieceIcon[piece] = new ImageIcon(newimg);
                }
            }
        }
    }

    private ChessPiece() {}

    public static final void init() {}


}
