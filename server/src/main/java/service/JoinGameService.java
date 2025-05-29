package service;

import dataaccess.BadRequestException;
import dataaccess.DataAccessException;
import dataaccess.TakenException;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;

import model.GameData;
import request.JoinGameRequest;

import java.util.HashSet;
import java.util.List;

public class JoinGameService {
    final HashSet<String> validTeams = new HashSet<>(List.of(
            "WHITE",
            "BLACK"
    ));

    private AuthDAO authDAO;
    private GameDAO gameDAO;

    public JoinGameService(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public void joinGame(JoinGameRequest request) throws DataAccessException {
        var team = request.playerColor();
        if (request.authToken() == null || team == null || request.gameID() == 0) {
            throw new BadRequestException("Error: bad request");
        }

        team = team.toUpperCase();
        if (!validTeams.contains(team)) {
            throw new BadRequestException("Error: bad request");
        }

        var auth = authDAO.authenticate(request.authToken());

        var oldGame = gameDAO.getGame(auth, request.gameID());
        var blkUser = oldGame.blackUsername();
        var whtUser = oldGame.whiteUsername();
        GameData newGame;

        if ("WHITE".equals(team)) {
            if (whtUser != null) {
                throw new TakenException("Error: already taken");
            }
            newGame = new GameData(oldGame.gameID(), auth.username(), blkUser, oldGame.gameName(), oldGame.game());
        }
        else {
            if (blkUser != null) {
                throw new TakenException("Error: already taken");
            }
            newGame = new GameData(oldGame.gameID(), whtUser, auth.username(), oldGame.gameName(), oldGame.game());
        }

        gameDAO.updateGame(newGame);
    }
}