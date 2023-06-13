package com.cb200.minchess.gui.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import com.cb200.minchess.board.Board;
import com.cb200.minchess.gui.components.ChessPiece;
import com.cb200.minchess.gui.controller.ChessBoardController;
import com.cb200.minchess.gui.model.ChessBoardModel;

public class ChessBoardView extends JPanel {
    
    private static final Color LIGHTCOLOR = new Color(0xeeeed2);
	private static final Color LIGHTCOLORPREV = new Color(0xf6f669);
	private static final Color LIGHTCOLORBEST = new Color(0x75c7e8);
	private static final Color DARKCOLOR = new Color(0x769656);
	private static final Color DARKCOLORPREV = new Color(0xbaca2b);
	private static final Color DARKCOLORBEST = new Color(0x268ccc);
    private static final int NORMAL_SQUARE_COLOR = 0;
    private static final int PREVIOUS_MOVE_COLOR = 1;
    private static final int BEST_MOVE_COLOR = 2;
    private static final int LIGHT = 0;
    private static final int DARK = 1;
    private static final Color[][] COLOR;
    static {
        COLOR = new Color[3][2];
        COLOR[NORMAL_SQUARE_COLOR][LIGHT] = LIGHTCOLOR;
        COLOR[NORMAL_SQUARE_COLOR][DARK] = DARKCOLOR;
        COLOR[PREVIOUS_MOVE_COLOR][LIGHT] = LIGHTCOLORPREV;
        COLOR[PREVIOUS_MOVE_COLOR][DARK] = DARKCOLORPREV;
        COLOR[BEST_MOVE_COLOR][LIGHT] = LIGHTCOLORBEST;
        COLOR[BEST_MOVE_COLOR][DARK] = DARKCOLORBEST;
    }
    
    public static final int SQUARE_SIZE = 100;
    private ChessBoardModel model;
    private ChessBoardController controller;
    private JLayeredPane layeredPane;
    private JButton square[];
    private JLabel piece[];
    private JLabel dragged;
    
    public ChessBoardView() {
        this(Board.startingPosition());
    }

    public ChessBoardView(long[] _board) {
        this(_board, Board.gen(_board, true, false));
    }

    public ChessBoardView(long[] _board, int[] _moves) {
        this(new ChessBoardModel(_board, _moves));
    }

    public ChessBoardView(ChessBoardModel _model) {
        this.model = _model;
        this.square = new JButton[64];
        this.piece = new JLabel[64];
        setPreferredSize(new Dimension(SQUARE_SIZE * 8, SQUARE_SIZE * 8));
        setLayout(new BorderLayout());
        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(SQUARE_SIZE * 8, SQUARE_SIZE * 8));
        layeredPane.setLayout(null);
        initializeBoard();
        add(layeredPane, BorderLayout.CENTER);
    }

    private void initializeBoard() {
        long[] board = this.model.getBoard();
        for(int rank = 7; rank >= 0; rank --) {
            for(int file = 0; file < 8; file ++) {
                int s = rank << 3 | file;
                setSquare(rank, file, Board.getSquare(board, s), 0);
                layeredPane.add(this.square[s], JLayeredPane.DEFAULT_LAYER);
                layeredPane.add(this.piece[s], JLayeredPane.DRAG_LAYER);
            }
        }
    }

    public void updateBoard(long[] board) {
        for(int rank = 7; rank >= 0; rank --) {
            for(int file = 0; file < 8; file ++) {
                int s = rank << 3 | file;
                layeredPane.remove(this.square[s]);
                layeredPane.remove(this.piece[s]);
                setSquare(rank, file, Board.getSquare(board, s), 0);
                layeredPane.add(this.square[s], JLayeredPane.DEFAULT_LAYER);
                layeredPane.add(this.piece[s], JLayeredPane.DRAG_LAYER);
            }
        }
    }

    private void setSquare(int rank, int file, int pieceInt, int colorIndex) {
        int s = rank << 3 | file;
        this.square[s] = new JButton();
        this.square[s].setBounds(file * SQUARE_SIZE, (7 - rank) * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
        this.square[s].setBackground(COLOR[colorIndex][1 - (Integer.bitCount(s & 0x9) & 1)]);
        this.square[s].setOpaque(true);
        this.square[s].setBorderPainted(false);
        this.piece[s] = new JLabel();
        this.piece[s].setBounds(file * SQUARE_SIZE, (7 - rank) * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
        this.piece[s].setOpaque(false);
        this.piece[s].setIcon(ChessPiece.pieceIcon[pieceInt]);
        this.piece[s].setHorizontalAlignment(JLabel.CENTER);
        this.piece[s].setVerticalAlignment(JLabel.CENTER);
        MouseAdapter ma = ChessBoardController.createMouseAdapter(this.piece[s], model, this);
        this.piece[s].addMouseListener(ma);
        this.piece[s].addMouseMotionListener(ma);
    }

    public JLayeredPane getLayeredPane() {
        return this.layeredPane;
    }

    public JLabel getDragged() {
        return this.dragged;
    }

}
