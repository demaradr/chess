package service;

import dataaccess.*;
import model.*;
import dataaccess.DataAccessException;

import java.util.UUID;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public AuthData register(UserData user) throws DataAccessException {
        if (userDAO.getUser(user.username()) != null) {
            throw new DataAccessException("User already exists");
        }
        userDAO.createUser(user.username(), user.password(), user.email());
        String token = UUID.randomUUID().toString();
        authDAO.createAuth(user.username(), token);
        return new AuthData(token, user.username());
    }

    public AuthData login(String username, String password) throws DataAccessException {
        if (!userDAO.authenticateUser(username, password)) {
            throw new DataAccessException("Invalid username or password");
        }
        String token = UUID.randomUUID().toString();
        authDAO.createAuth(username, token);
        return new AuthData(token, username);
    }

    public void logout(String token) throws DataAccessException {
        var auth = authDAO.getAuth(token);
        if (auth == null) {
            throw new DataAccessException("Invalid auth token");
        }
        authDAO.deleteAuth(token);
    }


    public void clear() throws DataAccessException {
        userDAO.clear();
        authDAO.clear();
    }
}