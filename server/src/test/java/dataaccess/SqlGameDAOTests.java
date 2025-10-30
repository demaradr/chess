package dataaccess;

import chess.ChessGame;
import models.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SqlGameDAOTests {

    private MySqlGameDAO gameDAO;

    @BeforeEach
    public void setUp() throws DataAccessException {
        gameDAO = new MySqlGameDAO();
        gameDAO.clear();
    }


    @Test
    public void createGamePositive() throws DataAccessException {
        ChessGame chessGame = new ChessGame();
        GameData game = new GameData(0, "whiteUser", "blackUser","test game", chessGame);

        assertEquals("whiteUser", game.whiteUsername());
        assertEquals("blackUser", game.blackUsername());
        assertEquals("test game", game.gameName());
        assertNotNull(game);;


    }

    @Test
    public void createGameNegative() throws DataAccessException {
        ChessGame chessGame = new ChessGame();
        GameData game = new GameData(0, "whiteUser", "blackUser",null, chessGame);
        assertThrows(DataAccessException.class, () -> gameDAO.createGame(game));

    }



    @Test
    public void getGamePositive() throws DataAccessException {
        ChessGame chessGame = new ChessGame();
        GameData game = new GameData(1, "white", "black", "test game", chessGame);
        gameDAO.createGame(game);

        GameData fetchedGame = gameDAO.getGame(game.gameID());
        assertEquals(game.gameID(), fetchedGame.gameID());
        assertEquals("white", fetchedGame.whiteUsername());
        assertEquals("black", fetchedGame.blackUsername());
        assertEquals("test game", fetchedGame.gameName());
        assertNotNull(fetchedGame.game());
    }

    @Test
    public void getGameNegative() throws DataAccessException {
        GameData fetdchedGame = gameDAO.getGame(21);
        assertNull(fetdchedGame);
    }
}
