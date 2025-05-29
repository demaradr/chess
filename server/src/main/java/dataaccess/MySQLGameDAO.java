package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

public class MySQLGameDAO implements GameDAO {

    private final String tableVal = DatabaseManager.TABLES[DatabaseManager.TableName.Games.ordinal()];
    private final Gson gson = new Gson();

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        Collection<GameData> games = new ArrayList<>();
        String sql = "SELECT id, whiteUsername, blackUsername, gameName, chessGame FROM " + tableVal + ";";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet res = stmt.executeQuery()) {
            while (res.next()) {
                int id = res.getInt(1);
                String whiteUsername = res.getString(2);
                String blackUsername = res.getString(3);
                String gameName = res.getString(4);
                ChessGame game = gson.fromJson(res.getString(5), ChessGame.class);
                games.add(new GameData(id, whiteUsername, blackUsername, gameName, game));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error" + e.getMessage());
        }
        return games;
    }

    @Override
    public int createGame(AuthData auth, String gameName) throws DataAccessException {
        String sql = "INSERT INTO " + tableVal + " (whiteUsername, blackUsername, gameName, chessGame) VALUES (?, ?, ?, ?);";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, null);
            stmt.setString(2, null);
            stmt.setString(3, gameName);
            stmt.setString(4, gson.toJson(new ChessGame()));
            if (stmt.executeUpdate() == 1) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
            throw new DataAccessException("Error: failed to create game");
        } catch (SQLException ex) {
            throw new DataAccessException("Error" + ex.getMessage());
        }
    }

    @Override
    public GameData getGame(AuthData data, int id) throws DataAccessException {
        String sql = "SELECT whiteUsername, blackUsername, gameName, chessGame FROM " + tableVal + " WHERE id = ?;";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet res = stmt.executeQuery()) {
                if (res.next()) {
                    String whiteUsername = res.getString(1);
                    String blackUsername = res.getString(2);
                    String gameName = res.getString(3);
                    ChessGame game = gson.fromJson(res.getString(4), ChessGame.class);
                    return new GameData(id, whiteUsername, blackUsername, gameName, game);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error" + e.getMessage());
        }
        throw new DataAccessException("Error: Game with ID " + id + " doesn't exist");
    }

    @Override
    public void updateGame(GameData data) throws DataAccessException {
        String sql = "UPDATE " + tableVal + " SET whiteUsername = ?, blackUsername = ?, gameName = ?, chessGame = ? WHERE id = ?;";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, data.whiteUsername());
            stmt.setString(2, data.blackUsername());
            stmt.setString(3, data.gameName());
            stmt.setString(4, gson.toJson(data.game()));
            stmt.setInt(5, data.gameID());
            if (stmt.executeUpdate() != 1) {
                throw new DataAccessException("Error Game with ID " + data.gameID() + " doesn't exist");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error" + e.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (Connection connection = DatabaseManager.getConnection()) {
            ClearHelper.clearDB(tableVal, connection);
        } catch (SQLException e) {
            throw new RuntimeException("Error " + e + ".");
        }
    }
}
