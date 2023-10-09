package org.example.proxy;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.exceptions.GameException;
import org.example.exceptions.IllegalMoveException;
import org.example.exceptions.JsonException;
import org.example.exceptions.PlayerException;
import org.example.rpc.RpcMessage;
import org.example.rpc.RpcReader;
import org.example.rpc.RpcWriter;
import org.example.ui.figure.Color;
import org.example.utils.LocalIPv4;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChessServerClientProxy implements IChessServer {
    private final Socket socket;
    private final RpcWriter writer;
    private final RpcReader reader;

    private final Set<PlayerServerProxy> openPlayerProxys = new HashSet<>();

    private enum Protocol {
        MOVE_PIECE, GET_BOARD, CREATE_GAME, JOIN_GAME, LEAVE_GAME, END_CONNECTION;
    }

    public ChessServerClientProxy(Socket socket) throws IOException {
        this.socket = socket;
        this.reader = new RpcReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new RpcWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    @Override
    public void movePiece(IPlayer player, String gameId, int fromX, int fromY, int toX, int toY, String promotionFigure) throws GameException, PlayerException, IllegalMoveException {
        try {
            reader.readRpcMessage();
            writer.sendRpcMessage(
                    new RpcMessage(Protocol.MOVE_PIECE.ordinal(),
                            gameId,
                            String.valueOf(fromX),
                            String.valueOf(fromY),
                            String.valueOf(toX),
                            String.valueOf(toY),
                            promotionFigure
                    )
            );
            sendPlayer(player);
            RpcMessage msg = reader.readRpcMessage();
            switch (msg.getCode()) {
                case 0:
                    break;
                case 1:
                    throw new GameException(msg.getMessage());
                case 2:
                    throw new PlayerException(msg.getMessage());
                case 3:
                    throw new IllegalMoveException(msg.getMessage());
                default:
                    throw new RuntimeException(msg.getMessage());
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public JsonObject getBoard(String gameId) {
        try {
            reader.readRpcMessage();
            writer.sendRpcMessage(
                    new RpcMessage(Protocol.GET_BOARD.ordinal(), gameId)
            );
            RpcMessage msg = reader.readRpcMessage();
            if (msg.getCode() != 0) {
                throw new RuntimeException(msg.getArg(0));
            }
            return JsonParser.parseString(msg.getMessage()).getAsJsonObject();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public String createGame(IPlayer player, Color color) throws JsonException {
        try {
            reader.readRpcMessage();
            writer.sendRpcMessage(
                    new RpcMessage(Protocol.CREATE_GAME.ordinal(), color.name())
            );
            sendPlayer(player);
            RpcMessage msg = reader.readRpcMessage();
            if (msg.getCode() == 4) {
                throw new JsonException(msg.getMessage());
            }
            if (msg.getCode() != 0) {
                throw new RuntimeException(msg.getMessage());
            }
            return msg.getMessage();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Color joinGame(IPlayer player, String gameId) throws GameException, PlayerException {
        try {
            reader.readRpcMessage();
            writer.sendRpcMessage(
                    new RpcMessage(Protocol.JOIN_GAME.ordinal(), gameId)
            );
            sendPlayer(player);
            RpcMessage msg = reader.readRpcMessage();
            switch (msg.getCode()) {
                case 0:
                    break;
                case 1:
                    throw new GameException(msg.getMessage());
                case 2:
                    throw new PlayerException(msg.getMessage());
                default:
                    throw new RuntimeException(msg.getMessage());
            }
            return Color.valueOf(msg.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void leaveGame(IPlayer player, String gameId) {
        try {
            reader.readRpcMessage();
            writer.sendRpcMessage(
                    new RpcMessage(Protocol.LEAVE_GAME.ordinal(), gameId)
            );
            sendPlayer(player);
            RpcMessage msg = reader.readRpcMessage();
            if (msg.getCode() != 0) {
                throw new RuntimeException(msg.getMessage());
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void endConnection() {
        try {
            for (PlayerServerProxy playerServerProxy : openPlayerProxys) {
                if (playerServerProxy == null) {
                    continue;
                }
                playerServerProxy.endConnection();
            }
            reader.readRpcMessage();
            writer.sendRpcMessage(
                    new RpcMessage(Protocol.END_CONNECTION.ordinal())
            );
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void sendPlayer(IPlayer player) throws IOException {
        reader.readRpcMessage();
        writer.sendRpcMessage(new RpcMessage(0, player.getId()));
        RpcMessage message = reader.readRpcMessage();
        if (message.getCode() == 999) {
            ServerSocket serverSocket = new ServerSocket(0);
            String ip = LocalIPv4.get();
            int port = serverSocket.getLocalPort();
            writer.sendRpcMessage(
                    new RpcMessage(0, ip, String.valueOf(port))
            );
            Socket socket = serverSocket.accept();
            PlayerServerProxy playerServerProxy = new PlayerServerProxy(socket, player);
            Thread t = new Thread(playerServerProxy);
            openPlayerProxys.add(playerServerProxy);
            t.start();
        }
    }
}
