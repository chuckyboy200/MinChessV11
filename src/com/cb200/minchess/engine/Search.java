package com.cb200.minchess.engine;

import com.cb200.minchess.board.Board;

public class Search {
    
    private Search() {}

    public static int searchBegin(long[] board, int maxDepth) {

        return rootSearch(board, maxDepth);
    }

    public static int rootSearch(long[] board, int depth) {
        int[] moves = Board.gen(board, false, false);
        int bestMove = moves[0];
        int bestEval = Integer.MIN_VALUE;
        for(int move = 0; move < moves[99]; move ++) {
            long[] boardAfterMove = Board.makeMove(board, moves[move]);
            if(Board.isPlayerInCheck(boardAfterMove, Board.player(board))) {
                continue;
            }
            int eval = -negaMax(boardAfterMove, depth, Integer.MIN_VALUE, Integer.MAX_VALUE);
            if(eval > bestEval) {
                bestEval = eval;
                bestMove = moves[move];
            }
        }
        return bestMove;
    }

    public static int negaMax(long[] board, int depth, int alpha, int beta) {
        if(depth == 0) {
            return Eval.eval(board);
        }
        int[] moves = Board.gen(board, false, false);
        for(int move = 0; move < moves[99]; move ++) {
            long[] boardAfterMove = Board.makeMove(board, moves[move]);
            if(Board.isPlayerInCheck(boardAfterMove, Board.player(board))) {
                continue;
            }
            int eval = -negaMax(boardAfterMove, depth - 1, -beta, -alpha);
            if(eval > alpha) {
                if(eval >= beta) {
                    return beta;
                }
                alpha = eval;
            }
        }
        return alpha;
    }

}
