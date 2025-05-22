package dataaccess;

import model.GameData;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class MemoryGameDAO implements GameDAO {
    private final HashMap<Integer, GameData> games = new HashMap<>();

    @Override
    public void createGame(GameData game) throws DataAccessException {
        if (games.containsKey(game.gameID())) {
            throw new DataAccessException("Error game already exists with ID " + game.gameID());
        }
        games.put(game.gameID(), game);
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        GameData game = games.get(gameID);
        if (game == null) {
            throw new DataAccessException("Error game not found with ID " + gameID);
        }
        return game;
    }

    @Override
    public Collection<GameData> listGames(String username) throws DataAccessException {
        return List.of();
    }

    @Override
    public Collection<GameData> listGames() {
        return games.values();
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if (!games.containsKey(game.gameID())) {
            throw new DataAccessException("Error game ID " + game.gameID() + " does not exist");
        }
        games.put(game.gameID(), game);
    }

    @Override
    public void clear() {
        games.clear();
    }
}


