package org.example.ui;

import com.google.gson.JsonObject;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Popup;
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

        getServerConnectionPopup().show(primaryStage);
    }

    private Popup getServerConnectionPopup() {
        final Popup popup = new Popup();
        BorderPane pane = new BorderPane();

        Text title = new Text("Server Connection");
        title.setStyle("-fx-font-weight: bold");
        title.setStyle("-fx-font-size: 20px");

        TextField ipInput = new TextField();
        ipInput.setText("localhost");
        Text ipLabel = new Text("IP Address");
//        ipInput.set();
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

        Button connectButton = getConnectButton(popup, ipInput, portInput);

        pane.setPadding(new Insets(5, 5, 5, 5));

        pane.setTop(title);
        pane.setCenter(table);
        pane.setBottom(connectButton);
        BorderPane.setAlignment(connectButton, Pos.CENTER);
        BorderPane.setAlignment(title, Pos.CENTER);
        BorderPane.setMargin(title, new Insets(0, 0, 5, 0));
        BorderPane.setMargin(connectButton, new Insets(5, 0, 0, 0));


        Rectangle bg = new Rectangle(235, 160, Color.WHITE);

        popup.getContent().addAll(bg, pane);

        return popup;
    }

    private Button getConnectButton(Popup popup, TextField ipInput, TextField portInput) {
        Button connectButton = new Button("Connect");
        connectButton.setOnAction(event -> {
            try {
                String ip = ipInput.getText();
                int port = Integer.parseInt(portInput.getText());
                Socket socket = new Socket(ip, port);
                // TODO: Save player id
                player = new Player(UUID.randomUUID().toString());
                proxy = new ChessServerClientProxy(socket);
                popup.hide();
                displayPopupMessage("");
                getGameConnectionPopup().show(this.primaryStage);
            } catch (NumberFormatException e) {
                ChessGameUI.displayPopupMessage("Invalid port");
            } catch (UnknownHostException e) {
                ChessGameUI.displayPopupMessage("Unknown host");
            } catch (IOException e) {
                ChessGameUI.displayPopupMessage("Connection failed");
            }
        });
        return connectButton;
    }

    private Popup getGameConnectionPopup() {
        final Popup popup = new Popup();
        Text title = new Text("Game Connection");
        title.setStyle("-fx-font-weight: bold");
        title.setStyle("-fx-font-size: 20px");


        BorderPane createPane = new BorderPane();

        ToggleSwitch colorSwitch = new ToggleSwitch();
        Text colorLabel = new Text("Color");
        Text whiteLabel = new Text("White");
        Text blackLabel = new Text("Black");
        Button createButton = getCreateButton(popup, colorSwitch);

        GridPane colorTable = new GridPane();
        colorTable.add(whiteLabel, 0, 0);
        colorTable.add(blackLabel, 2, 0);
        colorTable.add(colorSwitch, 1, 0);
        colorTable.setHgap(5);

        GridPane createTable = new GridPane();
        createTable.add(colorLabel, 0, 0);
        createTable.add(colorTable, 1, 0);
        createTable.setAlignment(Pos.CENTER);
        createTable.setHgap(5);

        createPane.setTop(createTable);
        createPane.setBottom(createButton);
        BorderPane.setAlignment(createButton, Pos.CENTER);
        BorderPane.setMargin(createButton, new Insets(5, 0, 0, 0));


        BorderPane joinPane = new BorderPane();

        TextField gameIdInput = new TextField();
        Text gameIdLabel = new Text("Game ID");
        Button joinButton = getJoinButton(popup, gameIdInput);

        GridPane joinTable = new GridPane();
        joinTable.add(gameIdLabel, 0, 2);
        joinTable.add(gameIdInput, 1, 2);
        joinTable.setAlignment(Pos.CENTER);
        joinTable.setHgap(5);

        joinPane.setTop(joinTable);
        joinPane.setBottom(joinButton);
        BorderPane.setAlignment(joinButton, Pos.CENTER);
        BorderPane.setMargin(joinButton, new Insets(5, 0, 0, 0));


        GridPane table = new GridPane();

        table.add(createPane, 0, 0);
        table.add(joinPane, 0, 1);
        table.setAlignment(Pos.CENTER);
        table.setVgap(5);


        BorderPane pane = new BorderPane();

        pane.setTop(title);
        pane.setCenter(table);
        pane.setBottom(popupMessage);
        BorderPane.setAlignment(title, Pos.CENTER);
        BorderPane.setAlignment(message, Pos.CENTER);
        BorderPane.setMargin(title, new Insets(0, 0, 5, 0));


        Rectangle bg = new Rectangle(235, 160, Color.WHITE);

        popup.getContent().addAll(bg, pane);
        popup.setWidth(235);
        popup.setHeight(160);

        return popup;
    }

    private Button getCreateButton(Popup popup, ToggleSwitch colorSwitch) {
        Button connectButton = new Button("Create");
        connectButton.setOnAction(event -> {
            try {
                org.example.ui.figure.Color color = colorSwitch.getState() ? org.example.ui.figure.Color.BLACK : org.example.ui.figure.Color.WHITE;
                player.setColor(color);

                gameId = proxy.createGame(player, color);

                loadGame();

                popup.hide();
            } catch (JsonException | RuntimeException e) {
                ChessGameUI.displayPopupMessage(e.getMessage());
            }
        });
        return connectButton;
    }

    private Button getJoinButton(Popup popup, TextField gameIdInput) {
        Button connectButton = new Button("Join");
        connectButton.setOnAction(event -> {
            try {
                String id = gameIdInput.getText();
                player.setColor(proxy.joinGame(player, id));
                gameId = id;

                loadGame();

                popup.hide();
            } catch (GameException | PlayerException | RuntimeException e) {
                ChessGameUI.displayPopupMessage(e.getMessage());
            }
        });
        return connectButton;
    }

    public static void showGameConnectionPopup() {
        instance.getGameConnectionPopup().show(instance.primaryStage);
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
        instance.getGameConnectionPopup().show(instance.primaryStage);
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
