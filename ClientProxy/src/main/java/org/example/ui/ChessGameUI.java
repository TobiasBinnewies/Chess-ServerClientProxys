package org.example.ui;

import com.google.gson.JsonObject;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Popup;
import javafx.util.Pair;
import org.example.exceptions.GameException;
import org.example.exceptions.JsonException;
import org.example.exceptions.PlayerException;
import org.example.proxy.ChessServerClientProxy;
import org.example.proxy.Player;
import org.example.ui.components.ToggleSwitch;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class ChessGameUI extends Application {

    private Label message;
    private final Label popupMessage = new Label();
    private ChessBoard board;
    private ChessServerClientProxy proxy;
    private Player player;

    private boolean gameStarted = false;
    private String gameId;
    private static ChessGameUI instance;

    private final Set<OptionButton> optionButtons = new HashSet<>();

    private javafx.stage.Stage primaryStage;

    @Override
    public void start(javafx.stage.Stage primaryStage) {

        instance = this;
        this.primaryStage = primaryStage;

        primaryStage.setTitle("Chess");

        BorderPane pane = new BorderPane();

        GridPane table = new GridPane();
        for (int i = 0; i < 8; i++) {
            table.add(newRowLabel(i), 0, i + 1, 1, 1);
            table.add(newRowLabel(i), 9, i + 1, 1, 1);
            table.add(newColLabel(i), i + 1, 0, 1, 1);
            table.add(newColLabel(i), i + 1, 9, 1, 1);
        }
        table.add(board = new ChessBoard(), 1, 1, 8, 8);
        table.setAlignment(Pos.CENTER);
        pane.setCenter(table);

        BorderPane menu = new BorderPane();
        menu.setPadding(new Insets(10, 10, 10, 0));

        GridPane options = new GridPane();
        OptionButton leaveButton = new OptionButton("images/remove.png", e -> resetGame(), "Leave");

        options.add(leaveButton, 0, 0, 1, 1);
        optionButtons.add(leaveButton);

        options.setAlignment(Pos.BOTTOM_RIGHT);

        menu.setRight(options);

        //status text
        message = new Label();
        message.setAlignment(Pos.BOTTOM_LEFT);
        message.setPadding(new Insets(10, 0, 10, 10));
        menu.setLeft(message);

//        status.setText("TEST");

        pane.setBottom(menu);

        //scene
        Scene scene = new Scene(pane, 440, 490);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setMinWidth(primaryStage.getWidth());
        primaryStage.setMinHeight(primaryStage.getHeight());

//        getServerConnectionPopup().show(primaryStage);
        new ConnectionDialog().showAndWait();

        primaryStage.setOnCloseRequest(event -> {
            proxy.endConnection();
        });
        new GameConnectionDialog().showAndWait();
    }

    class ConnectionDialog extends Dialog<Pair<String, Integer>> {
        boolean valid = false;
        ConnectionDialog() {
            BorderPane pane = new BorderPane();

            Text title = new Text("Server Connection");
            title.setStyle("-fx-font-weight: bold");
            title.setStyle("-fx-font-size: 20px");

            TextField ipInput = new TextField();
            ipInput.setText("172.30.33.15");
            Text ipLabel = new Text("IP Address");

            TextField portInput = new TextField();
            portInput.setText("9080");
            Text portLabel = new Text("Port");

            GridPane table = new GridPane();
            table.add(ipLabel, 0, 0);
            table.add(ipInput, 1, 0);
            table.add(portLabel, 0, 1);
            table.add(portInput, 1, 1);
            table.add(popupMessage, 0, 2, 2, 1);
            table.setAlignment(Pos.CENTER);
            table.setHgap(5);
            table.setVgap(5);

            pane.setPadding(new Insets(5, 5, 5, 5));

            pane.setTop(title);
            pane.setCenter(table);
            BorderPane.setAlignment(title, Pos.CENTER);
            BorderPane.setMargin(title, new Insets(0, 0, 5, 0));

            getDialogPane().setContent(pane);

            ButtonType connectBtnType = new ButtonType("Connect", ButtonBar.ButtonData.APPLY);
            getDialogPane().getButtonTypes().add(connectBtnType);
            Button connectBtn = (Button) getDialogPane().lookupButton(connectBtnType);
            connectBtn.addEventFilter(ActionEvent.ACTION, event -> {
                try {
                    String ip = ipInput.getText();
                    int port = Integer.parseInt(portInput.getText());
                    Socket socket = new Socket(ip, port);
                    player = new Player(UUID.randomUUID().toString());
                    proxy = new ChessServerClientProxy(socket);
                    displayPopupMessage("");

                    valid = true;
                } catch (NumberFormatException e) {
                    ChessGameUI.displayPopupMessage("Invalid port");
                    event.consume();
                } catch (UnknownHostException e) {
                    ChessGameUI.displayPopupMessage("Unknown host");
                    event.consume();
                } catch (IOException e) {
                    ChessGameUI.displayPopupMessage("Connection failed");
                    event.consume();
                }
            });

            ButtonType quitBtnType = new ButtonType("Quit", ButtonBar.ButtonData.CANCEL_CLOSE);
            getDialogPane().getButtonTypes().add(quitBtnType);
            Button quitBtn = (Button) getDialogPane().lookupButton(quitBtnType);
            quitBtn.addEventFilter(ActionEvent.ACTION, event -> {
                System.exit(0);
            });

            setOnCloseRequest(e -> {
                if (valid) return;
                System.exit(0);
            });
        }
    }

    class GameConnectionDialog extends Dialog<String> {
        boolean valid = false;
        GameConnectionDialog() {
            Text title = new Text("Game Connection");
            title.setStyle("-fx-font-weight: bold");
            title.setStyle("-fx-font-size: 20px");


            HBox box = new HBox();
            box.setSpacing(10);

            ToggleSwitch colorSwitch = new ToggleSwitch();
            Text colorLabel = new Text("Color");
            colorLabel.setStyle("-fx-font-weight: bold");
            colorLabel.setTextAlignment(TextAlignment.CENTER);
            Text whiteLabel = new Text("White");
            Text blackLabel = new Text("Black");

            GridPane colorTable = new GridPane();
            colorTable.add(whiteLabel, 0, 0);
            colorTable.add(blackLabel, 2, 0);
            colorTable.add(colorSwitch, 1, 0);
            colorTable.setHgap(5);

            GridPane createTable = new GridPane();
            createTable.add(colorLabel, 0, 0);
            createTable.add(colorTable, 0, 1);
            createTable.setAlignment(Pos.CENTER);
            createTable.setVgap(5);

            box.getChildren().add(createTable);

            TextField gameIdInput = new TextField();
            Text gameIdLabel = new Text("Game ID");
            gameIdLabel.setStyle("-fx-font-weight: bold");
            gameIdLabel.setTextAlignment(TextAlignment.CENTER);

            GridPane joinTable = new GridPane();
            joinTable.add(gameIdLabel, 0, 0);
            joinTable.add(gameIdInput, 0, 1);
            joinTable.setAlignment(Pos.CENTER);
            joinTable.setVgap(5);

            box.getChildren().add(joinTable);

            BorderPane pane = new BorderPane();

            pane.setTop(title);
            pane.setCenter(box);
            pane.setBottom(popupMessage);
            BorderPane.setAlignment(title, Pos.CENTER);
            BorderPane.setAlignment(message, Pos.CENTER);
            BorderPane.setMargin(title, new Insets(0, 0, 5, 0));
            BorderPane.setMargin(message, new Insets(5, 0, 0, 0));

            getDialogPane().setContent(pane);

            ButtonType joinBtnType = new ButtonType("Join", ButtonBar.ButtonData.APPLY);
            ButtonType createBtnType = new ButtonType("Create", ButtonBar.ButtonData.APPLY);

            getDialogPane().getButtonTypes().addAll(createBtnType, joinBtnType);

            Button joinBtn = (Button) getDialogPane().lookupButton(joinBtnType);
            Button createBtn = (Button) getDialogPane().lookupButton(createBtnType);

            joinBtn.addEventFilter(ActionEvent.ACTION, event -> {
                if (gameIdInput.getText().isEmpty()) {
                    ChessGameUI.displayPopupMessage("Game ID cannot be empty");
                    event.consume();
                    return;
                }
                try {
                    String id = gameIdInput.getText().toUpperCase();
                    player.setColor(proxy.joinGame(player, id));
                    gameId = id;

                    loadGame();
                    valid = true;
                } catch (GameException | PlayerException | RuntimeException e) {
                    ChessGameUI.displayPopupMessage(e.getMessage());
                    event.consume();
                }
            });
            createBtn.addEventFilter(ActionEvent.ACTION, event -> {
                try {
                    org.example.ui.figure.Color color = colorSwitch.getState() ? org.example.ui.figure.Color.BLACK : org.example.ui.figure.Color.WHITE;
                    player.setColor(color);

                    gameId = proxy.createGame(player, color);

                    loadGame();
                    valid = true;
                } catch (JsonException | RuntimeException e) {
                    ChessGameUI.displayPopupMessage(e.getMessage());
                    event.consume();
                }
            });

            ButtonType quitBtnType = new ButtonType("Quit", ButtonBar.ButtonData.CANCEL_CLOSE);
            getDialogPane().getButtonTypes().add(quitBtnType);
            Button quitBtn = (Button) getDialogPane().lookupButton(quitBtnType);
            quitBtn.addEventFilter(ActionEvent.ACTION, event -> {
                proxy.endConnection();
                System.exit(0);
            });

            setOnCloseRequest(e -> {
                if (valid) return;
                proxy.endConnection();
                System.exit(0);
            });
        }
    }

    public static void showGameConnectionPopup() {
        instance.new GameConnectionDialog().showAndWait();
    }

    private void loadGame() {
        JsonObject boardStanding = proxy.getBoard(gameId);

        BoardLoader.load(boardStanding, board);

        primaryStage.setTitle("Chess - " + player.getColor().getFancyName() + " - " + gameId);

        disableOptionButtons(false);
    }

    private Label newRowLabel(int i) {
        Label l = new Label(8 - i + "");
        l.setMinSize(20, 50);
        l.setAlignment(Pos.CENTER);
        return l;
    }

    private Label newColLabel(int i) {
        Label l = new Label((char) (i + 65) + "");
        l.setMinSize(50, 20);
        l.setAlignment(Pos.CENTER);
        return l;
    }

    public static void displayMessage(String text) {
        // Cut text at 40 characters
        if (text.length() > 30) {
            text = text.substring(0, 30) + "...";
        }
        instance.message.setText(text);
    }

    public static void displayPopupMessage(String text) {
        // Cut text at 40 characters
        if (text.length() > 30) {
            text = text.substring(0, 30) + "...";
        }
        instance.popupMessage.setText(text);
    }

    public static void resetGame() {
        getProxy().leaveGame(getPlayer(), getGameId());
        instance.gameStarted = false;
        instance.gameId = null;
        instance.board.clear();
        instance.disableOptionButtons(true);
        instance.primaryStage.setTitle("Chess");
        showGameConnectionPopup();
    }

    public static ChessBoard getBoard() {
        return instance.board;
    }

    public static ChessServerClientProxy getProxy() {
        return instance.proxy;
    }

    public static Player getPlayer() {
        return instance.player;
    }

    public static String getGameId() {
        return instance.gameId;
    }

    public static void startGame() {
        instance.gameStarted = true;
    }

    public static boolean gameStarted() {
        return instance.gameStarted;
    }

    public void disableOptionButtons(boolean disable) {
        optionButtons.forEach(button -> button.setDisable(disable));
    }
}
