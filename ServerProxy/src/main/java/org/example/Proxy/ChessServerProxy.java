package org.example.Proxy;

import org.example.RPC.RpcMessage;
import org.example.RPC.RpcReader;
import org.example.RPC.RpcWriter;
import org.example.exceptions.GameException;
import org.example.exceptions.IllegalMoveException;
import org.example.exceptions.JsonException;
import org.example.exceptions.PlayerException;
import org.example.game.Color;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ChessServerProxy implements Runnable {
    private final Socket socket;

    private final IChessServer chessServer;
    private RpcWriter writer;
    private RpcReader reader;
    private boolean running = true;

    private final Map<String, IPlayer> connectedPlayers = new HashMap<>();

    private enum Protocol {
        MOVE_PIECE, GET_BOARD, CREATE_GAME, JOIN_GAME, LEAVE_GAME, END_CONNECTION;

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
                case MOVE_PIECE -> getProtocolBegin() + "<gameId>,<fromX>,<fromY>,<toX>,<toY>,<promotion>";
                case JOIN_GAME, LEAVE_GAME, GET_BOARD -> getProtocolBegin() + "<gameId>";
                case CREATE_GAME, END_CONNECTION -> getProtocolBegin();
            };
        }

        private String getProtocolBegin() {
            return this.name() + " - " + this.ordinal() + ":";
        }
    }

    public ChessServerProxy(Socket socket, IChessServer chessServer) {
        this.socket = socket;
        this.chessServer = chessServer;
    }

    @Override
    public void run() {
        try {
            this.reader = new RpcReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new RpcWriter(new OutputStreamWriter(socket.getOutputStream()));

            ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
            exec.scheduleAtFixedRate(this::stillAlive, 0, 3, TimeUnit.SECONDS);

            while (running) {
                writer.sendMessage(999, Protocol.getProtocol());
                var message = reader.readRpcMessage();
                switch (Protocol.fromOrdinal(message.getCode())) {
                    case MOVE_PIECE -> movePiece(message);
                    case GET_BOARD -> getBoard(message);
                    case JOIN_GAME -> join(message);
                    case CREATE_GAME -> createGame(message);
                    case END_CONNECTION -> endConnection();
                    case LEAVE_GAME -> leave(message);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void endConnection() throws IOException {
        this.running = false;
        socket.close();
        writer.sendSuccess();
    }

    public void movePiece(RpcMessage message) {
        try {
            chessServer.movePiece(
                    getPlayer(),
                    message.getArg(0),
                    Integer.parseInt(message.getArg(1)),
                    Integer.parseInt(message.getArg(2)),
                    Integer.parseInt(message.getArg(3)),
                    Integer.parseInt(message.getArg(4)),
                    message.getArg(5)
            );
            writer.sendSuccess();
        } catch (IllegalMoveException e) {
            // TODO: Send message to enemy player, kick player from game
            writer.sendMessage(e.getErrorCode(), e.getMessage());
        } catch (GameException e) {
            writer.sendMessage(e.getErrorCode(), e.getMessage());
        } catch (PlayerException e) {
            writer.sendMessage(e.getErrorCode(), e.getMessage());
        } catch (IOException e) {
            writer.sendMessage(5, e.getMessage());
        }
    }

    public void getBoard(RpcMessage message) {
        try {
            writer.sendRpcMessage(new RpcMessage(0, chessServer.getBoard(message.getArg(0)).toString()));
        } catch (GameException e) {
            writer.sendMessage(e.getErrorCode(), e.getMessage());
        }
    }

    public void createGame(RpcMessage message) {
        try {
            String gameId = chessServer.createGame(getPlayer(), Color.valueOf(message.getArg(0)));
            writer.sendMessage(0, gameId);
        } catch (JsonException e) {
            writer.sendMessage(e.getErrorCode(), e.getMessage());
        } catch (IOException e) {
            writer.sendMessage(5, e.getMessage());
        }
    }

    public void join(RpcMessage message) {
        try {
            Color color = chessServer.joinGame(getPlayer(), message.getArg(0));
            writer.sendMessage(0, color.toString());
        } catch (GameException e) {
            writer.sendMessage(e.getErrorCode(), e.getMessage());
        } catch (PlayerException e) {
            writer.sendMessage(e.getErrorCode(), e.getMessage());
        } catch (IOException e) {
            writer.sendMessage(5, e.getMessage());
        }
    }

    public void leave(RpcMessage message) {
        try {
            chessServer.leaveGame(getPlayer(), message.getArg(0));
            writer.sendSuccess();
        } catch (GameException e) {
            writer.sendMessage(e.getErrorCode(), e.getMessage());
        } catch (PlayerException e) {
            writer.sendMessage(e.getErrorCode(), e.getMessage());
        } catch (IOException e) {
            writer.sendMessage(5, e.getMessage());
        }
    }

    private IPlayer getPlayer() throws IOException {
        writer.sendMessage(999, "USER_INFORMATION 0: <id>");
        String playerId = reader.readRpcMessage().getMessage();
        if (connectedPlayers.containsKey(playerId)) {
            writer.sendSuccess();
            return connectedPlayers.get(playerId);
        }
        writer.sendMessage(999, "CONNECTION_INFORMATION 0: <ip>,<port>");
        var connectionInformation = reader.readRpcMessage();
        String ip = connectionInformation.getArg(0);
        int port = Integer.parseInt(connectionInformation.getArg(1));
        IPlayer player = new PlayerClientProxy(new Socket(ip, port), playerId);
        connectedPlayers.put(playerId, player);
        return player;
    }

    private void stillAlive() {
        for (IPlayer player : connectedPlayers.values()) {
            try {
                if (!player.stillAlive()) {
                    ChessGame game = player.currentGame();
                    if (game != null) {
                        game.removePlayer(player);
                    }
                    connectedPlayers.remove(player.getId());
                }
            } catch (IOException e) {
                connectedPlayers.remove(player.getId());
            }
        }
    }
}
