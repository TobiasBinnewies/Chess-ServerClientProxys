package org.example.game.figure;

import org.example.game.ChessBoard;
import org.example.game.ChessField;
import org.example.game.Color;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class Figure implements Serializable {

    transient ChessField field;
    private final ChessBoard board;
    int x, y = -1;
    Color color;
    private final String name;
    private int firstTurn = -1;

    Figure(Color color, String name, ChessField field) {
        this.color = color;
        this.name = name;
        this.board = field.getBoard();
        setField(field);
        this.x = field.getX();
        this.y = field.getY();
        field.setFigure(this);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Color getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    public String getPos() {
        return ((char) (x + 97)) + "" + ((char) (7 - y + 49));
    }

    //returns whether the Figure was successfully moved
    public Figure move(ChessField field) {
        Figure killedFigure = field.getFigure();
        field.setFigure(this);
        if (this.field != null) {
            this.field.setFigure(null);
        }
        int oldX = x;
        int oldY = y;
        x = field.getX();
        y = field.getY();
        setField(field);
        Figure postFigure = postTurnAction(board.getField(oldX, oldY), field);
        if (firstTurn < 0) {
            firstTurn = field.getBoard().getCurrentTurn();
        }
        if (killedFigure != null || !(postFigure instanceof Pawn)) {
            board.set50MoveRuleTurns(0);
        }
        return killedFigure == null ? postFigure : killedFigure;
    }

    public boolean canMoveTo(ChessField field) {
        return canMove() && getAllAccessibleFields().contains(field);
    }

    public boolean canMove() {
        return getField().getBoard().getTurn() == color;
    }

    public void setField(ChessField field) {
        this.field = field;
    }

    public ChessField getField() {
        return field;
    }

    //executed after this Figure has been moved to the parameter field
    public Figure postTurnAction(ChessField oldField, ChessField newField) {
        return null;
    }

    //this also considers the king being in check
    public List<ChessField> getAllAccessibleFields() {
        List<ChessField> fields = getAccessibleFields();
        King king = field.getBoard().getKing(color);
        if (king != null) {
            List<ChessField> trueFields = new ArrayList<>();
            ChessField oldField = field;
            for (ChessField to : fields) {
                //simulate move to that field
                Figure killedFigure = move(to);
                field.getBoard().recalculateAttackedFields();
                boolean check = king.isCheck();
                if (!check) {
                    trueFields.add(to);
                }
                //revert move
                move(oldField);
                if (killedFigure != null) {
                    killedFigure.field.setFigure(killedFigure);
                }
                field.getBoard().recalculateAttackedFields();
            }
            fields = trueFields;
        }
        return fields;
    }

    public int getFirstTurn() {
        return firstTurn;
    }

    public void setFirstTurn(int firstTurn) {
        this.firstTurn = firstTurn;
    }

    //returns a list of all accessible fields of this figure, including fields with opponent's figures
    //that can be beaten
    public abstract List<ChessField> getAccessibleFields();

    @Override
    public String toString() {
        return "Figure:<" + color.getName() +
                " " + name + " " +
                "x=" + x + " y=" + y +
                " Field=" + field + ">";
    }
}
