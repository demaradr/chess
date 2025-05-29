package service;

import dataaccess.BadRequestException;
import dataaccess.DataAccessException;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;

import request.CreateGameRequest;
import response.CreateGameResponse;

public class CreateGameService {
    AuthDAO authDAO;
    GameDAO gameDAO;

    public CreateGameService(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public CreateGameResponse createGame(CreateGameRequest request) throws DataAccessException {
        if (request == null || request.authToken() == null || request.gameName() == null) {
            throw new BadRequestException("Error: bad request");
        }

        var auth = authDAO.authenticate(request.authToken());
        int id = gameDAO.createGame(auth, request.gameName());
        return new CreateGameResponse(id);
    }
}