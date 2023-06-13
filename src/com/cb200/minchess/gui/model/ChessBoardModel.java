package com.cb200.minchess.gui.model;

import java.util.Arrays;

import com.cb200.chessutils.value.Value;
import com.cb200.minchess.board.Board;

public class ChessBoardModel {
    
    private long[] board;
    private int[] moves;
    private int lastStart;
    private int lastTarget;

    public ChessBoardModel() {
        this(Board.startingPosition());
    }

    public ChessBoardModel(long[] _board) {
        this(_board, Board.gen(_board, true, false));
    }

    public ChessBoardModel(long[] _board, int[] _moves) {
        this.board = Arrays.copyOf(_board, _board.length);
        this.moves = Arrays.copyOf(_moves, _moves.length);
        this.lastStart = -1;
        this.lastTarget = -1;
    }

    public long[] getBoard() {
        return this.board;
    }

    public int[] getMoves() {
        return this.moves;
    }

    public int getLastStart() {
        return this.lastStart;
    }

    public int getLastTarget() {
        return this.lastTarget;
    }

    public void updateBoard(long[] _board, int[] _moves, int _lastStart, int _lastTarget) {
        this.board = Arrays.copyOf(_board, _board.length);
        this.moves = Arrays.copyOf(_moves, _moves.length);
        this.lastStart = _lastStart;
        this.lastTarget = _lastTarget;
    }

    public boolean makeMove(int startSquare, int targetSquare) {
        int moveIndex = Board.isValidMove(this.moves, startSquare, targetSquare);
        if(moveIndex != Value.INVALID) {
            this.board = Board.makeMove(this.board, this.moves[moveIndex]);
            this.moves = Board.gen(this.board, true, false);
            this.lastStart = startSquare;
            this.lastTarget = targetSquare;
            return true;
        }
        return false;
    }

}
