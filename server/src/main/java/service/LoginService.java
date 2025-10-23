package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import models.AuthData;
import models.UserData;
import service.request.LoginRequest;
import service.results.LoginResult;

public class LoginService {

    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public LoginService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }


    public LoginResult login(LoginRequest request) throws ServiceException {
        try {
            if (request == null || request.username() == null || request.password() == null) {
                throw new ServiceException("Error: bad request");
            }

            if (request.username().isEmpty() || request.password().isEmpty()) {
                throw new ServiceException("Error: bad request");
            }
            UserData user = userDAO.getUser(request.username());
            if (user == null) {
                throw new ServiceException("Error: unauthorized");
            }
            if (!user.password().equals(request.password())) {
                throw new ServiceException("Error: unauthorized");
            }
            String authToken = AuthDAO.generateToken();
            AuthData auth = new AuthData(authToken, request.username());
            authDAO.createAuth(auth);

            return new LoginResult(request.username(), authToken);

        }
        catch (DataAccessException e) {
            throw new ServiceException("Error: " + e.getMessage());
        }
        }
    }

