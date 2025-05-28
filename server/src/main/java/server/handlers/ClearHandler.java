package server.handlers;

import spark.Request;
import spark.Response;
import spark.Route;
import dataaccess.*;

public class ClearHandler implements Route {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public ClearHandler(UserDAO userDAO, AuthDAO authDAO, GameDAO gameDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        userDAO.clear();
        authDAO.clear();
        gameDAO.clear();
        res.status(200);
        return "{}";
    }
}