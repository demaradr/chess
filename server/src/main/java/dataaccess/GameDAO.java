package dataaccess;

import models.GameData;

import java.util.Collection;

public interface GameDAO {
    Collection<GameData> listGames() throws DataAccessException;
    void updateGame(GameData game) throws DataAccessException;
    void clear() throws DataAccessException;
}
