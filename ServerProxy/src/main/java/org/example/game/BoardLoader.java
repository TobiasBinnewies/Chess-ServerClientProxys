package org.example.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.exceptions.JsonException;
import org.example.game.figure.Figure;
import org.example.game.figure.FigureFactory;

import java.io.InputStream;
import java.io.InputStreamReader;

public class BoardLoader {
    private final FigureFactory figureFactory;

    public BoardLoader(FigureFactory figureFactory) {
        this.figureFactory = figureFactory;
    }

    public void loadNewBoard(ChessBoard board) throws JsonException {
        try {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            InputStream is = classloader.getResourceAsStream("init.json");
            assert is != null;
            InputStreamReader isr = new InputStreamReader(is);
            JsonObject boardStanding = JsonParser.parseReader(isr).getAsJsonObject();

            loadFigures(Color.BLACK, boardStanding.getAsJsonArray("black"), board);
            loadFigures(Color.WHITE, boardStanding.getAsJsonArray("white"), board);
            board.setCurrenTurn(boardStanding.get("current_turn").getAsInt());
            board.set50MoveRuleTurns(boardStanding.get("50_move_rule_turns").getAsInt());
        } catch (Exception e) {
            throw new JsonException("Could not load board: " + e.getMessage());
        }
    }

    private void loadFigures(Color color, JsonArray jsonFigures, ChessBoard board) {
        for (JsonElement jsonFigure : jsonFigures) {
            String type = jsonFigure.getAsJsonObject().get("type").getAsString();
            String pos = jsonFigure.getAsJsonObject().get("pos").getAsString();
            int firstTurn = jsonFigure.getAsJsonObject().get("first_turn").getAsInt();
            Figure figure = figureFactory.createFigure(color, type, pos, firstTurn);
            board.setFigure(figure);
        }
    }

    public JsonObject getCurrentBoard(ChessBoard board) {
        JsonObject main = new JsonObject();
        main.addProperty("current_turn", board.getCurrentTurn());
        main.addProperty("50_move_rule_turns", board.get50MoveRuleTurns());
        JsonArray black = new JsonArray();
        JsonArray white = new JsonArray();
        for (Figure figure : board.getFigures()) {
            JsonObject jsonFigure = new JsonObject();
            jsonFigure.addProperty("type", figure.getName());
            jsonFigure.addProperty("pos", figure.getPos());
            jsonFigure.addProperty("first_turn", figure.getFirstTurn());
            if (figure.getColor() == Color.BLACK) {
                black.add(jsonFigure);
            } else {
                white.add(jsonFigure);
            }
        }
        main.add("black", black);
        main.add("white", white);
        return main;
    }
}
