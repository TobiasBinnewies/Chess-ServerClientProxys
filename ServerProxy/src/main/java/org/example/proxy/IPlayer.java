package org.example.Proxy;

import java.io.IOException;

public interface IPlayer {
    void updateGame(int fromX, int fromY, int toX, int toY, String promotionFigure, boolean gameOver, String message);

    void receiveMessage(String message);

    void startGame();

    boolean stillAlive() throws IOException;

    String getId();

    void setCurrentGame(ChessGame game);

    ChessGame currentGame();
}
