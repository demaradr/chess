package service;

import chess.ChessGame;
import dataaccess.*;
import model.*;

import java.util.concurrent.ThreadLocalRandom;

public class GameService {
    public GameDAO gameDAO;
    public AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public int createGame(String authToken, String gameName) throws DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Unauthorized");
        }

        int gameID;
        do {
            gameID = ThreadLocalRandom.current().nextInt(1, 10000);
        } while (gameDAO.gameExists(gameID));

        ChessGame chessGame = new ChessGame(); // Add this line
        gameDAO.createGame(new GameData(gameID, null, null, gameName, chessGame)); // Pass a new ChessGame

        return gameID;
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

        String color = playerColor == null ? "" : playerColor.trim().toUpperCase();
        if ("WHITE".equals(color)) {
            if (oldGame.whiteUsername() != null) {
                throw new DataAccessException("White player already assigned");
            }
            updatedGame = new GameData(oldGame.gameID(), username, oldGame.blackUsername(), oldGame.gameName(), oldGame.game());
        } else if ("BLACK".equals(color)) {
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
