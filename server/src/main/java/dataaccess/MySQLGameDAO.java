package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;

public class MySQLGameDAO implements GameDAO {

    @Override
    public HashSet<GameData> listGames() throws DataAccessException {
        HashSet<GameData> games = new HashSet<>(16);
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement("SELECT gameID, whiteUsername, blackUsername, gameName, chessGame FROM game")) {
                try (var results = statement.executeQuery()) {
                    while (results.next()) {
                        var gameID = results.getInt("gameID");
                        var whiteUsername = results.getString("whiteUsername");
                        var blackUsername = results.getString("blackUsername");
                        var gameName = results.getString("gameName");
                        var chessGame = deserializeGame(results.getString("chessGame"));
                        games.add(new GameData(gameID, whiteUsername, blackUsername, gameName, chessGame));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error listing games", e);
        }
        return games;
    }

    @Override
    public void createGame(GameData game) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement(
                    "INSERT INTO game (gameID, whiteUsername, blackUsername, gameName, chessGame) " +
                            "VALUES(?, ?, ?, ?, ?)")) {
                statement.setInt(1, game.gameID());
                statement.setString(2, game.whiteUsername());
                statement.setString(3, game.blackUsername());
                statement.setString(4, game.gameName());
                statement.setString(5, serializeGame(game.game()));
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error creating game: " + e.getMessage(), e);
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement("SELECT whiteUsername, blackUsername, gameName, chessGame FROM game WHERE gameID=?")) {
                statement.setInt(1, gameID);
                try (var results = statement.executeQuery()) {
                    if (results.next()) { // Added proper check for row existence
                        var whiteUsername = results.getString("whiteUsername");
                        var blackUsername = results.getString("blackUsername");
                        var gameName = results.getString("gameName");
                        var chessGame = deserializeGame(results.getString("chessGame"));
                        return new GameData(gameID, whiteUsername, blackUsername, gameName, chessGame);
                    } else {
                        throw new DataAccessException("Game not found, id: " + gameID);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving game: " + e.getMessage(), e);
        }
    }

    @Override
    public Collection<GameData> listGames(String username) throws DataAccessException {
        HashSet<GameData> games = new HashSet<>();
        try (var conn = DatabaseManager.getConnection()) {
            String sql = "SELECT gameID, whiteUsername, blackUsername, gameName, chessGame " +
                    "FROM game " +
                    "WHERE whiteUsername = ? OR blackUsername = ?";
            try (var statement = conn.prepareStatement(sql)) {
                statement.setString(1, username);
                statement.setString(2, username);
                try (var results = statement.executeQuery()) {
                    while (results.next()) {
                        int gameID = results.getInt("gameID");
                        String whiteUsername = results.getString("whiteUsername");
                        String blackUsername = results.getString("blackUsername");
                        String gameName = results.getString("gameName");
                        ChessGame chessGame = deserializeGame(results.getString("chessGame"));
                        games.add(new GameData(gameID, whiteUsername, blackUsername, gameName, chessGame));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching games for user " + username, e);
        }
        return games;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement("UPDATE game SET whiteUsername=?, blackUsername=?, gameName=?, chessGame=? WHERE gameID=?")) {
                statement.setString(1, game.whiteUsername());
                statement.setString(2, game.blackUsername());
                statement.setString(3, game.gameName());
                statement.setString(4, serializeGame(game.game()));
                statement.setInt(5, game.gameID());
                int rowsUpdated = statement.executeUpdate();
                if (rowsUpdated == 0) {
                    throw new DataAccessException("Item requested to be updated not found");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error updating game: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean gameExists(int gameID) {
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement("SELECT gameID FROM game WHERE gameID=?")) {
                statement.setInt(1, gameID);
                try (var results = statement.executeQuery()) {
                    return results.next();
                }
            }
        } catch (SQLException | DataAccessException e) {
            return false;
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("TRUNCATE TABLE game");
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing game table", e);
        }
    }

    private String serializeGame(ChessGame game) {
        return new Gson().toJson(game);
    }

    private ChessGame deserializeGame(String serializedGame) {
        return new Gson().fromJson(serializedGame, ChessGame.class);
    }
}
