package org.example.Proxy;

import org.example.exceptions.GameException;
import org.example.exceptions.IllegalMoveException;
import org.example.exceptions.JsonException;
import org.example.exceptions.PlayerException;
import org.example.game.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChessServerTest {

    private ChessServer chessServer;
    private IPlayer mockPlayerWhite;
    private IPlayer mockPlayerBlack;

    @BeforeEach
    void setUp() {
        chessServer = new ChessServer();
        mockPlayerWhite = Mockito.mock(IPlayer.class);
        mockPlayerBlack = Mockito.mock(IPlayer.class);
    }

    @Test
    void createGame_shouldCreateGameAndReturnGameId() throws JsonException, GameException {
        // Arrange
        when(mockPlayerWhite.currentGame()).thenReturn(null);

        // Act
        String gameId = chessServer.createGame(mockPlayerWhite, Color.WHITE);

        // Assert
        assertNotNull(gameId);
        assertEquals(6, gameId.length());
        assertNotNull(chessServer.getGame(gameId));
        verify(mockPlayerWhite).setCurrentGame(any(ChessGame.class));
    }

    @Test
    void joinGame_shouldJoinExistingGameAndReturnColor() throws GameException, PlayerException, JsonException {
        // Arrange
        String gameId = chessServer.createGame(mockPlayerWhite, Color.WHITE);

        // Act
        Color color = chessServer.joinGame(mockPlayerBlack, gameId);

        // Assert
        assertNotNull(color);
        verify(mockPlayerBlack).setCurrentGame(any(ChessGame.class));
    }

    @Test
    void leaveGame_shouldLeaveGameAndRemoveIfEmpty() throws GameException, JsonException {
        // Arrange
        String gameId = chessServer.createGame(mockPlayerWhite, Color.WHITE);

        // Act
        chessServer.leaveGame(mockPlayerWhite, gameId);

        // Assert
        assertThrows(GameException.class, () -> chessServer.getGame(gameId));
    }

    @Test
    void movePiece_shouldMovePieceAndThrowIllegalMoveExceptionIfMoveIsNotPossible() throws GameException, PlayerException, IllegalMoveException, JsonException {
        // Arrange
        String gameId = chessServer.createGame(mockPlayerWhite, Color.WHITE);

        chessServer.joinGame(mockPlayerBlack, gameId);

        ChessGame game = chessServer.getGame(gameId);

        // Assert
        chessServer.movePiece(mockPlayerWhite, gameId, 2, 6, 2, 5, "");

        assertThrows(IllegalMoveException.class, () -> chessServer.movePiece(mockPlayerBlack, gameId, 2, 1, 2, 6, ""));
    }

    @Test
    void getBoard_shouldReturnBoard() throws GameException, JsonException {
        // Arrange
        String gameId = chessServer.createGame(mockPlayerWhite, Color.WHITE);

        // Act
        assertNotNull(chessServer.getBoard(gameId));
    }
}

