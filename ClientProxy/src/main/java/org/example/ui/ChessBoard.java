package org.example.ui;

import javafx.scene.layout.GridPane;
import org.example.ui.figure.Color;
import org.example.ui.figure.Figure;
import org.example.ui.figure.King;

import java.util.*;

public class ChessBoard extends GridPane {
    private final ChessField[] fields = new ChessField[64];
    private final Map<Color, Set<ChessField>> attackedFields = new HashMap<>();
    private int currentTurn = 1;


    ChessBoard() {
        resetAttackedFields();
        for (int i = 0; i < 64; i++) {
            int x = getX(i);
            int y = getY(i);
            ChessField field = new ChessField(this, x, y);
            add(field, x, y);
            fields[i] = field;
        }
        recalculateAttackedFields();
    }

    private void resetAttackedFields() {
        attackedFields.put(Color.BLACK, new HashSet<>());
        attackedFields.put(Color.WHITE, new HashSet<>());
    }

    private int getX(int index) {
        return index % 8;
    }

    private int getY(int index) {
        return (index - getX(index)) / 8;
    }

    public ChessField getField(int x, int y) {
        return x < 0 || x > 7 || y < 0 || y > 7 ? null : fields[y * 8 + x];
    }

    public void setFigure(Figure figure) {
        getField(figure.getX(), figure.getY()).setFigure(figure, true);
    }

    public int getCurrentTurn() {
        return currentTurn;
    }

    public void setCurrenTurn(int currentTurn) {
        this.currentTurn = currentTurn;
    }

    public void nextTurn() {
        currentTurn++;
        recalculateAttackedFields();
    }

    public Color getTurn() {
        return currentTurn % 2 == 0 ? Color.BLACK : Color.WHITE;
    }


    public void recalculateAttackedFields() {
        resetAttackedFields();
        Arrays.stream(fields).filter(f -> f.figure != null && f.figure.getColor() == Color.WHITE).forEach(f -> attackedFields.get(Color.WHITE).addAll(f.getFigure().getAccessibleFields()));
        Arrays.stream(fields).filter(f -> f.figure != null && f.figure.getColor() == Color.BLACK).forEach(f -> attackedFields.get(Color.BLACK).addAll(f.getFigure().getAccessibleFields()));
    }

    public Set<ChessField> getAllAccessibleFields(Color color) {
        return attackedFields.get(color);
    }

    public King getKing(Color color) {
        for (ChessField field : fields) {
            if (field.figure instanceof King && field.figure.getColor() == color) {
                return (King) field.figure;
            }
        }
        return null;
    }

    public List<Figure> getFigures(Color color) {
        List<Figure> figures = new ArrayList<>();
        Arrays.stream(fields).filter(f -> f.figure != null && (color == null || f.figure.getColor() == color)).forEach(f -> figures.add(f.figure));
        return figures;
    }

    public void clear() {
        for (ChessField field : fields) {
            field.setFigure(null, true);
        }
        recalculateAttackedFields();
        currentTurn = 1;
    }

}
