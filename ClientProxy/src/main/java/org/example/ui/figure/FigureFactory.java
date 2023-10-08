package org.example.ui.figure;


import org.example.ui.ChessField;
import org.example.ui.ChessGameUI;

public class FigureFactory {

    public static Figure createFigure(Color color, String type, String pos, int firstTurn) {
        if (pos.length() != 2) {
            throw new IllegalArgumentException("Invalid position: \"" + pos + "\"");
        }
        int x = pos.toLowerCase().charAt(0) - 97;
        int y = 7 - (pos.charAt(1) - 49);
        if (x < 0 || x > 7 || y < 0 || y > 7) {
            throw new IllegalArgumentException("Position not on board: \"" + pos + "\"");
        }
        Figure f = switch (type.toLowerCase()) {
            case "rook" -> new Rook(color, ChessGameUI.getBoard().getField(x, y));
            case "knight" -> new Knight(color, ChessGameUI.getBoard().getField(x, y));
            case "bishop" -> new Bishop(color, ChessGameUI.getBoard().getField(x, y));
            case "queen" -> new Queen(color, ChessGameUI.getBoard().getField(x, y));
            case "king" -> new King(color, ChessGameUI.getBoard().getField(x, y));
            case "pawn" -> new Pawn(color, ChessGameUI.getBoard().getField(x, y));
            default -> throw new IllegalArgumentException("Unknown Figure: \"" + type + "\"");
        };
        f.setFirstTurn(firstTurn);
        return f;
    }

    public static void createFigure(Color color, String type, ChessField field) {
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

    public static Figure createFigure(Color color, int type, int x, int y, int firstTurn) {
        if (x < 0 || x > 7 || y < 0 || y > 7) {
            throw new IllegalArgumentException("Position not on board: \"" + x + "/" + y + "\"");
        }
        Figure f;
        switch (type) {
            case 0b0:
                f = new Pawn(color, ChessGameUI.getBoard().getField(x, y));
                break;
            case 0b100:
                f = new Rook(color, ChessGameUI.getBoard().getField(x, y));
                break;
            case 0b101:
                f = new Knight(color, ChessGameUI.getBoard().getField(x, y));
                break;
            case 0b110:
                f = new Bishop(color, ChessGameUI.getBoard().getField(x, y));
                break;
            case 0b1110:
                f = new Queen(color, ChessGameUI.getBoard().getField(x, y));
                break;
            case 0b1111:
                f = new King(color, ChessGameUI.getBoard().getField(x, y));
                break;
            default:
                throw new IllegalArgumentException("Unknown Figure: \"" + type + "\"");
        }
        f.setFirstTurn(firstTurn);
        return f;
    }

    public static int getFigureID(Figure f) {
        if (f instanceof Pawn)
            return 0b0;
        if (f instanceof Rook)
            return 0b100;
        if (f instanceof Knight)
            return 0b101;
        if (f instanceof Bishop)
            return 0b110;
        if (f instanceof Queen)
            return 0b1110;
        if (f instanceof King)
            return 0b1111;
        return 0;
    }
}

