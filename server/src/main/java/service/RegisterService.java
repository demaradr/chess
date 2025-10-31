package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import models.AuthData;
import models.UserData;
import service.request.RegisterRequest;
import service.results.RegisterResult;

public class RegisterService {

    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public RegisterService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }


    public RegisterResult register(RegisterRequest request) throws ServiceException {
        try {
            if (request == null || request.username() == null || request.password() == null || request.email() == null) {
                throw new ServiceException("Error: bad request");
            }

            if (request.username().isEmpty() || request.password().isEmpty() || request.email().isEmpty()) {
                throw new ServiceException("Error: bad request");
            }

            UserData currUser = userDAO.getUser(request.username());
            if (currUser != null) {

                throw new ServiceException("Error: already exists");

            }

            UserData user = new UserData(request.username(), request.password(), request.email());

            userDAO.createUser(user);
            String authToken = AuthDAO.generateToken();
            AuthData auth = new AuthData(authToken, request.username());
            authDAO.createAuth(auth);

            return new RegisterResult(request.username(), authToken);

        } catch (DataAccessException error) {
            throw new ServiceException("Error: " + error.getMessage());
        }
    }
}
