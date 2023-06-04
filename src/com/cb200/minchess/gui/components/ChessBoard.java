package com.cb200.minchess.gui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JLayeredPane;

import com.cb200.chessutils.value.Value;
import com.cb200.minchess.board.Board;
import com.cb200.minchess.gui.views.Gui;

public class ChessBoard extends JPanel {

    public static final Color LIGHTCOLOR = new Color(0xeeeed2);
	public static final Color LIGHTCOLORPREV = new Color(0xf6f669);
	public static final Color LIGHTCOLORBEST = new Color(0x75c7e8);
	public static final Color DARKCOLOR = new Color(0x769656);
	public static final Color DARKCOLORPREV = new Color(0xbaca2b);
	public static final Color DARKCOLORBEST = new Color(0x268ccc);
    public static final int SQUARE_SIZE = 100;

    private static final Color[][] COLOR;
    static {
        COLOR = new Color[3][2];
        COLOR[0][0] = LIGHTCOLOR;
        COLOR[0][1] = DARKCOLOR;
        COLOR[1][0] = LIGHTCOLORPREV;
        COLOR[1][1] = DARKCOLORPREV;
        COLOR[2][0] = LIGHTCOLORBEST;
        COLOR[2][1] = DARKCOLORBEST;
    }
    private long[] board = new long[Board.MAX_BITBOARDS];
    private int[] moves = new int[100];
    private JLayeredPane layeredPane;
    private JButton square[] = new JButton[64];
    private JLabel piece[] = new JLabel[64];
    private JLabel dragged = null;
    private int startX, startY;
    private int lastStart, lastTarget;

    public ChessBoard() {
        this(Board.startingPosition());
    }

    public ChessBoard(long[] _board) {
        this.board = _board;
        this.moves = Board.gen(this.board, true, false);
        setPreferredSize(new Dimension(SQUARE_SIZE * 8, SQUARE_SIZE * 8));
        setLayout(new BorderLayout());
        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(SQUARE_SIZE * 8, SQUARE_SIZE * 8));
        layeredPane.setLayout(null);
        initializeBoard();
        add(layeredPane, BorderLayout.CENTER);
        lastStart = -1;
        lastTarget = -1;
    }

    private void initializeBoard() {
        for(int rank = 7; rank >= 0; rank --) {
            for(int file = 0; file < 8; file ++) {
                int s = rank << 3 | file;
                setSquare(rank, file, Board.getSquare(this.board, s), 0);
                layeredPane.add(this.square[s], JLayeredPane.DEFAULT_LAYER);
                layeredPane.add(this.piece[s], JLayeredPane.DRAG_LAYER);
            }
        }
    }

    private void setBoard() {
        for(int rank = 7; rank >= 0; rank --) {
            for(int file = 0; file < 8; file ++) {
                int s = rank << 3 | file;
                layeredPane.remove(this.piece[s]);
                layeredPane.remove(this.square[s]);
                setSquare(rank, file, Board.getSquare(this.board, s), s == lastStart || s == lastTarget ? 1 : 0);
                layeredPane.add(this.square[s], JLayeredPane.DEFAULT_LAYER);
                layeredPane.add(this.piece[s], JLayeredPane.DRAG_LAYER);
            }
        }
    }

    private final void setSquare(int rank, int file, int pieceInt, int colorIndex) {
        int s = rank << 3 | file;
        this.square[s] = new JButton();
        this.square[s].setBounds(file * SQUARE_SIZE, (7 - rank) * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
        this.square[s].setBackground(COLOR[colorIndex][Integer.bitCount(s & 0x9) & 1]);
        this.piece[s] = new JLabel();
        if(pieceInt != Value.NONE) {
            this.piece[s] = new JLabel(ChessPiece.pieceIcon[pieceInt], JLabel.CENTER);
        }
        this.piece[s].setBounds(file * SQUARE_SIZE, (7 - rank) * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
        setMouseListener(this.piece[s]);
    }

    private final void setMouseListener(JLabel pieceIcon) {
        MouseAdapter ma = new MouseAdapter() {
            Point origin;

            @Override
            public void mousePressed(MouseEvent e) {
                dragged = (JLabel) e.getSource();
                origin = new Point(e.getXOnScreen() - dragged.getLocationOnScreen().x, 
                                e.getYOnScreen() - dragged.getLocationOnScreen().y);
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
                    int convertStartX = (startX / SQUARE_SIZE);
                    int convertStartY = 7 - (startY / SQUARE_SIZE);
                    int convertEndX = (endX / SQUARE_SIZE);
                    int convertEndY = 7 - (endY / SQUARE_SIZE);
                    int startSquare = convertStartY << 3 | convertStartX;
                    int targetSquare = convertEndY << 3 | convertEndX;
                    int movePacked = startSquare | (targetSquare << 6);
                    int moveIndex = -1;
                    for(int move = 0; move < moves[99]; move ++) {
                        if((moves[move] & 0xfff) == movePacked) {
                            moveIndex = move;
                        }
                    }
                    if (moveIndex != -1) {
                        lastStart = moves[moveIndex] & 0x3f;
                        lastTarget = (moves[moveIndex] >>> 6) & 0x3f;
                        String moveNotation = Board.moveNotationString(board, moves[moveIndex]);
                        int moveNotationPadding = 8 - moveNotation.length();
                        String moveString = "";
                        if(((moves[moveIndex] >>> 16) & Value.BLACK_BIT) == 0) {
                            moveString = ((board[Board.STATUS] >>> 17) & 0xff) + ". " + moveNotation + "        ".substring(0, moveNotationPadding);
                        } else {
                            moveString = " " + moveNotation + "\n";
                        }
                        Gui.print(moveString, true);
                        board = Board.makeMove(board, moves[moveIndex]);
                        moves = Board.gen(board, true, false);
                        setBoard();
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
        pieceIcon.addMouseListener(ma);
        pieceIcon.addMouseMotionListener(ma);
    }
}
