package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import models.GameData;
import results.ListGamesResult;

import java.util.Collection;

public class ListGamesService {

    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public ListGamesService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;


    }

    public ListGamesResult listGames(String authToken) throws ServiceException {
        try {
            if (authToken == null || authToken.isEmpty()) {

                throw new ServiceException("Error: unauthorized");
            }


            var auth = authDAO.getAuth(authToken);
            if (auth == null) {
                throw new ServiceException("Error: unauthorized");
            }
            Collection<GameData> games = gameDAO.listGames();
            return new ListGamesResult(games);
        }
        catch (DataAccessException error) {

            throw new ServiceException("Error: " + error.getMessage());
        }}
}
