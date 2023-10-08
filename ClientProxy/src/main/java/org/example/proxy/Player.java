package org.example.proxy;

import javafx.application.Platform;
import org.example.ui.ChessField;
import org.example.ui.ChessGameUI;
import org.example.ui.figure.Color;
import org.example.ui.figure.Figure;
import org.example.ui.figure.FigureFactory;

import java.util.Objects;

public class Player implements IPlayer {

    private Color color;
    private final String id;

    public Player(String id) {
        this.id = id;
    }

    @Override
    public void updateGame(int fromX, int fromY, int toX, int toY, String promotionFigure, boolean gameOver, String message) throws IllegalStateException {
        Platform.runLater(() -> {
            if (gameOver) {
                ChessGameUI.displayMessage(message);
                ChessGameUI.showGameConnectionPopup();
                return;
            }
            Figure figure = ChessGameUI.getBoard().getField(fromX, fromY).getFigure();
            if (figure == null || figure.getColor() == color) {
                return;
            }
            ChessField field = ChessGameUI.getBoard().getField(toX, toY);
            if (!Objects.equals(promotionFigure, "")) {
                figure.getField().setFigure(null, true);
                FigureFactory.createFigure(color.revert(), promotionFigure, field);
            } else {
                figure.move(field, true);
            }
            ChessGameUI.getBoard().nextTurn();
        });
    }

    @Override
    public void receiveMessage(String message) {
        Platform.runLater(
                () -> ChessGameUI.displayMessage(message)
        );
    }

    @Override
    public void startGame() {
        ChessGameUI.startGame();
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    @Override
    public String getId() {
        return id;
    }
}
