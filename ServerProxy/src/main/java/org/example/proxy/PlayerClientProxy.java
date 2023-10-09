package org.example.proxy;

import com.google.gson.JsonObject;
import org.example.RPC.RpcMessage;
import org.example.RPC.RpcReader;
import org.example.RPC.RpcWriter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class PlayerClientProxy implements IPlayer {
    private final Socket socket;
    private final RpcWriter writer;
    private final RpcReader reader;

    private final String id;

    private ChessGame currentGame;

    enum Protocol {
        UPDATE_GAME, RECEIVE_MESSAGE, START_GAME, STILL_ALIVE;
    }

    public PlayerClientProxy(Socket socket, String id) throws IOException {
        this.socket = socket;
        this.writer = new RpcWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.reader = new RpcReader(new InputStreamReader(socket.getInputStream()));
        this.id = id;
    }

    @Override
    public void updateGame(int fromX, int fromY, int toX, int toY, String promotionFigure, boolean gameOver, String message) {
        JsonObject json = new JsonObject();
        json.addProperty("fromX", fromX);
        json.addProperty("fromY", fromY);
        json.addProperty("toX", toX);
        json.addProperty("toY", toY);
        json.addProperty("promotionFigure", promotionFigure);
        json.addProperty("gameOver", gameOver);
        json.addProperty("message", message);
        try {
            reader.readRpcMessage();
            writer.sendRpcMessage(
                    new RpcMessage(Protocol.UPDATE_GAME.ordinal(), json.toString())
            );
            RpcMessage msg = reader.readRpcMessage();
            if (msg.getCode() != 0) {
                throw new RuntimeException(msg.getMessage());
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void receiveMessage(String message) {
        try {
            reader.readRpcMessage();
            writer.sendMessage(Protocol.RECEIVE_MESSAGE.ordinal(), message);
            RpcMessage msg = reader.readRpcMessage();
            if (msg.getCode() != 0) {
                throw new RuntimeException(msg.getMessage());
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void startGame() {
        try {
            reader.readRpcMessage();
            writer.sendMessage(Protocol.START_GAME.ordinal(), "");
            RpcMessage msg = reader.readRpcMessage();
            if (msg.getCode() != 0) {
                throw new RuntimeException(msg.getMessage());
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean stillAlive() {
        boolean stillAlive = stillAliveIntern();
        if (!stillAlive) {
            try {
                currentGame.removePlayer(this);
                socket.close();
            } catch (IOException e) {
                return false;
            }
        }
        return stillAlive;
    }

    private boolean stillAliveIntern() {
        try {
            reader.readRpcMessage();
            writer.sendMessage(Protocol.STILL_ALIVE.ordinal(), "");
            RpcMessage msg = reader.readRpcMessage();
            return msg.getCode() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    public void setCurrentGame(ChessGame game) {
        this.currentGame = game;
    }

    public ChessGame currentGame() {
        return currentGame;
    }
}
