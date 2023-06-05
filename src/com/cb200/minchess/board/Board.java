package com.cb200.minchess.board;

import java.util.Arrays;

import com.cb200.chessutils.bitboard.B;
import com.cb200.chessutils.fen.Fen;
import com.cb200.chessutils.magic.Magic;
import com.cb200.chessutils.piece.Piece;
import com.cb200.chessutils.value.Value;
import com.cb200.chessutils.zobrist.Zobrist;

/**
 * This class is a static utility class which contains methods which create and manipulate board arrays
 * Board arrays consist of a set of bitboards (longs) which represent the board's various pieces and their locations, a set of status bits which represent the board's various statuses and a zobrist key
 * The bitboards are as follows:
 * [0] and [8] = white and black occupancy respectively
 * [1] and [9] = white and black king
 * [2] and [10] = white and black queen
 * [3] and [11] = white and black rook
 * [4] and [12] = white and black bishop
 * [5] and [13] = white and black knight
 * [6] and [14] = white and black pawn
 * [7] = status bits
 * [15] = 64-bit zobrist key
 * @author Charles Clark
 */
public class Board {
    
    /*
     * This is the index in the board array for the board's various status bits as follows:
     * 1) The player to move (bit 0) 0 = white, 1 = black
     * 2) Castling rights (bits 1-4) bit 1 = white king side, bit 2 = white queen side, bit 3 = black king side, bit 4 = black queen side
     * 3) En passant square (bits 5-10) no en passant square = Value.INVALID, any other valid enpassant square value directly corresponds to the square on the board, e.g. a3 = 16
     * 5) Half move count (bits 11-16) the number of half moves since the last capture or pawn move, used for the fifty move rule
     * 6) Full move count (bits 17-24) the number of full moves, incremented after black's move
     */
    public static final int STATUS = 7;
    /*
     * This is the maximum number of bitboards in the board array
     */
    public static final int MAX_BITBOARDS = 16;
    /*
     * This is the index in the board array for the board's Zobrist key
     */
    public static final int KEY = MAX_BITBOARDS - 1;
    /*
     * This is used when retrieving the player bit from STATUS
     */
    public static final int PLAYER_BIT = 1;
    /**
     * This is a string used in the parseMove method 
     */
    private static final String PIECE_STRING = " KQRBNPXXkqrbnp";

    /*
     * This is a utility class and should not be instantiated
     */
    private Board() {}

    /**
     * This method returns the player to move, 0 = white, 1 = black
     * @param board the board array
     * @return the player to move
     */
    public static final int player(long[] board) {
        return (int) board[STATUS] & PLAYER_BIT;
    }

    /**
     * This method returns whether kingside castling is possible for a player
     * @param board the board array
     * @param player the player to move
     * @return true if kingside castling is possible
     */
    public static final boolean kingSide(long[] board, int player) {
        return ((board[STATUS] >>> 1) & Value.KINGSIDE_BIT[player]) != 0L;
    }

    /**
     * This method returns whether queenside castling is possible for a player
     * @param board the board array
     * @param player the player to move
     * @return true if queenside castling is possible
     */
    public static final boolean queenSide(long[] board, int player) {
        return ((board[STATUS] >>> 1) & Value.QUEENSIDE_BIT[player]) != 0L;
    }

    /**
     * This method returns whether the current En passant square is a valid value
     * @param board the board array
     * @return true if the current En passant square is a valid value
     */
    public static final boolean isValidEnPassantSquare(long[] board) {
        return ((1L << ((int) (board[STATUS] >>> 5) & 0x3f)) & (B.BB[B.ENPASSANT_SQUARES_PLAYER0][0] | B.BB[B.ENPASSANT_SQUARES_PLAYER1][0])) != 0L;
    }

    /**
     * This method returns a valid En passant square or Value.INVALID if not
     * @param board the board array
     * @return a valid En passant square or Value.INVALID if not
     */
    public static final int enPassantSquare(long[] board) {
        return isValidEnPassantSquare(board) ? (int) (board[STATUS] >>> 5) & 0x3f : Value.INVALID;
    }

    /**
     * This method returns the half move clock
     * @param board the board array
     * @return the half move clock
     */
    public static final int halfMoveCount(long[] board) {
        return (int) (board[STATUS] >>> 11) & 0x3f;
    }

    /**
     * This method returns the full move counter
     * @param board the board array
     * @return the full move counter
     */
    public static final int fullMoveCount(long[] board) {
        return (int) (board[STATUS] >>> 17) & 0xff;
    }

    /**
     * This method returns the boards Zobrist key
     * @param board the board array
     * @return the Zobrist key
     */
    public static final long key(long[] board) {
        return board[KEY];
    }

    /**
     * This is a factory method which creates a new board array from a FEN string representing the starting position
     * @return a new board array representing the starting position
     */
    public static final long[] startingPosition() {
        return fromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }

    /**
     * This is a factory method which creates a new board array from a FEN string
     * @param fen the FEN string 
     * @return a new board array representing the position in the FEN string
     */
    public static final long[] fromFen(String fen) {
        /*
         * create a new empty board array
         */
        long[] board = new long[MAX_BITBOARDS];
        /*
         * get an array of ints representing 64 squares and their contents from the FEN string
         */
        int[] pieces = Fen.getPieces(fen);
        /*
         * loop over the squares on the board
         */
        for(int square = 0; square < 64; square ++) {
            /*
             * if the square is not empty, set the corresponding bit in the appropriate piece bitboard and occupancy bitboard for that player
             */
            if(pieces[square] != Value.NONE) {
                board[pieces[square]] |= 1L << square;
                board[pieces[square] & Value.BLACK_BIT] |= 1L << square;
            }
        }
        /*
         * get whether it is white to move from the FEN string and set the player bit in STATUS
         */
        boolean whiteToMove = Fen.getWhiteToMove(fen);
        board[STATUS] = whiteToMove ? Value.WHITE : Value.BLACK;
        /*
         * get the castling rights from the FEN string and set the castling bits in STATUS
         */
        int castling = Fen.getCastling(fen);
        board[STATUS] ^= castling << 1;
        /*
         * get the en passant square from the FEN string and store the en passant square in STATUS. if there is no en passant square, set the en passant square to Value.NONE
         */
        int eSquare = Fen.getEnPassantSquare(fen);
        boolean eSquareIsValid = ((eSquare > 15 && eSquare < 24) || (eSquare > 39 && eSquare < 40));
        board[STATUS] ^= eSquareIsValid ? eSquare << 5 : 0L;
        eSquare = eSquareIsValid ? eSquare : Value.INVALID;
        /*
         * get the half move count and full move count from the FEN string and set their bits in STATUS
         */
        board[STATUS] ^= Fen.getHalfMoveCount(fen) << 11;
        board[STATUS] ^= Fen.getFullMoveCount(fen) << 17;
        /*
         * get the Zobrist key and set it in KEY
         */
        board[KEY] = Zobrist.getKey(pieces, whiteToMove, (castling & Value.KINGSIDE_BIT[Value.WHITE]) != 0, (castling & Value.QUEENSIDE_BIT[Value.WHITE]) != 0, (castling & Value.KINGSIDE_BIT[Value.BLACK]) != 0, (castling & Value.QUEENSIDE_BIT[Value.BLACK]) != 0, eSquare);
        return board;
    }

    /**
     * This is a factory method which generates all moves for a given board
     * @param board the board array
     * @param legal whether to generate only legal moves
     * @param tactical whether to generate only tactical moves
     * @return an array of moves, the array's max size is set at 100, the last element of the array is the length of the move list
     */
    public static final int[] gen(long[] board, boolean legal, boolean tactical) {
        /*
         * get the player to move from STATUS, the playerBit (for index into the appropriate bitboard, and the otherBit (for index into the other player's bitboard)
         */
        int player = (int) board[STATUS] & PLAYER_BIT;
        int playerBit = player << 3;
        int otherBit = 8 ^ playerBit;
        /*
         * get the occupancy bitboard for all squares on the board and the tactical occupancy, depending on whether only tactical moves are required
         */
        long allOccupancy = board[playerBit] | board[otherBit];
        long tacticalOccupancy = tactical ? board[otherBit] : ~board[playerBit];
        /*
         * create an array of ints to store the moves, the array's max size is set at 100, the last element of the array is the length of the move list
         */
        int[] moves = new int[100];
        /*
         * generate king moves, knight moves, pawn moves, and slider moves, and store them in the moves array, moveListLength is updated to be the current number of moves in the moves array
         */
        int moveListLength = getKingMoves(board, moves, Piece.KING | playerBit, player, allOccupancy, tacticalOccupancy, tactical);
        moveListLength = getKnightMoves(board, moves, Piece.KNIGHT | playerBit, moveListLength, tacticalOccupancy);
        moveListLength = getPawnMoves(board, moves, Piece.PAWN | playerBit, moveListLength, player, allOccupancy, board[otherBit], tactical);
        moveListLength = getSliderMoves(board, moves, player, moveListLength, allOccupancy, tacticalOccupancy);
        /*
         * throw an error if there are more moves than can fit in the moves array
         */
        if(moveListLength > 98) {
            throw new RuntimeException("Move list overflow");
        }
        /*
         * set the last element in the moves array to the length of the move list
         */
        moves[99] = moveListLength;
        return legal ? purgeIllegalMoves(board, moves) : moves;
    }

    private static final int[] purgeIllegalMoves(long[] board, int[] moves) {
        int[] legalMoves = new int[100];
        int legalMoveCount = 0;
        for(int move = 0; move < moves[99]; move ++) {
            long[] boardAfterMove = Board.makeMove(board, moves[move]);
            if(!Board.isPlayerInCheck(boardAfterMove, (int) board[STATUS] & 0x1)) {
                legalMoves[legalMoveCount ++] = moves[move];
            }
        }
        legalMoves[99] = legalMoveCount;
        return legalMoves;
    }

    /**
     * This is a factory method which returns a new board array representing the position after making a move
     * @param board the board array
     * @param move the move to make
     * @return a new board array representing the position after making the move
     */
    public static final long[] makeMove(long[] board, int move) {
        /*
         * create a new board array and create copies of the board's bitboards, castling rights, en passant square, half move count, full move count, and Zobrist key as these may be modified by the move
         */
        long[] newBoard = Arrays.copyOf(board, board.length);
        int castling = (int) (newBoard[STATUS] >>> 1) & 0xf;
        int eSquare = enPassantSquare(board);
        int halfMoveCount = (int) newBoard[STATUS] >>> 11 & 0x3f;
        int fullMoveCount = (int) newBoard[STATUS] >>> 17 & 0x3f;
        long key = newBoard[KEY];
        /*
         * get piece information from the move
         */
        int startSquare = move & 0x3f;
        int startPiece = (move >>> 16) & 0xf;
        int startPieceType = startPiece & Piece.TYPE;
        int targetSquare = (move >>> 6) & 0x3f;
        int targetPiece = (move >>> 20) & 0xf;
        /*
         * get the player to move from STATUS
         */
        int player = (int) newBoard[STATUS] & PLAYER_BIT;
        /*
         * reset the en passant square if it is set
         */
        if(eSquare != Value.INVALID) {
            key ^= Zobrist.ENPASSANT_FILE[eSquare & Value.FILE];
            eSquare = Value.INVALID;
        }
        /*
         * perform the move based on the piece type
         */
        switch(startPieceType) {
            /*
             * Queens, bishops and knights have the same move logic, so they are grouped together
             */
            case Piece.QUEEN:
            case Piece.BISHOP:
            case Piece.KNIGHT: {
                /*
                 * translate the target square into it's bitboard version and get a bitboard containing the bits of the start square and target square
                 */
                long targetSquareBit = 1L << targetSquare;
                long pieceMoveBits = (1L << startSquare) | targetSquareBit;
                /*
                 * update the piece bitboard and player occupancy bitboard for the start square and target square by flipping their bits, then update the zobrist key
                 */
                newBoard[startPiece] ^= pieceMoveBits;
                newBoard[player << 3] ^= pieceMoveBits;
                key ^= Zobrist.PIECE[startPieceType][player][startSquare] ^ Zobrist.PIECE[startPieceType][player][targetSquare];
                /*
                 * if the target square is not empty, this is a capture so reset the half move counter and update the target piece bitboard and other player occupancy bitboard for the target square, and update the zobrist key
                 */
                if(targetPiece != Value.NONE) {
                    halfMoveCount = 0;
                    int other = 1 ^ player;
                    newBoard[targetPiece] ^= targetSquareBit;
                    newBoard[other << 3] ^= targetSquareBit;
                    key ^= Zobrist.PIECE[targetPiece & Piece.TYPE][other][targetSquare];
                }
                break;
            }
            /*
             * Handle king moves
             */
            case Piece.KING: {
                /*
                 * get the player bit used in calculations later
                 */
                int playerBit = player << 3;
                /*
                 * handle moving the piece as above
                 */
                long targetSquareBit = 1L << targetSquare;
                long pieceMoveBits = (1L << startSquare) | targetSquareBit;
                newBoard[startPiece] ^= pieceMoveBits;
                newBoard[player << 3] ^= pieceMoveBits;
                key ^= Zobrist.PIECE[startPieceType][player][startSquare] ^ Zobrist.PIECE[startPieceType][player][targetSquare];
                /*
                 * check if castling is possible and if it is, turn off the castling bits for that player and update the zobrist key
                 */
                boolean playerKingSideCastling = (castling & Value.KINGSIDE_BIT[player]) != 0;
                boolean playerQueenSideCastling = (castling & Value.QUEENSIDE_BIT[player]) != 0;
                if(playerKingSideCastling || playerQueenSideCastling) {
                    key ^= (playerKingSideCastling ? Zobrist.KING_SIDE[player] : 0) ^ (playerQueenSideCastling ? Zobrist.QUEEN_SIDE[player] : 0);
                    castling &= (player == Value.WHITE ? ~3 : ~12);
                }
                /*
                 * if the king moves 2 squares horizontally, this is a castling move, so update the rook bitboard and player occupancy bitboard for the rook's start and target squares, then update the zobrist key
                 */
                if(Math.abs(startSquare - targetSquare) == 2) {
                    int rookRank = player == Value.WHITE ? 0 : 56;
                    if((targetSquare & Value.FILE) == Value.FILE_G) {
                        long rookMoveBits = (1L << (rookRank | Value.FILE_H)) | (1L << (rookRank | Value.FILE_F));
                        newBoard[Piece.ROOK | playerBit] ^= rookMoveBits;
                        newBoard[playerBit] ^= rookMoveBits;
                        key ^= Zobrist.PIECE[Piece.ROOK][player][rookRank | 7] ^ Zobrist.PIECE[Piece.ROOK][player][rookRank | 5];
                    } else {
                        long rookMoveBits = (1L << rookRank) | (1L << (rookRank | Value.FILE_D));
                        newBoard[Piece.ROOK | playerBit] ^= rookMoveBits;
                        newBoard[playerBit] ^= rookMoveBits;
                        key ^= Zobrist.PIECE[Piece.ROOK][player][rookRank] ^ Zobrist.PIECE[Piece.ROOK][player][rookRank | 3];
                    }
                }
                /*
                 * handle a capture as above
                 */
                if(targetPiece != Value.NONE) {
                    halfMoveCount = 0;
                    int other = 1 ^ player;
                    newBoard[targetPiece] ^= targetSquareBit;
                    newBoard[other << 3] ^= targetSquareBit;
                    key ^= Zobrist.PIECE[targetPiece & Piece.TYPE][other][targetSquare];
                }
                break;
            }
            /*
             * Handle rook moves
             */
            case Piece.ROOK: {
                /*
                 * handle moving the piece as above
                 */
                long targetSquareBit = 1L << targetSquare;
                long pieceMoveBits = (1L << startSquare) | targetSquareBit;
                newBoard[startPiece] ^= pieceMoveBits;
                newBoard[player << 3] ^= pieceMoveBits;
                key ^= Zobrist.PIECE[startPieceType][player][startSquare] ^ Zobrist.PIECE[startPieceType][player][targetSquare];
                /*
                 * if castling on the rook's side is available, reset it, then update the castling bits and the zobrist key
                 */
                if((castling & Value.KINGSIDE_BIT[player]) != Value.NONE) {
                    if(startSquare == (player == Value.WHITE ? 7 : 63)) {
                        castling ^= Value.KINGSIDE_BIT[player];
                        key ^= Zobrist.KING_SIDE[player];
                    }
                } 
                if((castling & Value.QUEENSIDE_BIT[player]) != Value.NONE) {
                    if(startSquare == (player == Value.WHITE ? 0 : 56)) {
                        castling ^= Value.QUEENSIDE_BIT[player];
                        key ^= Zobrist.QUEEN_SIDE[player];
                    }
                }
                /*
                 * handle captures as above
                 */
                if(targetPiece != Value.NONE) {
                    halfMoveCount = 0;
                    int other = 1 ^ player;
                    newBoard[targetPiece] ^= targetSquareBit;
                    newBoard[other << 3] ^= targetSquareBit;
                    key ^= Zobrist.PIECE[targetPiece & Piece.TYPE][other][targetSquare];
                }
                break;
            }
            /*
             * Handle pawn moves
             */
            case Piece.PAWN: {
                long targetSquareBit = 1L << targetSquare;
                /*
                 * get the promotion piece from the move, if there is no promotion piece, this is equal to 0
                 */
                int promotePiece = (move >>> 12) & 0xf;
                /*
                 * get the player bit for calculations later
                 */
                int playerBit = player << 3;
                /*
                 * pawn moves reset the half move counter
                 */
                halfMoveCount = 0;
                /*
                 * if there is no promotion piece, perform a normal pawn move
                 */
                if(promotePiece == Value.NONE) {
                    long pieceMoveBits = (1L << startSquare) | targetSquareBit;
                    newBoard[startPiece] ^= pieceMoveBits;
                    newBoard[playerBit] ^= pieceMoveBits;
                    key ^= Zobrist.PIECE[startPieceType][player][startSquare] ^ Zobrist.PIECE[startPieceType][player][targetSquare];
                } else {
                    /*
                     * A promotion piece exists, so update the start piece bitboard for the start square, the promotion piece bitboard for the target square, and the player occupancy bitboard, then update the zobrist key
                     */
                    long startSquareBit = 1L << startSquare;
                    newBoard[startPiece] ^= startSquareBit;
                    newBoard[promotePiece] ^= targetSquareBit;
                    newBoard[playerBit] ^= startSquareBit | targetSquareBit;
                    key ^= Zobrist.PIECE[startPieceType][player][startSquare] ^ Zobrist.PIECE[promotePiece & Piece.TYPE][player][targetSquare];
                }
                /*
                 * perform a capture as above
                 */
                if(targetPiece != Value.NONE) {
                    int other = 1 ^ player;
                    newBoard[targetPiece] ^= targetSquareBit;
                    newBoard[other << 3] ^= targetSquareBit;
                    key ^= Zobrist.PIECE[targetPiece & Piece.TYPE][other][targetSquare];
                }
                /*
                 * perform an en passant capture. the difference between this capture and a normal capture is that the captured pawn is on a different square to the target square
                 */
                if(targetSquare == enPassantSquare(board)) {
                    int other = 1 ^ player;
                    int otherBit = other << 3;
                    int captureSquare = targetSquare + (player == Value.WHITE ? -8 : 8);
                    long captureSquareBit = 1L << captureSquare;
                    newBoard[Piece.PAWN | otherBit] ^= captureSquareBit;
                    newBoard[otherBit] ^= captureSquareBit;
                    key ^= Zobrist.PIECE[Piece.PAWN][other][captureSquare];
                }
                /*
                 * if the pawn advances 2 squares, set a new en passant square and update the zobrist key
                 */
                if(Math.abs(startSquare - targetSquare) == 16) {
                    eSquare = startSquare + (player == Value.WHITE ? 8 : -8);
                    key ^= Zobrist.ENPASSANT_FILE[eSquare & Value.FILE];
                }
                break;
            }
            default: break;
        }
        /*
         * if this move captures a rook, reset the appropriate castling rights and update the zobrist key
         */
        if((targetPiece & Piece.TYPE) == Piece.ROOK) {
            int other = 1 ^ player;
            if((castling & Value.KINGSIDE_BIT[other]) != Value.NONE) {
                if(targetSquare == (other == Value.WHITE ? 7 : 63)) {
                    castling ^= Value.KINGSIDE_BIT[other];
                    key ^= Zobrist.KING_SIDE[other];
                }
            }
            if((castling & Value.QUEENSIDE_BIT[other]) != Value.NONE) {
                if(targetSquare == (other == Value.WHITE ? 0 : 56)) {
                    castling ^= Value.QUEENSIDE_BIT[other];
                    key ^= Zobrist.QUEEN_SIDE[other];
                }
            }
        }
        /*
         * create the new boards STATUS bits and set its KEY, then return the new board array
         */
        newBoard[STATUS] = (1 ^ player) | (castling << 1) | (eSquare != Value.INVALID ? (eSquare << 5) : 0) | (halfMoveCount << 11) | ((fullMoveCount + player) << 17);
        newBoard[KEY] = key;
        return newBoard;
    }

    /**
     * This method makes a null move on the board, and returns the new board array. This shouldn't be used twice in a row and not while the current player is in check
     * @param board the board array
     * @param move the move to make 
     * @return the new board array
     */
    public static final long[] nullMove(long[] board, int move) {
        /*
         * a null move is where a player makes a second move in a row without the opponent making a move
        * to set this up, we are passed a board where normally a player would make the next move but we flip the player bit and reset the en passant square since an en passant move is not possible in a null move
        */
        board[STATUS] = (board[STATUS] ^ 1) & ~0x7e0;
        return board;
    }

    /**
     * This method returns the contents of a square on the board
     * @param board the board array
     * @param square the square to get the contents of
     * @return the contents of the square, an empty square returns Value.NONE
     */
    public static final int getSquare(long[] board, int square) {
        /*
         * get the bitboard representation of the square
         */
        long squareBit = 1L << square;
        /*
         * check if the white occupancy bitboard has the square set, if it does, check which piece is on the square and return it
         */
        if((board[Value.WHITE_BIT] & squareBit) != 0L) {
            if((board[Piece.WHITE_PAWN] & squareBit) != 0L) return Piece.WHITE_PAWN;
            if((board[Piece.WHITE_KNIGHT] & squareBit) != 0L) return Piece.WHITE_KNIGHT;
            if((board[Piece.WHITE_BISHOP] & squareBit) != 0L) return Piece.WHITE_BISHOP;
            if((board[Piece.WHITE_ROOK] & squareBit) != 0L) return Piece.WHITE_ROOK;
            if((board[Piece.WHITE_QUEEN] & squareBit) != 0L) return Piece.WHITE_QUEEN;
            return Piece.WHITE_KING;
        }
        /*
         * check if the black occupancy bitboard has the square set, if it does, check which piece is on the square and return it
         */
        if((board[Value.BLACK_BIT] & squareBit) == 0L) return Value.NONE;
        if((board[Piece.BLACK_PAWN] & squareBit) != 0L) return Piece.BLACK_PAWN;
        if((board[Piece.BLACK_KNIGHT] & squareBit) != 0L) return Piece.BLACK_KNIGHT;
        if((board[Piece.BLACK_BISHOP] & squareBit) != 0L) return Piece.BLACK_BISHOP;
        if((board[Piece.BLACK_ROOK] & squareBit) != 0L) return Piece.BLACK_ROOK;
        if((board[Piece.BLACK_QUEEN] & squareBit) != 0L) return Piece.BLACK_QUEEN;
        return Piece.BLACK_KING;
    }

    /**
     * check if a square is attacked by a certain player
     * @param board the board array
     * @param square the square to check
     * @param player the player to check is attacking the square
     * @return true if the square is attacked by the player, false otherwise
     */
    public static final boolean isSquareAttackedByPlayer(long[] board, int square, int player) {
        int playerBit = player << 3;
        /*
         * check whether a player's knight is attacking this square, if so return true
         */
        if((B.BB[B.LEAP_ATTACKS][square] & board[Piece.KNIGHT | playerBit]) != 0L) return true;
        /*
         * check whether a player's king is attacking this square, if so return true
         */
        if((B.BB[B.KING_ATTACKS][square] & board[Piece.KING | playerBit]) != 0L) return true;
        /*
         * check whether a player's pawn is attacking this square, if so return true
         */
        if((B.BB[B.PAWN_ATTACKS_PLAYER1 - player][square] & board[Piece.PAWN | playerBit]) != 0L) return true;
        /*
         * get the full occupancy of the board for Magic bitboard calculations
         */
        long allOccupancy = board[Value.WHITE_BIT] | board[Value.BLACK_BIT];
        /*
         * check whether a player's bishop or queen is attacking this square on a diagonal, if so return true
         */
        if((Magic.bishopMoves(square, allOccupancy) & (board[Piece.BISHOP | playerBit] | board[Piece.QUEEN | playerBit])) != 0L) return true;
        /*
         * check whether a player's rook or queen is attacking this square on a file or rank, if so return true
         */
        if((Magic.rookMoves(square, allOccupancy) & (board[Piece.ROOK | playerBit] | board[Piece.QUEEN | playerBit])) != 0L) return true;
        /*
         * no piece is attacking this square, return false
         */
        return false;
    }

    /**
     * check if a player is in check
     * @param board the board array
     * @param player the player to check
     * @return true if the player is in check, false otherwise
     */
    public static final boolean isPlayerInCheck(long[] board, int player) {
        return isSquareAttackedByPlayer(board, Long.numberOfTrailingZeros(board[Piece.KING | (player << 3)]), 1 ^ player);
    }

    /**
     * During move generation, add a move to the moves array
     * @param board the board array
     * @param moves the moves array
     * @param startSquare the start square of the move
     * @param targetSquare the target square of the move
     * @param moveListLength the current number of moves in the moves array
     * @param piece the piece being moved
     */
    private static final void addMove(long[] board, int[] moves, int startSquare, int targetSquare, int moveListLength, int piece) {
        moves[moveListLength] = startSquare | (targetSquare << 6) | (piece << 16) | (getSquare(board, targetSquare) << 20);
    }

    /**
     * During move generation, add pawn promotion moves to the moves array
     * @param board the board array
     * @param moves the moves array
     * @param startSquare the start square of the move
     * @param targetSquare the target square of the move
     * @param playerBit the player bit
     * @param moveListLength the current number of moves in the moves array
     * @param piece the piece being moved
     */
    private static final void addPromotionMoves(long[] board, int[] moves, int startSquare, int targetSquare, int playerBit, int moveListLength, int piece) {
        int moveInfo = startSquare | (targetSquare << 6) | (piece << 16) | (getSquare(board, targetSquare) << 20);
        moves[moveListLength ++] = moveInfo | ((Piece.ROOK | playerBit) << 12);
        moves[moveListLength ++] = moveInfo | ((Piece.BISHOP | playerBit) << 12);
        moves[moveListLength ++] = moveInfo | ((Piece.KNIGHT | playerBit) << 12);
        moves[moveListLength ++] = moveInfo | ((Piece.QUEEN | playerBit) << 12);
    }

    /**
     * generate king moves
     * @param board the board array
     * @param moves the moves array
     * @param piece the king piece
     * @param player the player to move
     * @param allOccupancy the occupancy of all squares on the board
     * @param tacticalOccupancy the occupancy of all squares on the board for tactical moves
     * @param tactical whether to generate only tactical moves
     * @return the number of moves in the moves array after king moves have been generated
     */
    private static final int getKingMoves(long[] board, int[] moves, int piece, int player, long allOccupancy, long tacticalOccupancy, boolean tactical) {
        /*
         * since king generation is first, there are no moves in the moves array and so moveListLength is 0
         */
        int moveListLength = 0;
        /*
         * get the king square from the king bitboard
         */
        int square = Long.numberOfTrailingZeros(board[piece]);
        /*
         * get all squares that the king attacks. if tactical is true, only get the squares that are occupied by the other player's pieces, otherwise get all squares not occupied by the player's pieces
         */
        long attacks = B.BB[B.KING_ATTACKS][square] & tacticalOccupancy;
        /*
         * loop over each bit in the attacks bitboard
         */
        for(; attacks != 0L; attacks &= attacks - 1L) {
            /*
             * add a move to the moves array for the king moving from the king square to the square of the bit in the attacks bitboard and increment moveListLength
             */
            addMove(board, moves, square, Long.numberOfTrailingZeros(attacks), moveListLength ++, piece);
        }
        /*
         * if tactical is true, don't handle castling and return early
         */
        if(tactical) return moveListLength;
        /*
         * get the castling rights from STATUS and check whether castling is possible on kingside and queenside
         */
        int castling = (int) (board[STATUS] >>> 1) & 0xf;
        boolean kingSide = (castling & Value.KINGSIDE_BIT[player]) != Value.NONE;
        boolean queenSide = (castling & Value.QUEENSIDE_BIT[player]) != Value.NONE;
        /*
         * if either side has castling rights, check whether the squares between the king and rook are empty and whether the squares that the king moves through are attacked by the other player. if so, add a castling move to the moves array and increment moveListLength
         */
        if((kingSide || queenSide)) {
            int other = 1 ^ player;
            if(!isSquareAttackedByPlayer(board, square, other)) {
                if(kingSide) {
                    if((allOccupancy & B.BB[B.CASTLE_PLAYER0 + player][0]) == 0L && !isSquareAttackedByPlayer(board, square + 1, other)) {
                        addMove(board, moves, square, square + 2, moveListLength ++, piece);
                    }
                }
                if(queenSide) {
                    if((allOccupancy & B.BB[B.CASTLE_PLAYER0 + player][1]) == 0L && !isSquareAttackedByPlayer(board, square - 1, other)) {
                        addMove(board, moves, square, square - 2, moveListLength ++, piece);
                    }
                }
            }
        }
        return moveListLength;
    }

    /**
     * generate knight moves
     * @param board the board array
     * @param moves the moves array
     * @param piece the knight piece
     * @param moveListLength the current number of moves in the moves array
     * @param tacticalOccupancy the occupancy of all squares on the board for tactical moves
     * @return the number of moves in the moves array after knight moves have been generated
     */
    private static final int getKnightMoves(long[] board, int[] moves, int piece, int moveListLength, long tacticalOccupancy) {
        /*
         * get the knight bitboard and loop over each set bit in the bitboard, where each set bit represents a square with a knight on it
         */
        long knightBitboard = board[piece];
        for(; knightBitboard != 0L; knightBitboard &= knightBitboard - 1) {
            /*
             * get the square of the knight
             */
            int square = Long.numberOfTrailingZeros(knightBitboard);
            /*
             * get all squares that the knight attacks and loop over each set bit in the bitboard, where each set bit represents a square that the knight attacks
             */
            long attacks = B.BB[B.LEAP_ATTACKS][square] & tacticalOccupancy;
            for(; attacks != 0L; attacks &= attacks - 1) {
                /*
                 * add a move to the moves array for the knight moving from the knight square to the square of the bit in the attacks bitboard and increment moveListLength 
                 */
                addMove(board, moves, square, Long.numberOfTrailingZeros(attacks), moveListLength ++, piece);
            }
        }
        return moveListLength;
    }

    /**
     * generate pawn moves
     * @param board the board array
     * @param moves the moves array
     * @param piece the pawn piece
     * @param moveListLength the current number of moves in the moves array
     * @param player the player to move
     * @param allOccupancy the occupancy of all squares on the board
     * @param otherOccupancy the occupancy of all squares on the board for the other player
     * @param tactical whether to generate only tactical moves
     * @return 
     */
    private static final int getPawnMoves(long[] board, int[] moves, int piece, int moveListLength, int player, long allOccupancy, long otherOccupancy, boolean tactical) {
        /*
         * get the pawn bitboard and loop over each set bit in the bitboard, where each set bit represents a square with a pawn on it
         */
        long pawnBitboard = board[piece];
        /*
         * get the player bit for calculations later
         */
        int playerBit = player << 3;
        for(; pawnBitboard != 0L; pawnBitboard &= pawnBitboard - 1) {
            /*
             * get the square of the pawn
             */
            int square = Long.numberOfTrailingZeros(pawnBitboard);
            /*
             * initialize and set the attacks bitboard to 0
             */
            long attacks = 0L;
            /*
             * if tactical is false, get possible single pawn pushes. get double pawn pushes if relevant
             */
            if(!tactical) {
                /*
                 * get single pawn pushes
                 */
                attacks = B.BB[B.PAWN_ADVANCE_1_PLAYER0 + player][square] & ~allOccupancy;
                if(attacks != 0L) {
                    /*
                     * if there is a single pawn push, get double pawn pushes if relevant
                     */
                    attacks |= B.BB[B.PAWN_ADVANCE_2_PLAYER0 + player][square] & ~allOccupancy;
                }
            }
            /*
             * get the en passant square
             */
            int eSquare = enPassantSquare(board);
            /*
             * add the en passant square to the other occupancy bitboard if it is set, as this is a square that the pawn can attack
             */
            otherOccupancy |= (eSquare != Value.INVALID ? (1L << eSquare) : 0L);
            attacks |= B.BB[B.PAWN_ATTACKS_PLAYER0 + player][square] & otherOccupancy;
            /*
             * loop over each set bit in the attacks bitboard, where each set bit represents a square that the pawn attacks
             */
            for(; attacks != 0L; attacks &= attacks - 1) {
                /*
                 * get the target square of the attack and the rank of the target square
                 */
                int targetSquare = Long.numberOfTrailingZeros(attacks);
                int targetRank = targetSquare >>> 3;
                /*
                 * if the target rank is a pawn promotion rank, add a pawn promotion move to the moves array, otherwise add a normal pawn move to the moves array
                 */
                if(targetRank == 0 || targetRank == 7) {
                    addPromotionMoves(board, moves, square, targetSquare, playerBit, moveListLength, Piece.PAWN | playerBit);
                    moveListLength += 4;
                } else {
                    addMove(board, moves, square, targetSquare, moveListLength ++, Piece.PAWN | playerBit);
                }
            }
         }
        return moveListLength;
    }

    /**
     * generate slider moves for queens, rooks and bishops
     * @param board the board array
     * @param moves the moves array
     * @param player the player to move
     * @param moveListLength the current number of moves in the moves array
     * @param allOccupancy the occupancy of all squares on the board
     * @param tacticalOccupancy the occupancy of all squares on the board for tactical moves
     * @return the number of moves in the moves array after slider moves have been generated
     */
    private static final int getSliderMoves(long[] board, int[] moves, int player, int moveListLength, long allOccupancy, long tacticalOccupancy) {
        /*
         * get the player bit for calculations later
         */
        int playerBit = player << 3;
        /*
         * get the bitboard of all sliders for the player 
         */
        long sliderBitboard = board[Piece.QUEEN | playerBit] | board[Piece.ROOK | playerBit] | board[Piece.BISHOP | playerBit];
        /*
         * loop over each set bit in the bitboard, where each set bit represents a square with a slider on it 
         */
        for(; sliderBitboard != 0L; sliderBitboard &= sliderBitboard - 1) {
            int square = Long.numberOfTrailingZeros(sliderBitboard);
            int piece = getSquare(board, square);
            int pieceType = piece & Piece.TYPE;
            long attacks = pieceType != Piece.ROOK ? Magic.bishopMoves(square, allOccupancy) : Magic.rookMoves(square, allOccupancy);
            attacks |= pieceType == Piece.QUEEN ? Magic.rookMoves(square, allOccupancy) : 0L;
            attacks &= tacticalOccupancy;
            for(; attacks != 0L; attacks &= attacks - 1) {
                addMove(board, moves, square, Long.numberOfTrailingZeros(attacks), moveListLength ++, piece);
            }
        }
        return moveListLength;
    }

    public static final void drawText(long[] board) {
        System.out.println(boardString(board));
    }

    public static final String boardString(long[] board) {
        String boardString = "";
        for(int rank = 7; rank >= 0; rank --) {
			for(int file = 0; file < 8; file ++) {
				int square = rank << 3 | file;
				int piece = getSquare(board, square);
				if(piece != Value.NONE) {
                    boardString += Piece.SHORT_STRING[piece] + " ";
				} else {
                    boardString += ". ";
				}
			}
			boardString += "\n";
		}
        return boardString;
    }

    public static final String squareToString(int square) {
        return Value.FILE_STRING.charAt(square & Value.FILE) + Integer.toString((square >>> 3) + 1);
    }

    public static final String moveString(int move) {
        int promotePiece = (move >>> 12) & 0xf;
        return squareToString(move & 0x3f) + squareToString((move >>> 6) & 0x3f) + (promotePiece == Value.NONE ? "" : Piece.SHORT_STRING[promotePiece].toUpperCase());
    }

    public static final String moveNotationString(long[] board, int move) {
        int startSquare = move & 0x3f;
        int startFile = startSquare & Value.FILE;
        int startRank = startSquare >>> 3;
        int targetSquare = (move >>> 6) & 0x3f;
        int targetFile = targetSquare & Value.FILE;
        int targetRank = targetSquare >>> 3;
        int startPiece = (move >>> 16) & 0xf;
        long pieceBitboard = board[startPiece];
        int startType = startPiece & Piece.TYPE;
		int player = startPiece >>> 3;
        int targetPiece = (move >>> 20) & 0xf;
        int promotePiece = (move >>> 12) & 0xf;
        long allOccupancy = board[Value.WHITE_BIT] | board[Value.BLACK_BIT];
        String notation = "";
		switch(startType) {
			case Piece.KING: {
				if(Math.abs(startSquare - targetSquare) == 2) {
					return "O-O" + (targetFile == Value.FILE_G ? "" : "-O");
				}
				notation = "K";
				break;
			}
            case Piece.QUEEN: {
                notation = "Q";
                long queensAttackTargetSquare = Magic.queenMoves(targetSquare, allOccupancy) & pieceBitboard;
                if(queensAttackTargetSquare > 1L) {
                    int queensOnFile = Long.bitCount(queensAttackTargetSquare & B.BB[B.FILE][targetFile]);
                    int queensOnRank = Long.bitCount(queensAttackTargetSquare & B.BB[B.RANK][targetRank]);
                    int queensOnDiagonals = Long.bitCount(queensAttackTargetSquare & (B.BB[B.DIAGONAL_ATTACKS][targetSquare]));
                    if(queensOnRank > 1) {
                        notation += Value.FILE_STRING.charAt(startFile);
                    }
                    if(queensOnFile > 1) {
                        notation += Integer.toString(startRank + 1);
                    }
                    if(notation.length() == 1 && queensOnDiagonals > 1) {
                        notation += Value.FILE_STRING.charAt(startFile);
                    }
                }
                break;
            }
			case Piece.ROOK: {
				notation = "R";
				long rooksAttackTargetSquare = Magic.rookMoves(targetSquare, allOccupancy) & pieceBitboard;
                if(rooksAttackTargetSquare > 1L) {
                    int rooksOnFile = Long.bitCount(rooksAttackTargetSquare & B.BB[B.FILE][targetFile]);
                    int rooksOnRank = Long.bitCount(rooksAttackTargetSquare & B.BB[B.RANK][targetRank]);
                    if(rooksOnRank > 1) {
                        notation += Value.FILE_STRING.charAt(startFile);
                    }
                    if(rooksOnFile > 1) {
                        notation += Integer.toString(startRank + 1);
                    }
                }
				break;
			}
			case Piece.BISHOP: {
				notation = "B";
				long bishopsAttackTargetSquare = Magic.bishopMoves(targetSquare, allOccupancy) & pieceBitboard;
                if(bishopsAttackTargetSquare > 1L) {
                    int bishopsOnFile = Long.bitCount(bishopsAttackTargetSquare & B.BB[B.FILE][targetFile]);
                    int bishopsOnRank = Long.bitCount(bishopsAttackTargetSquare & B.BB[B.RANK][targetRank]);
                    int bishopsOnDiagonals = Long.bitCount(bishopsAttackTargetSquare & (B.BB[B.DIAGONAL_ATTACKS][targetSquare]));
                    if(bishopsOnRank > 1) {
                        notation += Value.FILE_STRING.charAt(startFile);
                    }
                    if(bishopsOnFile > 1) {
                        notation += Integer.toString(startRank + 1);
                    }
                    if(notation.length() == 1 && bishopsOnDiagonals > 1) {
                        notation += Value.FILE_STRING.charAt(startFile);
                    }
                }
				break;
			}
			case Piece.KNIGHT: {
				notation = "N";
				if(Long.bitCount(B.BB[B.LEAP_ATTACKS][targetSquare] & pieceBitboard) > 1) {
                    if(Long.bitCount(B.BB[B.RANK][startRank] & pieceBitboard) > 1){
						notation += Value.FILE_STRING.charAt(startFile);
					}
					if(Long.bitCount(B.BB[B.FILE][startFile] & pieceBitboard) > 1) {
						notation += Integer.toString(startRank + 1);
					} 
				}
				break;
			}
			case Piece.PAWN:
            default: {
				notation = "";
                break;
			}
		}
		if(targetPiece != Value.NONE || targetSquare == enPassantSquare(board)) {
			if(startType == Piece.PAWN) {
				notation += Value.FILE_STRING.charAt(startFile);
			}
			notation += "x";
		}
		notation += squareToString(targetSquare);
		if(promotePiece != Value.NONE) {
			notation += "=";
			switch(promotePiece & Piece.TYPE) {
				case Piece.QUEEN: notation += "Q"; break;
				case Piece.ROOK: notation += "R"; break;
				case Piece.BISHOP: notation += "B"; break;
				case Piece.KNIGHT: notation += "N"; break;
			}
		}
        long[] tempBoard = Board.makeMove(board, move);
		if(Board.isPlayerInCheck(tempBoard, 1 ^ player)) {
			int[] moves = Board.gen(tempBoard, true, false);
			if(moves[99] == 0) {
				notation += "#";
			} else {
				notation += "+";
			}
		}
		return notation;
    }

    public static final String moveStringVerbose(int move) {
        int startSquare = move & 0x3f;
        int targetSquare = (move >>> 6) & 0x3f;
        int promotePiece = (move >>> 12) & 0xf;
        int startPiece = (move >>> 16) & 0xf;
        int targetPiece = (move >>> 20) & 0xf;
        return Piece.SHORT_STRING[startPiece] + "[" + startSquare + "] " + Piece.SHORT_STRING[targetPiece] + "[" + targetSquare + "] " + Piece.SHORT_STRING[promotePiece] + "[" + promotePiece + "]";
    }

    public static final int parseMove(long[] board, String moveString) {
        int startSquare = Value.FILE_STRING.indexOf(moveString.charAt(0)) + ((Character.getNumericValue(moveString.charAt(1)) - 1) << 3);
        int targetSquare = Value.FILE_STRING.indexOf(moveString.charAt(2)) + ((Character.getNumericValue(moveString.charAt(3)) - 1) << 3);
        int promotePiece = 0;
        if(moveString.length() > 4) {
            promotePiece = PIECE_STRING.indexOf(moveString.charAt(4));
        }
        return startSquare | (targetSquare << 6) | (promotePiece << 12) | (getSquare(board, startSquare) << 16) | (getSquare(board, targetSquare) << 20); 
    }

    public static final String toFenString(long[] board) {
        //System.out.println("toFenString");
        StringBuilder fen = new StringBuilder();
        for(int rank = 7; rank >= 0; rank --) {
            int empty = 0;
            for(int file = 0; file < 8; file ++) {
                int square = rank << 3 | file;
                int piece = Board.getSquare(board, square);
                if(piece != Value.NONE) {
                    if(empty > 0) {
                        fen.append(empty);
                        empty = 0;
                    }
                    fen.append(Piece.SHORT_STRING[piece]);
                } else {
                    empty ++;
                }
            }
            if(empty > 0) {
                fen.append(empty);
            }
            if(rank > 0) {
                fen.append('/');
            }
        }
        fen.append(" " + (Board.player(board) == Value.WHITE ? "w " : "b "));
        int castling = (int) (board[STATUS] >>> 1) & 0xf;
        if(castling == 0) {
            fen.append("- ");
        } else {
            fen.append((Board.kingSide(board, Value.WHITE) ? "K" : "") + (Board.queenSide(board, Value.WHITE) ? "Q" : "") + (Board.kingSide(board, Value.BLACK) ? "k" : "") + (Board.queenSide(board, Value.BLACK) ? "q" : "") + " ");
        }
        if(isValidEnPassantSquare(board)) {
            fen.append(squareToString(enPassantSquare(board)) + " ");
        } else {
            fen.append("- ");
        }
        fen.append(Integer.toString(halfMoveCount(board)) + " " + Integer.toString(fullMoveCount(board)));
        return fen.toString();
    }

}
