package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

class MySQLGameDAOTest {
    Connection connection;
    MySQLGameDAO gameDAO;
    Gson gson;

    GameData[] testGames = {
            new GameData(1, "wht", "blk", "test1", new ChessGame()),
            new GameData(2, "wht", "blk", "test2", new ChessGame()),
            new GameData(3, "wht", "blk", "test3", new ChessGame()),
    };

    @BeforeEach
    void setup() throws DataAccessException, SQLException {

        DatabaseManager.createDatabase();
        connection = DatabaseManager.getConnection();
        gson = new Gson();

        gameDAO = new MySQLGameDAO();
    }

    @AfterEach
    void cleanup() throws DataAccessException {
        clearTestGames();
    }

    private void addGames() throws DataAccessException {
        clearTestGames();
        String sql = "INSERT INTO games (whiteUsername, blackUsername, gameName, chessGame) VALUES (?, ?, ?, ?);";
        for (int i = 0; i < testGames.length; i++) {
            var game = testGames[i];
            try (PreparedStatement stmt = connection.prepareStatement(sql,
                    Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, game.whiteUsername());
                stmt.setString(2, game.blackUsername());
                stmt.setString(3, game.gameName());
                stmt.setString(4, gson.toJson(game.game()));
                if (stmt.executeUpdate() == 1) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        generatedKeys.next();
                        int id = generatedKeys.getInt(1); // ID of the inserted book
                        testGames[i] = new GameData(id, game.whiteUsername(), game.blackUsername(), game.gameName(), game.game());
                    }
                }
                stmt.executeUpdate();
            } catch (SQLException ex) {
                throw new DataAccessException(ex.getMessage());
            }
        }
    }

    private void clearTestGames() throws DataAccessException {
        String sql = "truncate table games;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }

    }

    private boolean attemptFindGame(int id) throws DataAccessException {

        boolean returnVal;
        String sql = "select id, whiteUsername, blackUsername, gameName, chessGame from games where id = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            returnVal = stmt.executeQuery().next();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
        return returnVal;
    }

    private int getCount() throws DataAccessException {
        int count = 0;
        String sql = "select id, whiteUsername, blackUsername, gameName, chessGame from games;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            var res = stmt.executeQuery();
            while (res.next()) {
                count++;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
        return count;
    }


    @Test
    void listGamesValid() throws DataAccessException {
        clearTestGames();
        addGames();
        int expectedCount = getCount();

        var gamesList = gameDAO.listGames();

        Assertions.assertEquals(expectedCount, gamesList.size());
    }

    @Test
    void listGamesInvalid() throws DataAccessException {
        clearTestGames();
        var gamesList = gameDAO.listGames();

        Assertions.assertEquals(0, gamesList.size());
    }

    @Test
    void createGameValid() throws DataAccessException {
        int expectedCount = getCount() + 1;
        int testId = gameDAO.createGame(new AuthData("abc123", "nightblood"), "test");

        assertTrue(attemptFindGame(testId));
        assertEquals(expectedCount, getCount());
    }

    @Test
    void createGameInvalid() {
        var auth = new AuthData("abc123", "nightblood");
        assertThrows(DataAccessException.class, () -> gameDAO.createGame(auth, null));
    }

    @Test
    void getGameValid() throws DataAccessException {
        clearTestGames();
        addGames();
        var auth = new AuthData("abc123", "nightblood");

        var testId = testGames[0].gameID();
        assertEquals(testGames[0], gameDAO.getGame(auth, testId));

    }

    @Test
    void getGameInvalid() throws DataAccessException {
        clearTestGames();
        addGames();
        var testId = testGames[0].gameID();
        var auth = new AuthData("abc123", "nightblood");
        clearTestGames();

        assertThrows(DataAccessException.class, () -> gameDAO.getGame(auth, testId));
    }

    @Test
    void updateGameValid() throws DataAccessException {
        clearTestGames();
        addGames();

        var newGame = testGames[0];
        assertDoesNotThrow(() -> gameDAO.updateGame(newGame));
    }

    @Test
    void updateGameInvalid() throws DataAccessException {
        clearTestGames();
        addGames();
        var newGame = testGames[0];
        clearTestGames();

        assertThrows(DataAccessException.class, () -> gameDAO.updateGame(newGame));
    }

    @Test
    void clearValid() throws DataAccessException {
        gameDAO.clear();
        assertEquals(0, getCount());
    }

}