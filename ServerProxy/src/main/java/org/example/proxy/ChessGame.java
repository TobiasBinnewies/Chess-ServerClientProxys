package org.example.Proxy;

import com.google.gson.JsonObject;
import org.example.exceptions.IllegalMoveException;
import org.example.exceptions.JsonException;
import org.example.exceptions.PlayerException;
import org.example.game.BoardLoader;
import org.example.game.ChessBoard;
import org.example.game.ChessField;
import org.example.game.Color;
import org.example.game.figure.Figure;
import org.example.game.figure.FigureFactory;
import org.example.game.figure.Pawn;

import java.util.Objects;

public class ChessGame {
    private final String id;
    private IPlayer whitePlayer;
    private IPlayer blackPlayer;

    private final ChessBoard board;
    private final BoardLoader boardLoader;
    private final FigureFactory figureFactory;

    public ChessGame(String id, IPlayer player, Color playerColor) throws JsonException {
        this.board = new ChessBoard();
        this.figureFactory = new FigureFactory(board);
        this.boardLoader = new BoardLoader(figureFactory);
        boardLoader.loadNewBoard(board);
        this.id = id;
        switch (playerColor) {
            case WHITE -> {
                whitePlayer = player;
                messagePlayers("White player has joined");
            }
            case BLACK -> {
                blackPlayer = player;
                messagePlayers("Black player has joined");
            }
        }
    }

    public void movePiece(IPlayer player, int fromX, int fromY, int toX, int toY, String promotionFigure) throws IllegalMoveException, PlayerException {
        if (whitePlayer == null || blackPlayer == null) {
            throw new PlayerException("Both players must be added before starting the game");
        }
        Color playerColor;
        if (Objects.equals(whitePlayer.getId(), player.getId())) {
            playerColor = Color.WHITE;
        } else if (Objects.equals(blackPlayer.getId(), player.getId())) {
            playerColor = Color.BLACK;
        } else {
            throw new PlayerException("Player is not part of this game");
        }

        Figure figure = board.getField(fromX, fromY).getFigure();
        ChessField field = board.getField(toX, toY);
        if (figure == null) {
            throw new IllegalMoveException("No figure on field " + fromX + "," + fromY);
        }
        if (!figure.canMoveTo(field)) {
            throw new IllegalMoveException("Figure cannot move to " + toX + "," + toY);
        }

        figure.move(field);

        boolean isPromotion = figure instanceof Pawn && (toY == 0 || toY == 7);
        if (isPromotion) {
            figureFactory.createFigure(playerColor, promotionFigure, field);
        }

        board.nextTurn();

        blackPlayer.updateGame(fromX, fromY, toX, toY, isPromotion ? promotionFigure : "", isFinished(), board.getCurrentMessage());
        whitePlayer.updateGame(fromX, fromY, toX, toY, isPromotion ? promotionFigure : "", isFinished(), board.getCurrentMessage());
    }

    public JsonObject getBoard() {
        return boardLoader.getCurrentBoard(board);
    }

    public Color join(IPlayer player) throws PlayerException {
        if (whitePlayer == null) {
            whitePlayer = player;
            startGame();
            messagePlayers("White player has joined");
            return Color.WHITE;
        }
        if (blackPlayer == null) {
            blackPlayer = player;
            startGame();
            messagePlayers("Black player has joined");
            return Color.BLACK;
        }
        throw new PlayerException("Both players have already joined");
    }

    public void leave(IPlayer player) {
        if (whitePlayer != null && Objects.equals(whitePlayer.getId(), player.getId())) {
            whitePlayer = null;
            messagePlayers("White player has left");
        }
        if (blackPlayer != null && Objects.equals(blackPlayer.getId(), player.getId())) {
            blackPlayer = null;
            messagePlayers("Black player has left");
        }
    }

    public void messagePlayers(String message) {
        if (whitePlayer != null) whitePlayer.receiveMessage(message);
        if (blackPlayer != null) blackPlayer.receiveMessage(message);
    }

    public void removePlayer(IPlayer player) {
        if (whitePlayer != null && Objects.equals(whitePlayer.getId(), player.getId())) {
            whitePlayer = null;
            messagePlayers("White player has left");
        }
        if (blackPlayer != null && Objects.equals(blackPlayer.getId(), player.getId())) {
            blackPlayer = null;
            messagePlayers("Black player has left");
        }
    }

    public String getId() {
        return id;
    }

    public boolean isFinished() {
        return board.isGameOver();
    }

    public boolean isEmpty() {
        return whitePlayer == null && blackPlayer == null;
    }

    private void startGame() {
        whitePlayer.startGame();
        blackPlayer.startGame();
    }
}
