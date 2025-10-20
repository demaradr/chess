package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import models.AuthData;
import service.request.LogoutRequest;
import service.results.LogoutResult;

public class LogoutService {
    private final AuthDAO authDAO;
    public LogoutService(AuthDAO authDAO) {
        this.authDAO = authDAO;
    }


    public LogoutResult logout(LogoutRequest request) throws ServiceException {
        try {
            if (request == null || request.authToken() == null || request.authToken().isEmpty()) {
                throw new ServiceException("Error: unauthorized");
            }

            AuthData auth = authDAO.getAuth(request.authToken());
            if (auth == null) {
                throw new ServiceException("Error: unauthorized");
            }

            authDAO.deleteAuth(request.authToken());
            return new LogoutResult();

        } catch (DataAccessException error) {
            throw new ServiceException(("Eror: " + error.getMessage()));
    }}
}
