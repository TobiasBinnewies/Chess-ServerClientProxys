package org.example.game.figure;


import org.example.game.ChessBoard;
import org.example.game.ChessField;
import org.example.game.Color;

public class FigureFactory {

    private final ChessBoard board;

    public FigureFactory(ChessBoard board) {
        this.board = board;
    }

    public Figure createFigure(Color color, String type, String pos, int firstTurn) {
        if (pos.length() != 2) {
            throw new IllegalArgumentException("Invalid position: \"" + pos + "\"");
        }
        int x = pos.toLowerCase().charAt(0) - 97;
        int y = 7 - (pos.charAt(1) - 49);
        if (x < 0 || x > 7 || y < 0 || y > 7) {
            throw new IllegalArgumentException("Position not on board: \"" + pos + "\"");
        }
        Figure f = switch (type.toLowerCase()) {
            case "rook" -> new Rook(color, board.getField(x, y));
            case "knight" -> new Knight(color, board.getField(x, y));
            case "bishop" -> new Bishop(color, board.getField(x, y));
            case "queen" -> new Queen(color, board.getField(x, y));
            case "king" -> new King(color, board.getField(x, y));
            case "pawn" -> new Pawn(color, board.getField(x, y));
            default -> throw new IllegalArgumentException("Unknown Figure: \"" + type + "\"");
        };
        f.setFirstTurn(firstTurn);
        return f;
    }

    public void createFigure(Color color, String type, ChessField field) {
        switch (type.toLowerCase()) {
            case "rook" -> new Rook(color, field);
            case "knight" -> new Knight(color, field);
            case "bishop" -> new Bishop(color, field);
            case "queen" -> new Queen(color, field);
            case "king" -> new King(color, field);
            case "pawn" -> new Pawn(color, field);
            default -> throw new IllegalArgumentException("Unknown Figure: \"" + type + "\"");
        }
    }

}

