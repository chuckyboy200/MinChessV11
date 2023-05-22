package com.cb200.minchess.gui;

import java.util.Arrays;
import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

import com.cb200.chessutils.piece.Piece;
import com.cb200.minchess.board.Board;

public class BoardPanel extends JPanel {
    
    public final static Color LIGHTCOLOR = new Color(0xeeeed2);
	public final static Color LIGHTCOLORPREV = new Color(0xf6f669);
	public final static Color LIGHTCOLORBEST = new Color(0x75c7e8);
	public final static Color DARKCOLOR = new Color(0x769656);
	public final static Color DARKCOLORPREV = new Color(0xbaca2b);
	public final static Color DARKCOLORBEST = new Color(0x268ccc);

    private long[] board;
    private int squareSize;
    private Color[][] squareColor;

    public BoardPanel() {
        super();
        this.board = null;
        this.squareSize = 64;
        this.squareColor = new Color[3][2];
        this.squareColor[0][0] = LIGHTCOLOR;
        this.squareColor[0][1] = DARKCOLOR;
        this.squareColor[1][0] = LIGHTCOLORPREV;
        this.squareColor[1][1] = DARKCOLORPREV;
        this.squareColor[2][0] = LIGHTCOLORBEST;
        this.squareColor[2][1] = DARKCOLORBEST;
    }

    public void updateBoard(long[] _board) {
        this.board = Arrays.copyOf(_board, _board.length);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(this.board == null) {
            return;
        }
        for(int square = 0; square < 64; square ++) {
            int rank = square >>> 3;
            int file = square & 7;
            int x = file * squareSize;
            int y = (7 - rank) * squareSize;
            Color squareColor = getSquareColor(rank, file);
            g.setColor(squareColor);
            g.fillRect(x, y, squareSize, squareSize);
            int piece = Board.getSquare(this.board, square);
            if(piece != Piece.EMPTY) {
                System.out.println("Drawing piece " + piece + " at " + square + " (" + rank + ", " + file + ")");
               PieceGraphics.drawPiece(g, piece, x, y, squareSize);  
            }
        }
    }

    private Color getSquareColor(int rank, int file) {
        return squareColor[0][1 - ((rank + file) % 2)];
    }

    private Color getSquareColor(int rank, int file, int type) {
        if((rank + file) % 2 == 0) {
            return squareColor[type][0];
        } else {
            return squareColor[type][1];
        }
    }

}
