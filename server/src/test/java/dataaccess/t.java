//package dataaccess;
//
//import chess.ChessBoard;
//import chess.ChessGame;
//import com.google.gson.Gson;
//import dataaccess.*;
//import model.GameData;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.sql.SQLException;
//import java.util.HashSet;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class MySQLGameDAOTest {
//
//    GameDAO dao;
//
//    GameData defaultGameData;
//
//    @BeforeEach
//    void setUp() throws DataAccessException, SQLException {
//        DatabaseManager.createDatabase();
//        dao = new MySQLGameDAO();
//        try (var conn = DatabaseManager.getConnection()) {
//            try (var statement = conn.prepareStatement("TRUNCATE game")) {
//                statement.executeUpdate();
//            }
//        }
//        ChessGame defaultChessGame = new ChessGame();
//        ChessBoard board = new ChessBoard();
//        board.resetBoard();
//        defaultChessGame.setBoard(board);
//
//        defaultGameData = new GameData(1234, "white", "black", "gamename", defaultChessGame);
//    }
//
//    @AfterEach
//    void tearDown() throws SQLException, DataAccessException {
//        try (var conn = DatabaseManager.getConnection()) {
//            try (var statement = conn.prepareStatement("TRUNCATE game")) {
//                statement.executeUpdate();
//            }
//        }
//    }
//
//    @Test
//    void createGamePositive() throws DataAccessException, SQLException {
//        dao.createGame(defaultGameData);
//
//        GameData resultGameData;
//
//        try (var conn = DatabaseManager.getConnection()) {
//            try (var statement = conn.prepareStatement("SELECT gameID, whiteUsername, blackUsername, gameName, chessGame FROM game WHERE gameID=?")) {
//                statement.setInt(1, defaultGameData.gameID());
//                try (var results = statement.executeQuery()) {
//                    results.next();
//                    var gameID = results.getInt(("gameID"));
//                    var whiteUsername = results.getString("whiteUsername");
//                    var blackUsername = results.getString("blackUsername");
//                    var gameName = results.getString("gameName");
//                    var chessGame = deserializeGame(results.getString("chessGame"));
//                    assertEquals(defaultGameData.game(), chessGame, "Game board is not equal");
//                    resultGameData = new GameData(gameID, whiteUsername, blackUsername, gameName, chessGame);
//                }
//            }
//        }
//
//        assertEquals(defaultGameData, resultGameData);
//    }
//
//    @Test
//    void createGameNegative() throws DataAccessException {
//        dao.createGame(defaultGameData);
//        assertThrows(DataAccessException.class, () -> dao.createGame(defaultGameData));
//
//    }
//
//    @Test
//    void listGamesPositive() throws DataAccessException, SQLException {
//        dao.createGame(defaultGameData);
//        dao.createGame(new GameData(2345, "white", "black", "gamename", new ChessGame()));
//
//        HashSet<GameData> resultGames = (HashSet<GameData>) dao.listGames();
//
//        try (var conn = DatabaseManager.getConnection()) {
//            try (var statement = conn.prepareStatement("SELECT gameID, whiteUsername, blackUsername, gameName, chessGame FROM game")) {
//                try (var results = statement.executeQuery()) {
//                    int i = 0;
//                    while(results.next()) { i++; }
//                    assertEquals(i, resultGames.size(), "Improper game count in list");
//                }
//            }
//        }
//    }
//
//    @Test
//    void listGamesNegative() throws DataAccessException {
//        HashSet<GameData> games = (HashSet<GameData>) dao.listGames();
//        assertEquals(0, games.size(), "Expected an empty set from listGames()");
//    }
//
//    @Test
//    void getGamePositive() throws DataAccessException {
//        dao.createGame(defaultGameData);
//        assertEquals(defaultGameData, dao.getGame(defaultGameData.gameID()));
//    }
//
//    @Test
//    void getGameNegative() {
//        assertThrows(DataAccessException.class, () -> dao.getGame(defaultGameData.gameID()));
//    }
//
//    @Test
//    void gameExistsPositive() throws DataAccessException {
//        dao.createGame(defaultGameData);
//        assertTrue(dao.gameExists(defaultGameData.gameID()));
//    }
//
//    @Test
//    void gameExistsNegative() {
//        assertFalse(dao.gameExists(defaultGameData.gameID()));
//    }
//
//    @Test
//    void updateGamePositive() throws DataAccessException {
//        dao.createGame(defaultGameData);
//        GameData updatedGame = new GameData(defaultGameData.gameID(), "newWhite", "black", "gamename", defaultGameData.game());
//        dao.updateGame(updatedGame);
//
//        assertEquals(updatedGame, dao.getGame(defaultGameData.gameID()));
//    }
//
//    @Test
//    void updateGameNegative() {
//        assertThrows(DataAccessException.class, () -> dao.updateGame(defaultGameData));
//    }
//
//    @Test
//    void clear() throws DataAccessException, SQLException {
//        dao.createGame(defaultGameData);
//        dao.clear();
//
//        try (var conn = DatabaseManager.getConnection()) {
//            try (var statement = conn.prepareStatement("SELECT gameID FROM game WHERE gameID=?")) {
//                statement.setInt(1, defaultGameData.gameID());
//                try (var results = statement.executeQuery()) {
//                    assertFalse(results.next()); //There should be no elements
//                }
//            }
//        }
//    }
//
//    private ChessGame deserializeGame(String serializedGame) {
//        return new Gson().fromJson(serializedGame, ChessGame.class);
//    }
//}