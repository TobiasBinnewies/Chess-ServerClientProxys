package org.example.Proxy;

import com.google.gson.JsonObject;
import org.example.exceptions.GameException;
import org.example.exceptions.IllegalMoveException;
import org.example.exceptions.JsonException;
import org.example.exceptions.PlayerException;
import org.example.game.Color;

public interface IChessServer {
    String createGame(IPlayer player, Color color) throws JsonException;

    Color joinGame(IPlayer player, String gameId) throws GameException, PlayerException;

    void leaveGame(IPlayer player, String gameId) throws GameException, PlayerException;

    void movePiece(IPlayer player, String gameId, int fromX, int fromY, int toX, int toY, String promotionFigure) throws GameException, PlayerException, IllegalMoveException;

    JsonObject getBoard(String gameId) throws GameException;
}
