package org.example.proxy;

public interface IPlayer {
    void updateGame(int fromX, int fromY, int toX, int toY, String promotionFigure, boolean gameOver, String message) throws IllegalStateException;

    void receiveMessage(String message);

    void startGame();

    String getId();
}
