package com.cb200.minchess.gui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
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
import com.cb200.minchess.gui.controller.ChessBoardController;
import com.cb200.minchess.gui.model.ChessBoardModel;
import com.cb200.minchess.gui.views.ChessBoardView;
import com.cb200.minchess.gui.views.Gui;

public class ChessBoard {

    private ChessBoardModel model;
    private ChessBoardView view;

    public ChessBoard() {
        this(Board.startingPosition());
    }

    public ChessBoard(long[] _board) {
        this.model = new ChessBoardModel(_board);
        this.view = new ChessBoardView(this.model);
    }

    public ChessBoardView getView() {
        return this.view;
    }

    public long[] getBoard() {
        return this.model.getBoard();
    }

}
