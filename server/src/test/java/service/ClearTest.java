package service;

import dataaccess.*;
import models.AuthData;
import models.GameData;
import models.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClearTest {

    private UserDAO userDAO;
    private GameDAO gameDAO;
    private AuthDAO authDAO;
    private ClearService clearService;

    @BeforeEach
    public void setUp() {
        userDAO = new MemoryUserDAO();
        gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();
        clearService = new ClearService(userDAO, gameDAO, authDAO);
    }

    @Test
    public void clearTest() throws Exception {
        UserData user = new UserData("test", "pass", "test@gmail.com");
        userDAO.createUser(user);
        AuthData auth = new AuthData("token", "test");
        authDAO.createAuth(auth);
        GameData game = new GameData(1, "player1", "player2", "Test", new chess.ChessGame());
        gameDAO.createGame(game);

        clearService.clear();
        assertNull(userDAO.getUser("test"));
        assertNull(authDAO.getAuth("token"));
        assertNull(gameDAO.getGame(1));
        assertTrue(gameDAO.listGames().isEmpty());
}

}
