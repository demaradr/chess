package service;

import dataaccess.DataAccessException;
import dataaccess.AuthDAO;

import request.LogoutRequest;

public class LogoutService {
    private AuthDAO authDAO;

    public LogoutService(AuthDAO authDAO) {
        this.authDAO = authDAO;
    }

    public void logout(LogoutRequest request) throws DataAccessException {
        var authData = authDAO.authenticate(request.authToken());

        authDAO.deleteAuth(authData);
    }
}