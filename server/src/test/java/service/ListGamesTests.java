package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import models.AuthData;
import models.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ListGamesTests {

    private ListGamesService listGamesService;
    private GameDAO gameDAO;
    private AuthDAO authDAO;

    @BeforeEach
    public void setUp() throws Exception {
        gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();
        listGamesService = new ListGamesService(gameDAO, authDAO);

        GameData game = new GameData(0, null, null, "Test", new chess.ChessGame());
        gameDAO.createGame(game);
        authDAO.createAuth(new AuthData("token", "test"));
    }

    @Test
    public void listPositive() throws Exception {
        var result = listGamesService.listGames("token");


        assertNotNull(result);
        assertFalse(result.games().isEmpty());
    }

    @Test
    public void listNegative() {
        assertThrows(ServiceException.class, () -> listGamesService.listGames("bad"));
    }
}
