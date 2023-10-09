package org.example.proxy;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.rpc.RpcMessage;
import org.example.rpc.RpcReader;
import org.example.rpc.RpcWriter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class PlayerServerProxy implements Runnable {
    private final Socket socket;
    private final IPlayer player;
    private RpcReader reader;
    private RpcWriter writer;
    private boolean running = true;

    private enum Protocol {
        UPDATE_GAME, RECEIVE_MESSAGE, START_GAME, STILL_ALIVE, END_CONNECTION;

        public static Protocol fromOrdinal(int i) {
            for (Protocol protocol : Protocol.values()) {
                if (protocol.ordinal() == i) {
                    return protocol;
                }
            }
            throw new IllegalArgumentException("No enum constant " + Protocol.class.getName() + "." + i);
        }

        public static String getProtocol() {
            StringBuilder sb = new StringBuilder();
            for (Protocol protocol : Protocol.values()) {
                sb.append(protocol.getSyntax()).append(" ; ");
            }
            return sb.toString();
        }

        private String getSyntax() {
            return switch (this) {
                case UPDATE_GAME -> getProtocolBegin() + "<JSON>";
                case RECEIVE_MESSAGE -> getProtocolBegin() + "<message>";
                case START_GAME, STILL_ALIVE, END_CONNECTION -> getProtocolBegin();
            };
        }

        private String getProtocolBegin() {
            return this.name() + " - " + this.ordinal() + ":";
        }
    }

    public PlayerServerProxy(Socket socket, IPlayer player) {
        this.player = player;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            this.reader = new RpcReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new RpcWriter(new OutputStreamWriter(socket.getOutputStream()));
            while (running) {
                writer.sendMessage(0, Protocol.getProtocol());
                RpcMessage message = reader.readRpcMessage();
                switch (Protocol.fromOrdinal(message.getCode())) {
                    case UPDATE_GAME -> updateGame(message);
                    case RECEIVE_MESSAGE -> receiveMessage(message);
                    case END_CONNECTION -> endConnection();
                    case STILL_ALIVE -> stillAlive();
                    case START_GAME -> startGame();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void endConnection() throws IOException {
        this.running = false;
        socket.close();
    }

    private void updateGame(RpcMessage message) {
        JsonObject json = JsonParser.parseString(message.getMessage()).getAsJsonObject();
        try {
            player.updateGame(
                    json.get("fromX").getAsInt(),
                    json.get("fromY").getAsInt(),
                    json.get("toX").getAsInt(),
                    json.get("toY").getAsInt(),
                    json.get("promotionFigure").getAsString(),
                    json.get("gameOver").getAsBoolean(),
                    json.get("message").getAsString()
            );
            writer.sendSuccess();
        } catch (IllegalStateException e) {
            writer.println("1: " + e.getMessage());
        }
    }

    private void receiveMessage(RpcMessage message) {
        String msg = message.getArg(0);
        player.receiveMessage(msg);
        writer.sendSuccess();
    }

    private void startGame() {
        player.startGame();
        writer.sendSuccess();
    }

    private void stillAlive() {
        writer.sendSuccess();
    }
}
