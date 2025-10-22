package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import models.AuthData;
import models.GameData;
import service.request.JoinGameRequest;
import service.results.JoinGameResult;

public class JoinGameService {


    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public JoinGameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public JoinGameResult joinGame(JoinGameRequest request, String authToken) throws ServiceException {
        try {
            if (authToken == null || authToken.isEmpty()) {
                throw new ServiceException("Error: unauthorized");
            }

            AuthData auth = authDAO.getAuth(authToken);
            if (auth == null) {
                throw new ServiceException("Error: unauthorized");
            }

            String username = auth.username();

            if (request == null || request.playerColor() == null || request.gameID() <= 0) {
                throw new ServiceException("Error: bad request");
            }

            if (!request.playerColor().equals("WHITE") && !request.playerColor().equals("BLACK")) {
                throw new ServiceException("Error: bad request");
            }

            GameData game = gameDAO.getGame(request.gameID());
            if (game == null) {
                throw new ServiceException("Error: bad request");
            }

            if (request.playerColor().equals("WHITE") && game.whiteUsername() != null) {
                throw new ServiceException("Error: already taken");
            }
            if (request.playerColor().equals("BLACK") && game.blackUsername() != null) {
                throw new ServiceException("Error: already taken");
            }

            String whiteUsername = request.playerColor().equals("WHITE") ? username : game.whiteUsername();
            String blackUsername = request.playerColor().equals("BLACK") ? username : game.blackUsername();

            GameData updatedGame = new GameData(
                    game.gameID(),
                    whiteUsername,
                    blackUsername,
                    game.gameName(),
                    game.game()
            );

            gameDAO.updateGame(updatedGame);
            return new JoinGameResult();

        } catch (DataAccessException e) {
            throw new ServiceException("Error: " + e.getMessage());
        }
    }
}