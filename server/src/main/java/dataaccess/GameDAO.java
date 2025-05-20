package dataaccess;

import model.GameData;
import chess.*;
import java.util.HashSet;

public interface GameDAO {
    HashSet<GameData> listGames(String username);
    void createGame(int gameID, String whitePlayer, String blackPlayer, String gameName, ChessGame game);
    void createGame(GameData game);
    GameData getGame(int gameID) throws
            DataAccessException;
    void clear();
}
