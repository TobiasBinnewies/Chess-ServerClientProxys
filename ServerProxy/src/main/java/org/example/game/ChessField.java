package org.example.game;

import org.example.game.figure.Figure;

public class ChessField {

    Figure figure;
    private final int x, y;
    private final ChessBoard board;

    ChessField(ChessBoard board, int x, int y) {
        this.board = board;
        this.x = x;
        this.y = y;
    }

    public ChessBoard getBoard() {
        return board;
    }

    public void setFigure(Figure figure) {
        this.figure = figure;
    }

    public Figure getFigure() {
        return figure;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return "<" + x + "," + y + ">";
    }
}
