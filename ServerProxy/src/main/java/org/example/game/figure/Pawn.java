package org.example.game.figure;


import org.example.game.ChessField;
import org.example.game.Color;

import java.util.ArrayList;
import java.util.List;

public class Pawn extends Figure {

    public Pawn(Color color, ChessField field) {
        super(color, "pawn", field);
    }

    //black: from top to bottom
    @Override
    public List<ChessField> getAccessibleFields() {
        List<ChessField> fields = new ArrayList<>();
        ChessField field;
        if (color == Color.WHITE) {
            if ((field = this.field.getBoard().getField(x, y - 1)) != null && field.getFigure() == null) {
                fields.add(field);
                if (y == 6 && (field = this.field.getBoard().getField(x, y - 2)).getFigure() == null) {
                    fields.add(field);
                }
            }
            if (x + 1 < 8 && (field = this.field.getBoard().getField(x + 1, y - 1)) != null && field.getFigure() != null
                    && field.getFigure().color != color) {
                fields.add(field);
            }
            if (x - 1 >= 0 && (field = this.field.getBoard().getField(x - 1, y - 1)) != null && field.getFigure() != null
                    && field.getFigure().color != color) {
                fields.add(field);
            }
            if (y == 3) {
                if (x - 1 >= 0 && (field = this.field.getBoard().getField(x - 1, y)).getFigure() != null
                        && field.getFigure().color != color
                        && field.getFigure() instanceof Pawn
                        && field.getBoard().getCurrentTurn() - field.getFigure().getFirstTurn() == 1) {
                    fields.add(this.field.getBoard().getField(x - 1, y - 1));
                }
                if (x + 1 < 8 && (field = this.field.getBoard().getField(x + 1, y)).getFigure() != null
                        && field.getFigure().color != color
                        && field.getFigure() instanceof Pawn
                        && field.getBoard().getCurrentTurn() - field.getFigure().getFirstTurn() == 1) {
                    fields.add(this.field.getBoard().getField(x + 1, y - 1));
                }
            }
        } else {
            if ((field = this.field.getBoard().getField(x, y + 1)) != null && field.getFigure() == null) {
                fields.add(field);
                if (y == 1 && (field = this.field.getBoard().getField(x, y + 2)) != null && field.getFigure() == null) {
                    fields.add(field);
                }
            }
            if (x + 1 < 8 && (field = this.field.getBoard().getField(x + 1, y + 1)) != null && field.getFigure() != null
                    && field.getFigure().color != color) {
                fields.add(field);
            }
            if (x - 1 >= 0 && (field = this.field.getBoard().getField(x - 1, y + 1)) != null && field.getFigure() != null
                    && field.getFigure().color != color) {
                fields.add(field);
            }
            if (y == 4) {
                if (x - 1 >= 0 && (field = this.field.getBoard().getField(x - 1, y)).getFigure() != null
                        && field.getFigure().color != color
                        && field.getFigure() instanceof Pawn
                        && field.getBoard().getCurrentTurn() - field.getFigure().getFirstTurn() == 1) {
                    fields.add(this.field.getBoard().getField(x - 1, y + 1));
                }
                if (x + 1 < 8 && (field = this.field.getBoard().getField(x + 1, y)).getFigure() != null
                        && field.getFigure().color != color
                        && field.getFigure() instanceof Pawn
                        && field.getBoard().getCurrentTurn() - field.getFigure().getFirstTurn() == 1) {
                    fields.add(this.field.getBoard().getField(x + 1, y + 1));
                }
            }
        }
        return fields;
    }

    @Override
    public Figure postTurnAction(ChessField oldField, ChessField newField) {
        //kill en passant pawn
        Figure pawn = null;
        if (color == Color.BLACK && field.getY() == 5
                && (pawn = field.getBoard().getField(x, 4).getFigure()) != null
                && pawn instanceof Pawn
                && field.getBoard().getCurrentTurn() - pawn.getFirstTurn() == 1) {
            pawn.getField().setFigure(null);
        }
        if (color == Color.WHITE && field.getY() == 2
                && (pawn = field.getBoard().getField(x, 3).getFigure()) != null
                && pawn instanceof Pawn
                && field.getBoard().getCurrentTurn() - pawn.getFirstTurn() == 1) {
            pawn.getField().setFigure(null);
        }
        return pawn;
    }
}
