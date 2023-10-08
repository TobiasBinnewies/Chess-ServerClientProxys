package org.example.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.*;
import org.example.exceptions.GameException;
import org.example.exceptions.IllegalMoveException;
import org.example.exceptions.PlayerException;
import org.example.ui.figure.Color;
import org.example.ui.figure.Figure;
import org.example.ui.figure.Pawn;

import java.util.List;
import java.util.Optional;

public class ChessField extends Label {

    Figure figure;
    private final int x, y;
    private final ChessBoard board;
    private static final String defaultStyleBlack = "-fx-background-color: gray;";
    private static final String defaultStyleWhite = "-fx-background-color: white;";
    private static final String highlightStyleBlack = "-fx-background-color: forestgreen;";
    private static final String highlightStyleWhite = "-fx-background-color: palegreen;";
    private static final String highlightKillStyleBlack = "-fx-background-color: darkred;";
    private static final String highlightKillStyleWhite = "-fx-background-color: #ff5555;";

    private static final int dragViewOffset;

    static {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("windows")) {
            dragViewOffset = 25;
        } else {
            dragViewOffset = 0;
        }
    }

    ChessField(ChessBoard board, int x, int y) {
        this.board = board;
        this.x = x;
        this.y = y;
        setAlignment(Pos.CENTER);
        resetBackgroundColor();
        setOnDragDetected(this::onDragDetected);
        setOnDragOver(this::onDragOver);
        setOnDragDropped(this::onDragDropped);
        setOnDragDone(this::onDragDone);
        setOnMouseEntered(e -> onMouseEntered());
        setOnMouseExited(e -> onMouseExited());
        setMinSize(50, 50);
        setMaxSize(50, 50);
    }

    private void setHighlightEmpty() {
        setStyle(getColor() == Color.BLACK ? highlightStyleBlack : highlightStyleWhite);
    }

    private void setHighlightKill() {
        setStyle(getColor() == Color.BLACK ? highlightKillStyleBlack : highlightKillStyleWhite);
    }

    private void resetBackgroundColor() {
        setStyle(getColor() == Color.BLACK ? defaultStyleBlack : defaultStyleWhite);
    }

    private boolean isEnPassantField(Figure movingFigure) {
        Figure figure;
        return movingFigure instanceof Pawn
                && (y == 2
                && (figure = board.getField(x, 3).getFigure()) instanceof Pawn
                && board.getCurrentTurn() - figure.getFirstTurn() == 1
                || y == 5
                && (figure = board.getField(x, 4).getFigure()) instanceof Pawn
                && board.getCurrentTurn() - figure.getFirstTurn() == 1);
    }

    private Color getColor() {
        return x % 2 == 1 && y % 2 == 1 || x % 2 == 0 && y % 2 == 0 ? Color.WHITE : Color.BLACK;
    }

    public ChessBoard getBoard() {
        return board;
    }

    public void setFigure(Figure figure, boolean graphic) {
        this.figure = figure;
        if (graphic) {
            if (figure == null) {
                setGraphic(null);
            } else {
                setGraphic(figure.getImageView());
            }
        }
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

    private void onMouseEntered() {
        List<ChessField> trueFields;
        if (figure != null && figure.canMove() && !(trueFields = figure.getAllAccessibleFields()).isEmpty()) {
            for (ChessField field : trueFields) {
                if (field.figure != null || field.isEnPassantField(figure)) {
                    field.setHighlightKill();
                } else {
                    field.setHighlightEmpty();
                }
            }
        }
    }

    private void onMouseExited() {
        List<ChessField> trueFields;
        if (figure != null && figure.canMove() && !(trueFields = figure.getAllAccessibleFields()).isEmpty()) {
            trueFields.forEach(ChessField::resetBackgroundColor);
        }
    }

    private void onDragDetected(MouseEvent e) {
        List<ChessField> trueFields;
        if (figure != null && figure.canMove() && !(trueFields = figure.getAllAccessibleFields()).isEmpty()) {
            Dragboard db = startDragAndDrop(TransferMode.MOVE);
            db.setDragView(figure.getImage());
            db.setDragViewOffsetX(dragViewOffset);
            db.setDragViewOffsetY(dragViewOffset);
            ClipboardContent content = new ClipboardContent();
            content.put(Figure.CHESS_FIGURE, figure);
            db.setContent(content);
            for (ChessField field : trueFields) {
                if (field.figure != null || field.isEnPassantField(figure)) {
                    field.setHighlightKill();
                } else {
                    field.setHighlightEmpty();
                }
            }
            e.consume();
        }
    }

    private void onDragOver(DragEvent e) {
        if (e.getDragboard().hasContent(Figure.CHESS_FIGURE)) {
            e.acceptTransferModes(TransferMode.MOVE);
        }
        e.consume();
    }

    private void onDragDone(DragEvent e) {
        Dragboard db = e.getDragboard();
        if (db.hasContent(Figure.CHESS_FIGURE)) {
            Figure source = deserializeFigure(db);
            //use the cloned instance from the Dragboard because we don't want to touch the original instance anymore
            source.getAccessibleFields().forEach(ChessField::resetBackgroundColor);
        }
        e.consume();
    }

    private void onDragDropped(DragEvent e) {
        Dragboard db = e.getDragboard();
        if (db.hasContent(Figure.CHESS_FIGURE)) {
            Figure source = deserializeFigure(db);
            source = board.getField(source.getX(), source.getY()).getFigure();
            if (source.canMoveTo(this)) {
                try {
                    boolean isPromotion = source instanceof Pawn && (y == 0 || y == 7);
                    Figure newFigure = null;
                    if (isPromotion) {
                        Optional<Figure> result = new PromotionDialog((Pawn) source).showAndWait();
                        if (result.isPresent()) {
                            result.get().move(this, true);
                            newFigure = result.get();
                        }
                    }
                    ChessGameUI.getProxy().movePiece(ChessGameUI.getPlayer(), ChessGameUI.getGameId(), source.getX(), source.getY(), x, y, newFigure == null ? "null" : newFigure.getName());
                    resetBackgroundColor();
                    source.getAccessibleFields().forEach(ChessField::resetBackgroundColor);
                    source.move(this, true);
                    board.nextTurn();
                } catch (IllegalMoveException ex) {
                    ChessGameUI.displayMessage(ex.getMessage());
                } catch (GameException | PlayerException ex) {
                    ChessGameUI.resetGame();
                    ChessGameUI.displayMessage(ex.getMessage());
                    ChessGameUI.displayPopupMessage(ex.getMessage());
                }
            }
        }
        e.consume();
    }

    //returns a NEW INSTANCE of the figure that was serialized on the Dragboard
    private Figure deserializeFigure(Dragboard db) {
        Figure source = (Figure) db.getContent(Figure.CHESS_FIGURE);
        source.setField(ChessGameUI.getBoard().getField(source.getX(), source.getY()));
        return source;
    }

    @Override
    public String toString() {
        return "<" + x + "," + y + ">";
    }
}
