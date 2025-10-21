package service;

import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.MemoryGameDAO;
import models.AuthData;
import models.GameData;
import service.request.CreateGameRequest;
import service.results.CreateGameResult;

public class CreateGameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public CreateGameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }



    public CreateGameResult createGame(CreateGameRequest request, String authToken) throws ServiceException {
        try {
            if (authToken == null || authToken.isEmpty()) {
                throw new ServiceException("Error: unauthorized");
            }

            AuthData auth = authDAO.getAuth(authToken);
            if (auth == null) {
                throw new ServiceException("Error: unauthorized");
            }

            if (request == null || request.gameName() == null) {
                throw new ServiceException("Error: bad request");
            }

            ChessGame newGame = new ChessGame();
            GameData gameData = new GameData(0, null, null, request.gameName(), newGame);

            gameDAO.createGame(gameData);
            int gameID = gameData.gameID();
            return new CreateGameResult(gameID);

        } catch (DataAccessException e) {
            throw new ServiceException("Error: " + e.getMessage());
        }
    }
}
