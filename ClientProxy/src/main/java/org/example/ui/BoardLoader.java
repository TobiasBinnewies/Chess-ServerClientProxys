package org.example.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.example.ui.figure.Color;
import org.example.ui.figure.Figure;
import org.example.ui.figure.FigureFactory;

public class BoardLoader {
    public static void load(JsonObject boardStanding, ChessBoard board) {
        board.clear();
        loadFigures(Color.BLACK, boardStanding.getAsJsonArray("black"), board);
        loadFigures(Color.WHITE, boardStanding.getAsJsonArray("white"), board);
        board.setCurrenTurn(boardStanding.get("current_turn").getAsInt());
    }

    private static void loadFigures(Color color, JsonArray jsonFigures, ChessBoard board) {
        for (JsonElement jsonFigure : jsonFigures) {
            String type = jsonFigure.getAsJsonObject().get("type").getAsString();
            String pos = jsonFigure.getAsJsonObject().get("pos").getAsString();
            int firstTurn = jsonFigure.getAsJsonObject().get("first_turn").getAsInt();
            Figure figure = FigureFactory.createFigure(color, type, pos, firstTurn);
            board.setFigure(figure);
        }
    }
}
