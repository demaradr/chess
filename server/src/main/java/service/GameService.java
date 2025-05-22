package service;

import chess.ChessGame;
import dataaccess.*;
import model.*;
import response.ListGamesResponse;
import java.util.Collection;
import java.util.HashSet;


public class GameService {
    public GameDAO gameDAO;
    public AuthDAO authDAO;
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

    public ListGamesResponse listGames(String authToken) throws DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Unauthorized");
        }

        Collection<GameData> games = gameDAO.listGames();
        return new ListGamesResponse(new HashSet<>(games));
    }


    public void joinGame(String authToken, int gameID, String playerColor) throws DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Unauthorized");
        }

        GameData oldGame = gameDAO.getGame(gameID);
        if (oldGame == null) {
            throw new DataAccessException("Game not found");
        }

        String username = auth.username();

        GameData updatedGame;
        if ("WHITE".equalsIgnoreCase(playerColor)) {
            if (oldGame.whiteUsername() != null) {
                throw new DataAccessException("White player already assigned");
            }
            updatedGame = new GameData(oldGame.gameID(), username, oldGame.blackUsername(), oldGame.gameName(), oldGame.game());
        } else if ("BLACK".equalsIgnoreCase(playerColor)) {
            if (oldGame.blackUsername() != null) {
                throw new DataAccessException("Black player already assigned");
            }
            updatedGame = new GameData(oldGame.gameID(), oldGame.whiteUsername(), username, oldGame.gameName(), oldGame.game());
        } else {
            throw new DataAccessException("Invalid player color");
        }



        gameDAO.updateGame(updatedGame);

    }


}
