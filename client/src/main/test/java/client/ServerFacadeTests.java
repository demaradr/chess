package client;

import chess.ChessGame;
import dataaccess.*;
import model.*;
import request.*;
import response.*;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;
import server.Server;

import static org.junit.jupiter.api.Assertions.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ServerFacadeTests {

    private Server server;
    private ServerFacade facade;

    private AuthDAO authDAO;
    private GameDAO gameDAO;
    private UserDAO userDAO;

    @BeforeAll
    void startServer() throws DataAccessException {
        server = new Server();
        int port = server.run(0);
        facade = new ServerFacade("http://localhost:" + port);
        System.out.println("Started test HTTP server on port " + port);
    }

    @AfterAll
    void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clearDatabase() throws DataAccessException {
        authDAO = new MySQLAuthDAO();
        gameDAO = new MySQLGameDAO();
        userDAO = new MySQLUserDAO();
        authDAO.clear();
        userDAO.clear();
        gameDAO.clear();
    }

    @Test
    void registerValidUserSucceeds() {
        UserData user = new UserData("test_user", "pass", "test.com");
        assertDoesNotThrow(() -> facade.register(user));
    }

    @Test
    void registerDuplicateUserFails() {
        UserData user = new UserData("test_user", "pass", "test.com");
        assertDoesNotThrow(() -> facade.register(user));
        ResultException e = assertThrows(ResultException.class, () -> facade.register(user));
        assertEquals(403, e.statusCode());
    }

    @Test
    void loginValidCredentialsSucceeds() throws DataAccessException {
        String username = "test_user";
        String password = "pass";
        String email = "test.com";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        userDAO.createUser(new UserData(username, hashedPassword, email));

        LoginResponse result = assertDoesNotThrow(() -> facade.login(new UserData(username, password, email)));
        assertEquals(username, result.username());
    }

    @Test
    void loginInvalidCredentialsFails() throws DataAccessException {
        String username = "test_user";
        String password = "pass";
        String email = "test.com";
        String wrongPassword = password + "MM";

        userDAO.createUser(new UserData(username, BCrypt.hashpw(wrongPassword, BCrypt.gensalt()), email));

        ResultException e = assertThrows(ResultException.class, () -> facade.login(new UserData(username, password, email)));
        assertEquals(401, e.statusCode());
    }

    @Test
    void logoutValidAuthTokenSucceeds() throws DataAccessException {
        String token = "abc123";
        authDAO.createAuth(new AuthData(token, "test_user"));
        assertDoesNotThrow(() -> facade.logout(token));
    }

    @Test
    void logoutInvalidAuthTokenFails() {
        ResultException e = assertThrows(ResultException.class, () -> facade.logout("invalidToken"));
        assertEquals(401, e.statusCode());
    }

    @Test
    void listGamesValidAuthTokenSucceeds() throws DataAccessException {
        String token = "abc123";
        authDAO.createAuth(new AuthData(token, "test_user"));
        assertDoesNotThrow(() -> facade.listGames(new ListGamesRequest(token)));
    }

    @Test
    void listGamesInvalidAuthTokenFails() {
        ResultException e = assertThrows(ResultException.class, () -> facade.listGames(new ListGamesRequest("invalidToken")));
        assertEquals(401, e.statusCode());
    }

    @Test
    void createGameValidAuthTokenSucceeds() throws DataAccessException {
        String token = "abc123";
        authDAO.createAuth(new AuthData(token, "test_user"));
        assertDoesNotThrow(() -> facade.createGame(new CreateGameRequest(token, "Test Game")));
    }

    @Test
    void createGameInvalidAuthTokenFails() {
        ResultException e = assertThrows(ResultException.class, () -> facade.createGame(new CreateGameRequest("invalidToken", "Test Game")));
        assertEquals(401, e.statusCode());
    }

    @Test
    void joinGameValidAuthTokenSucceeds() throws DataAccessException {
        String token = "abc123";
        String username = "test_user";
        String gameName = "test_game";
        int gameID = 1;

        authDAO.createAuth(new AuthData(token, username));
        gameDAO.createGame(new AuthData(token, username), gameName);
        gameDAO.updateGame(new GameData(gameID, null, username, gameName, new ChessGame()));

        assertDoesNotThrow(() -> facade.joinGame(new JoinGameRequest(token, "WHITE", gameID)));
    }

    @Test
    void joinGameInvalidAuthTokenFails() {
        ResultException e = assertThrows(ResultException.class, () -> facade.joinGame(new JoinGameRequest("invalidToken", "WHITE", 1)));
        assertEquals(401, e.statusCode());
    }

    @Test
    void joinGameSpotTakenFails() throws DataAccessException {
        String token = "abc123";
        String username = "test_user";
        String gameName = "test_game";
        int gameID = 1;

        authDAO.createAuth(new AuthData(token, username));
        gameDAO.createGame(new AuthData(token, username), gameName);
        gameDAO.updateGame(new GameData(gameID, "test_user2", username, gameName, new ChessGame()));

        ResultException e = assertThrows(ResultException.class, () -> facade.joinGame(new JoinGameRequest(token, "WHITE", gameID)));
        assertEquals(403, e.statusCode());
    }
}
