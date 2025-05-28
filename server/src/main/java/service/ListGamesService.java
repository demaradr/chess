package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import request.ListGamesRequest;
import response.ListGamesResponse;
import model.GameData;

import java.util.Collection;

public class ListGamesService {
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public ListGamesService(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public ListGamesResponse listGames(ListGamesRequest request) throws DataAccessException {
        var auth = authDAO.getAuth(request.authToken());
        if (auth == null) {
            throw new DataAccessException("Unauthorized");
        }
        Collection<GameData> games = gameDAO.listGames(auth.username());
        return new ListGamesResponse(games);
    }
}
