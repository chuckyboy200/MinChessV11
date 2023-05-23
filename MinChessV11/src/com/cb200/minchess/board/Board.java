package com.cb200.minchess.board;

import java.util.Arrays;

import com.cb200.chessutils.bitboard.B;
import com.cb200.chessutils.fen.Fen;
import com.cb200.chessutils.magic.Magic;
import com.cb200.chessutils.piece.Piece;
import com.cb200.chessutils.value.Value;
import com.cb200.chessutils.zobrist.Zobrist;

public class Board {
    
    /**
     * This is the index in the board array for the board's various statuses as follows:
     * 1) The player to move (1 bit) 0 = white, 1 = black
     * 2) Castling rights (4 bits) bit 1 = white king side, bit 2 = white queen side, bit 3 = black king side, bit 4 = black queen side
     * 3) En passant square (6 bits) no en passant square = 0, any other valid enpassant square value directly corresponds to the square on the board, e.g. a3 = 16
     * 4) Half move count (6 bits) the number of half moves since the last capture or pawn move, used for the fifty move rule
     * 5) Full move count (8 bits) the number of full moves, incremented after black's move
     * 
     */
    public final static int STATUS = 15;
    /**
     * This is the index in the board array for the board's Zobrist key
     */
    public final static int KEY = 16;
    /**
     * This is used when retrieving the player bit from STATUS
     */
    public final static int PLAYER_BIT = 1;
    /**
     * This is the maximum number of bitboards in the board array
     */
    private final static int MAX_BITBOARDS = 17;

    /**
     * This is a utility class and should not be instantiated
     */
    private Board() {}

    /**
     * This method returns the player to move, 0 = white, 1 = black
     * @param board the board array
     * @return the player to move
     */
    public final static int player(long[] board) {
        return (int) board[STATUS] & PLAYER_BIT;
    }

    /**
     * This is a factory method which creates a new board array from a FEN string representing the starting position
     * @return a new board array representing the starting position
     */
    public final static long[] startingPosition() {
        return fromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }

    /**
     * This is a factory method which creates a new board array from a FEN string
     * @param fen the FEN string 
     * @return a new board array representing the position in the FEN string
     */
    public final static long[] fromFen(String fen) {
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
         * get whether it is white to move from the FEN string
         */
        boolean whiteToMove = Fen.getWhiteToMove(fen);
        /*
         * set the player bit in STATUS
         */
        board[STATUS] = whiteToMove ? Value.WHITE : Value.BLACK;
        /*
         * get the castling rights from the FEN string
         */
        int castling = Fen.getCastling(fen);
        /*
         * set the castling bits in STATUS
         */
        board[STATUS] ^= castling << 1;
        /*
         * get the en passant square from the FEN string
         */
        int eSquare = Fen.getEnPassantSquare(fen);
        if(!((eSquare > 15 && eSquare < 24) || (eSquare > 39 && eSquare < 40))) {
            eSquare = Value.NONE;
        }
        /*
         * set the en passant square bits in STATUS
         */
        board[STATUS] ^= eSquare << 5;
        /*
         * get the half move count and full move count from the FEN string and set their bits in STATUS
         */
        board[STATUS] ^= Fen.getHalfMoveCount(fen) << 11;
        board[STATUS] ^= Fen.getFullMoveCount(fen) << 17;
        /*
         * get the Zobrist key and set it in KEY in the board array
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
    public final static int[] gen(long[] board, boolean legal, boolean tactical) {
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
        return moves;
    }

    /**
     * This is a factory method which returns a new board array representing the position after making a move
     * @param board the board array
     * @param move the move to make
     * @return a new board array representing the position after making the move
     */
    public final static long[] makeMove(long[] board, int move) {
        /*
         * create a new board array and create copies of the board's bitboards, castling rights, en passant square, half move count, full move count, and Zobrist key as these may be modified by the move
         */
        long[] newBoard = Arrays.copyOf(board, board.length);
        int castling = (int) (newBoard[STATUS] >>> 1) & 0xf;
        int eSquare = (int) newBoard[STATUS] >>> 5 & 0x3f;
        int halfMoveCount = (int) newBoard[STATUS] >>> 11 & 0x3f;
        int fullMoveCount = (int) newBoard[STATUS] >>> 17 & 0x3f;
        long key = newBoard[KEY];
        /*
         * get all piece information from the move
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
        if(eSquare != Value.NONE) {
            key ^= Zobrist.ENPASSANT_FILE[eSquare & Value.FILE];
            eSquare = Value.NONE;
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
                 * update the piece bitboard and player occupancy bitboard for the start square and target square
                 */
                newBoard[startPiece] ^= pieceMoveBits;
                newBoard[player << 3] ^= pieceMoveBits;
                /*
                 * update the zobrist key
                 */
                key ^= Zobrist.PIECE[startPieceType][player][startSquare] ^ Zobrist.PIECE[startPieceType][player][targetSquare];
                /*
                 * if the target square is not empty, this is a capture so reset the half move counter and update the target piece bitboard and other player occupancy bitboard for the target square, then update the zobrist key
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
             * 
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
                 * check if castling is possible and update the castling bits in STATUS and the zobrist key if it is
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
             * 
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
                 * if castling on the rook's side is available, reset it and update the castling bits in STATUS and the zobrist key
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
             * 
             */
            case Piece.PAWN: {
                /*
                 * get the promotion piece from the move, if there is no promotion piece, this is set to 0
                 */
                int promotePiece = (move >>> 12) & 0xf;
                long targetSquareBit = 1L << targetSquare;
                /*
                 * get the player bit for calculations later
                 */
                int playerBit = player << 3;
                /*
                 * pawn moves reset the half move counter
                 */
                halfMoveCount = 0;
                /*
                 * if a promotion piece exists, update the start piece bitboard and player occupancy bitboard for the start square and the promotion piece bitboard and player occupancy bitboard for the target square, then update the zobrist key
                 */
                if(promotePiece == Value.NONE) {
                    long pieceMoveBits = (1L << startSquare) | targetSquareBit;
                    newBoard[startPiece] ^= pieceMoveBits;
                    newBoard[playerBit] ^= pieceMoveBits;
                    key ^= Zobrist.PIECE[startPieceType][player][startSquare] ^ Zobrist.PIECE[startPieceType][player][targetSquare];
                } else {
                    /*
                     * perform a normal pawn move
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
                if(targetSquare == ((board[STATUS] >>> 5) & 0x3f)) {
                    int other = 1 ^ player;
                    int otherBit = other << 3;
                    int captureSquare = targetSquare + (player == Value.WHITE ? -8 : 8);
                    long captureSquareBit = 1L << captureSquare;
                    newBoard[Piece.PAWN | otherBit] ^= captureSquareBit;
                    newBoard[otherBit] ^= captureSquareBit;
                    key ^= Zobrist.PIECE[Piece.PAWN][other][captureSquare];
                }
                /*
                 * if the pawn advances 2 squares, set the en passant square in STATUS and update the zobrist key
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
        newBoard[STATUS] = (1 ^ player) | (castling << 1) | (eSquare << 5) | (halfMoveCount << 11) | ((fullMoveCount + player) << 17);
        newBoard[KEY] = key;
        return newBoard;
    }

    /**
     * This method returns the contents of a square on the board
     * @param board the board array
     * @param square the square to get the contents of
     * @return the contents of the square, an empty square returns Value.NONE
     */
    public final static int getSquare(long[] board, int square) {
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
    public final static boolean isSquareAttackedByPlayer(long[] board, int square, int player) {
        int playerBit = player << 3;
        if((B.BB[B.LEAP_ATTACKS][square] & board[Piece.KNIGHT | playerBit]) != 0L) return true;
        if((B.BB[B.KING_ATTACKS][square] & board[Piece.KING | playerBit]) != 0L) return true;
        if((B.BB[B.PAWN_ATTACKS_PLAYER1 - player][square] & board[Piece.PAWN | playerBit]) != 0L) return true;
        long allOccupancy = board[Value.WHITE_BIT] | board[Value.BLACK_BIT];
        if((Magic.bishopMoves(square, allOccupancy) & (board[Piece.BISHOP | playerBit] | board[Piece.QUEEN | playerBit])) != 0L) return true;
        if((Magic.rookMoves(square, allOccupancy) & (board[Piece.ROOK | playerBit] | board[Piece.QUEEN | playerBit])) != 0L) return true;
        return false;
    }

    public final static boolean isPlayerInCheck(long[] board, int player) {
        int playerBit = player << 3;
        return isSquareAttackedByPlayer(board, Long.numberOfTrailingZeros(board[Piece.KING | playerBit]), 1 ^ player);
    }   

    private final static void addMove(long[] board, int[] moves, int startSquare, int targetSquare, int moveListLength, int piece) {
        moves[moveListLength] = startSquare | (targetSquare << 6) | (piece << 16) | (getSquare(board, targetSquare) << 20);
    }

    private final static void addPromotionMoves(long[] board, int[] moves, int startSquare, int targetSquare, int playerBit, int moveListLength, int piece) {
        piece <<= 16;
        int targetPiece = getSquare(board, targetSquare) << 20;
        targetSquare <<= 6;
        moves[moveListLength ++] = startSquare | targetSquare | ((Piece.QUEEN | playerBit) << 12) | piece | targetPiece;
        moves[moveListLength ++] = startSquare | targetSquare | ((Piece.ROOK | playerBit) << 12) | piece | targetPiece;
        moves[moveListLength ++] = startSquare | targetSquare | ((Piece.BISHOP | playerBit) << 12) | piece | targetPiece;
        moves[moveListLength] = startSquare | targetSquare | ((Piece.KNIGHT | playerBit) << 12) | piece | targetPiece;
    }

    private final static int getKingMoves(long[] board, int[] moves, int piece, int player, long allOccupancy, long tacticalOccupancy, boolean tactical) {
        int moveListLength = 0;
        int square = Long.numberOfTrailingZeros(board[piece]);
        long attacks = B.BB[B.KING_ATTACKS][square] & tacticalOccupancy;
        for(; attacks != 0L; attacks &= attacks - 1L) {
            addMove(board, moves, square, Long.numberOfTrailingZeros(attacks), moveListLength ++, piece);
        }
        if(tactical) return moveListLength;
        int castling = (int) (board[STATUS] >>> 1) & 0xf;
        boolean kingSide = (castling & Value.KINGSIDE_BIT[player]) != Value.NONE;
        boolean queenSide = (castling & Value.QUEENSIDE_BIT[player]) != Value.NONE;
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

    private final static int getKnightMoves(long[] board, int[] moves, int piece, int moveListLength, long tacticalOccupancy) {
        long knightBitboard = board[piece];
        for(; knightBitboard != 0L; knightBitboard &= knightBitboard - 1) {
            int square = Long.numberOfTrailingZeros(knightBitboard);
            long attacks = B.BB[B.LEAP_ATTACKS][square] & tacticalOccupancy;
            for(; attacks != 0L; attacks &= attacks - 1) {
                addMove(board, moves, square, Long.numberOfTrailingZeros(attacks), moveListLength ++, piece);
            }
        }
        return moveListLength;
    }

    private final static int getPawnMoves(long[] board, int[] moves, int piece, int moveListLength, int player, long allOccupancy, long otherOccupancy, boolean tactical) {
        long pawnBitboard = board[piece];
        int playerBit = player << 3;
        for(; pawnBitboard != 0L; pawnBitboard &= pawnBitboard - 1) {
            int square = Long.numberOfTrailingZeros(pawnBitboard);
            long attacks = 0L;
            if(!tactical) {
                attacks = B.BB[B.PAWN_ADVANCE_1_PLAYER0 + player][square] & ~allOccupancy;
                if(attacks != 0L) {
                    attacks |= B.BB[B.PAWN_ADVANCE_2_PLAYER0 + player][square] & ~allOccupancy;
                }
            }
            int eSquare = (int) board[STATUS] >>> 5 & 0x3f;
            otherOccupancy |= (eSquare != Value.INVALID ? (1L << eSquare) : 0L);
            attacks |= B.BB[B.PAWN_ATTACKS_PLAYER0 + player][square] & otherOccupancy;
            for(; attacks != 0L; attacks &= attacks - 1) {
                int targetSquare = Long.numberOfTrailingZeros(attacks);
                int targetRank = targetSquare >>> 3;
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

    private final static int getSliderMoves(long[] board, int[] moves, int player, int moveListLength, long allOccupancy, long tacticalOccupancy) {
        int playerBit = player << 3;
        long sliderBitboard = board[Piece.QUEEN | playerBit] | board[Piece.ROOK | playerBit] | board[Piece.BISHOP | playerBit];
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

    public final static void drawText(long[] board) {
        System.out.println(boardString(board));
    }

    public final static String boardString(long[] board) {
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

    public final static String moveString(int move) {
        return Value.FILE_STRING.charAt(move & Value.FILE) + Integer.toString(((move & 0x3f) >>> 3) + 1) + Value.FILE_STRING.charAt((move >>> 6) & Value.FILE) + Integer.toString(((move >>> 6 & 0x3f) >>> 3) + 1);
    }

}
