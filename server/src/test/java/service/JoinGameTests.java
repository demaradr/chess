package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import models.AuthData;
import models.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import request.JoinGameRequest;

import static org.junit.jupiter.api.Assertions.*;

public class JoinGameTests {
    private JoinGameService joinGameService;
    private GameDAO gameDAO;
    private AuthDAO authDAO;

    @BeforeEach
    public void setUp() throws Exception {
        gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();
        joinGameService = new JoinGameService(gameDAO, authDAO);

        AuthData auth = new AuthData("token", "user");
        authDAO.createAuth(auth);
        GameData game = new GameData(1, null, null, "Test Game", new chess.ChessGame());
        gameDAO.createGame(game);
    }
    @Test
    public void joinGamePositive() throws Exception {
        JoinGameRequest request = new JoinGameRequest("WHITE", 1);
        var result = joinGameService.joinGame(request, "token");

        assertNotNull(result);
        GameData updatedGame = gameDAO.getGame(1);
        assertEquals("user", updatedGame.whiteUsername());
    }

    @Test
    public void joinGameNegative() throws Exception {
        JoinGameRequest request1 = new JoinGameRequest("WHITE", 1);
        joinGameService.joinGame(request1, "token");

        JoinGameRequest request2 = new JoinGameRequest("WHITE", 1);
        assertThrows(ServiceException.class, () -> joinGameService.joinGame(request2, "token"));
    }
}
