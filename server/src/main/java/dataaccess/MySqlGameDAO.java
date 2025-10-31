package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import models.GameData;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

public class MySqlGameDAO implements GameDAO{

    private final Gson gson = new Gson();

    public MySqlGameDAO() throws DataAccessException {
        DatabaseManager.configureDatabase(createStatements);

    }

    @Override
    public void createGame(GameData game) throws DataAccessException {
        String sql = "INSERT INTO games (whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?)";
        String json = gson.toJson(game.game());
        DatabaseManager.updateData(sql, game.whiteUsername(), game.blackUsername(), game.gameName(), json);

    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM games WHERE gameID=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, gameID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readGame(rs);
                    }
                }
            }
        }
        catch (SQLException error) {
            throw new DataAccessException(error.getMessage());
        }
        return null;
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        Collection<GameData> listOfGames = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM games";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                try(ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {
                        listOfGames.add(readGame(rs));
                    }}
            }
        }


        catch (SQLException error) {
            throw new DataAccessException((error.getMessage()));
        }

        return listOfGames;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {

        String sql = "UPDATE games SET whiteUsername=?, blackUsername = ?, gameName= ?, game=? WHERE gameID = ?";
        String json = gson.toJson(game.game());

        DatabaseManager.updateData(sql, game.whiteUsername(), game.blackUsername(),game.gameName(), json, game.gameID());

    }

    @Override
    public void clear() throws DataAccessException {
        String sql = "TRUNCATE TABLE games";
        DatabaseManager.updateData(sql);

    }


    private GameData readGame(ResultSet rs) throws SQLException {
        int gameID = rs.getInt("gameID");
        String whiteUsername = rs.getString("whiteUsername");
        String blackUsername = rs.getString("blackUsername");
        String gameName = rs.getString("gameName");
        String json = rs.getString("game");

        ChessGame chessGame = gson.fromJson(json, ChessGame.class);
        return new GameData(gameID, whiteUsername, blackUsername, gameName, chessGame);



    }






    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS games (
                gameID INT NOT NULL AUTO_INCREMENT,
                whiteUsername VARCHAR(255),
                blackUsername VARCHAR(255),
                gameName VARCHAR(255) NOT NULL,
                game TEXT NOT NULL,
                PRIMARY KEY (gameID)
                )
            """
    };

}
