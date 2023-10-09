package org.example.proxy;

import com.google.gson.JsonObject;
import org.example.exceptions.GameException;
import org.example.exceptions.IllegalMoveException;
import org.example.exceptions.JsonException;
import org.example.exceptions.PlayerException;
import org.example.game.Color;
import org.example.utils.RandomStringGenerator;

import java.util.HashMap;
import java.util.Map;

public class ChessServer implements IChessServer {
    private final Map<String, ChessGame> games = new HashMap<>();

    @Override
    public String createGame(IPlayer player, Color color) throws JsonException {
        String id;
        do {
            id = RandomStringGenerator.generateRandomString(6);
        } while (games.containsKey(id));
        ChessGame game = new ChessGame(id, player, color);
        String gameId = game.getId();
        games.put(gameId, game);
        player.setCurrentGame(game);
        return gameId;
    }

    @Override
    public Color joinGame(IPlayer player, String gameId) throws GameException, PlayerException {
        ChessGame game = getGame(gameId);
        player.setCurrentGame(game);
        return game.join(player);
    }

    @Override
    public void leaveGame(IPlayer player, String gameId) throws GameException {
        ChessGame game = getGame(gameId);
        game.leave(player);
        if (game.isEmpty()) {
            games.remove(gameId);
        }
    }

    @Override
    public void movePiece(IPlayer player, String gameId, int fromX, int fromY, int toX, int toY, String promotionFigure) throws GameException, PlayerException, IllegalMoveException {
        ChessGame game = getGame(gameId);
        game.movePiece(player, fromX, fromY, toX, toY, promotionFigure);
        if (game.isFinished()) {
            games.remove(gameId);
        }
    }

    @Override
    public JsonObject getBoard(String gameId) throws GameException {
        ChessGame game = getGame(gameId);
        return game.getBoard();
    }

    public ChessGame getGame(String gameId) throws GameException {
        ChessGame game = games.get(gameId);
        if (game == null) {
            throw new GameException("Game with id " + gameId + " does not exist");
        }
        return game;
    }
}
