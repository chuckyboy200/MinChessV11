package com.cb200.minchess.engine;

import com.cb200.chessutils.piece.Piece;
import com.cb200.minchess.board.Board;

public class Eval {
    
    private Eval() {}

    public static int eval(long[] board) {
        int playerBit = (int) (board[Board.STATUS] & Board.PLAYER_BIT) << 3;
        int otherBit = playerBit ^ 8;
        int material = Long.bitCount(board[Piece.QUEEN | playerBit]) * 9
                + Long.bitCount(board[Piece.ROOK | playerBit]) * 5
                + Long.bitCount(board[Piece.BISHOP | playerBit]) * 3
                + Long.bitCount(board[Piece.KNIGHT | playerBit]) * 3
                - Long.bitCount(board[Piece.QUEEN | otherBit]) * 9
                - Long.bitCount(board[Piece.ROOK | otherBit]) * 5
                - Long.bitCount(board[Piece.BISHOP | otherBit]) * 3
                - Long.bitCount(board[Piece.KNIGHT | otherBit]) * 3;
        int pawnMaterial = Long.bitCount(board[Piece.PAWN | playerBit]) * 1
                - Long.bitCount(board[Piece.PAWN | otherBit]) * 1;
        return material + pawnMaterial;
    }

}
