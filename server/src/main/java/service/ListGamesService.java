package service;

import dataaccess.DataAccessException;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import request.ListGamesRequest;
import response.ListGamesResponse;

public class ListGamesService {
    AuthDAO authDAO;
    GameDAO gameDAO;

    public ListGamesService(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public ListGamesResponse listGames(ListGamesRequest request) throws DataAccessException {
        var auth = authDAO.authenticate(request.authToken());
        return new ListGamesResponse(gameDAO.listGames());
    }
}