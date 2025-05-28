package dataaccess;

import model.GameData;
import java.util.Collection;

public interface GameDAO {
    void createGame(GameData game) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    Collection<GameData> listGames(String username) throws DataAccessException;
    Collection<GameData> listGames() throws DataAccessException;
    void updateGame(GameData game) throws DataAccessException;
    boolean gameExists(int gameID);
    void clear() throws DataAccessException;
}
