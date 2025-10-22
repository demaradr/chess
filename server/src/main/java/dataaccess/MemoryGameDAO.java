package dataaccess;

import models.GameData;

import java.util.*;

public class MemoryGameDAO implements GameDAO{

    private final Map<Integer, GameData> games = new HashMap<>();
    private int nextGameID = 1;

    @Override
    public void createGame(GameData game) throws DataAccessException {
        int gameID = game.gameID();
        if (gameID <= 0) {
            gameID = nextGameID++;
        }
        GameData newGame = new GameData(gameID, game.whiteUsername(), game.blackUsername(), game.gameName(), game.game());
        games.put(gameID, newGame);

    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return games.get(gameID);
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return new ArrayDeque<>(games.values());
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        games.put(game.gameID(), game);

    }

    @Override
    public void clear() throws DataAccessException {
        games.clear();
    }
}
