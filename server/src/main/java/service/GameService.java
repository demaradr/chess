package service;

import chess.ChessGame;
import dataaccess.*;
import model.*;

import java.util.HashSet;
import java.util.UUID;

public class GameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;
    private static int nextGameID = 1;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public GameData createGame(String authToken, String gameName) throws DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) throw new DataAccessException("Unauthorized");
        int gameID = nextGameID++;
        GameData game = new GameData(gameID, null, null, gameName, new ChessGame());
        gameDAO.createGame(game);
        return game;
    }

    public HashSet<GameData> listGames(String authToken) throws DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) throw new DataAccessException("Unauthorized");
        return (HashSet<GameData>) gameDAO.listGames(auth.username());
    }

    public void joinGame(String authToken, int gameID, String playerColor) throws DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) throw new DataAccessException("Unauthorized");

        GameData game = gameDAO.getGame(gameID);
        if (game == null) throw new DataAccessException("Game not found");

        if ("WHITE".equalsIgnoreCase(playerColor)) {
            if (game.whitePlayer() != null) throw new DataAccessException("White already taken");
            game = new GameData(game.gameID(), auth.username(), game.blackPlayer(), game.gameName(), game.game());
        } else if ("BLACK".equalsIgnoreCase(playerColor)) {
            if (game.blackPlayer() != null) throw new DataAccessException("Black already taken");
            game = new GameData(game.gameID(), game.whitePlayer(), auth.username(), game.gameName(), game.game());
        }

        gameDAO.createGame(game);
    }
}
