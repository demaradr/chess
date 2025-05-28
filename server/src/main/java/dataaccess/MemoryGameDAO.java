package dataaccess;

import model.GameData;
import java.util.Collection;
import java.util.HashMap;

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
        return games.values().stream()
                .filter(g -> username.equals(g.whiteUsername()) || username.equals(g.blackUsername()))
                .toList();
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
    public boolean gameExists(int gameID) {
        return false;
    }

    @Override
    public void clear() {
        games.clear();
    }
}

