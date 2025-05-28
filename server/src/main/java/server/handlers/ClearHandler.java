package server.handlers;

import dataaccess.DataAccessException;
import dataaccess.*;
import spark.Request;
import spark.Response;
import spark.Route;

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
    public Object handle(Request req, Response res) {
        try {
            // Clear all database tables
            userDAO.clear();
            authDAO.clear();
            gameDAO.clear();

            res.status(200);
            return "{}"; // Success response
        } catch (DataAccessException e) {
            // Handle database connection error
            res.status(500);
            return "{\"error\": \"Internal Server Error: Database access failed\"}";
        }
    }
}