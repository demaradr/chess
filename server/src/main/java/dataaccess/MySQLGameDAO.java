package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

public class MySQLGameDAO implements GameDAO {
    Connection connection;

    String tableVal;

    Gson gson;

    public MySQLGameDAO(Connection connection) {
        this.connection = connection;
        gson = new Gson();

        tableVal = DatabaseManager.TABLES[DatabaseManager.TableName.Games.ordinal()];
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        ArrayList<GameData> games = new ArrayList<>();

        String sql = "select id, whiteUsername, blackUsername, gameName, chessGame from games;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            var res = stmt.executeQuery();
            while (res.next()) {
                int id = res.getInt(1);
                String whiteUsername = res.getString(2);
                String blackUsername = res.getString(3);
                String gameName = res.getString(4);
                String gameJson = res.getString(5);
                ChessGame game = gson.fromJson(gameJson, ChessGame.class);
                games.add(new GameData(id, whiteUsername, blackUsername, gameName, game));
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }

        return games;
    }

    @Override
    public int createGame(AuthData auth, String gameName) throws DataAccessException {
        String sql = "INSERT INTO games (whiteUsername, blackUsername, gameName, chessGame) VALUES (?, ?, ?, ?);";
        int id = 0;
        try (PreparedStatement stmt = connection.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, null);
            stmt.setString(2, null);
            stmt.setString(3, gameName);
            stmt.setString(4, gson.toJson(new ChessGame()));
            if (stmt.executeUpdate() == 1) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    generatedKeys.next();
                    id = generatedKeys.getInt(1);
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException(ex.getMessage());
        }

        return id;
    }

    @Override
    public GameData getGame(AuthData data, int id) throws DataAccessException {
        GameData gameData = null;

        String sql = "select whiteUsername, blackUsername, gameName, chessGame from " + tableVal + " where id = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            var res = stmt.executeQuery();
            if (res.next()) {
                String whiteUsername = res.getString(1);
                String blackUsername = res.getString(2);
                String gameName = res.getString(3);
                String gameJson = res.getString(4);
                ChessGame game = gson.fromJson(gameJson, ChessGame.class);
                gameData = new GameData(id, whiteUsername, blackUsername, gameName, game);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }

        if (gameData == null) {
            throw new DataAccessException("Error: Game with ID " + id + " doesn't exist");
        }
        return gameData;
    }

    @Override
    public void updateGame(GameData data) throws DataAccessException {
        String sql = "update " + tableVal + " set whiteUsername = ?, blackUsername = ?, gameName = ?, chessGame = ? " + "where id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, data.whiteUsername());
            stmt.setString(2, data.blackUsername());
            stmt.setString(3, data.gameName());
            stmt.setString(4, gson.toJson(data.game()));
            stmt.setInt(5, data.gameID());
            if (stmt.executeUpdate() != 1) {
                throw new DataAccessException("Error: Game with ID " + data.gameID() + " doesn't exist");
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        ClearHelper.clearDB(tableVal, connection);
    }
}