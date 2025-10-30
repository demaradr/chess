package dataaccess;

import chess.ChessGame;
import models.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

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


    @Test
    public void listGamesPositive() throws DataAccessException {
        ChessGame chessGame = new ChessGame();
        ChessGame chessGame2 = new ChessGame();
        GameData game = new GameData(0, "white", "black", "test game", chessGame);
        GameData game2 = new GameData(1, "white2", "black2", "test game2", chessGame2);
        gameDAO.createGame(game);
        gameDAO.createGame(game2);

        Collection<GameData> games = gameDAO.listGames();
        assertEquals(2, games.size());
    }

    @Test
    public void listGamesNegative() throws DataAccessException {
        gameDAO.clear();

        Collection<GameData> games = gameDAO.listGames();
        assertNotEquals(2, games.size());

    }


    @Test
    public void updateGamePositive() throws DataAccessException {
        ChessGame chessGame = new ChessGame();
        GameData game = new GameData(1, "white", "black", "test game", chessGame);
        gameDAO.createGame(game);

        ChessGame updatedGame = new ChessGame();
        updatedGame.setTeamTurn(ChessGame.TeamColor.BLACK);
        GameData updatedGame2 = new GameData(1, "john", "aitana", "test game", updatedGame);
        gameDAO.updateGame(updatedGame2);
        GameData fetchedGame = gameDAO.getGame(game.gameID());
        assertEquals("john", fetchedGame.whiteUsername());
        assertEquals("aitana", fetchedGame.blackUsername());



    }


    @Test
    public void updateGameNegative() throws DataAccessException {
        ChessGame chessGame = new ChessGame();
        GameData game = new GameData(213, "white", "black","game", chessGame);
        assertDoesNotThrow(() -> gameDAO.updateGame(game));

    }

}
