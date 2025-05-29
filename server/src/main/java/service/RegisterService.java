package service;

import dataaccess.BadRequestException;
import dataaccess.DataAccessException;
import dataaccess.TakenException;
import dataaccess.AuthDAO;
import dataaccess.UserDAO;

import model.UserData;
import request.RegisterRequest;
import response.RegisterResponse;
import org.mindrot.jbcrypt.BCrypt;

public class RegisterService {
    UserDAO userDAO;
    AuthDAO authDAO;

    public RegisterService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public RegisterResponse register(RegisterRequest request) throws DataAccessException {
        var userData = request.userData();
        if (userData == null || userData.email() == null || userData.password() == null || userData.username() == null) {
            throw new BadRequestException("Error: bad request");
        }

        if (userDAO.getUser(userData.username()) != null) {
            throw new TakenException("Error: already taken");
        }
        else {
            String hash = BCrypt.hashpw(userData.password(), BCrypt.gensalt());
            var hashedUser = new UserData(userData.username(), hash, userData.email());
            userDAO.createUser(hashedUser);

            var token = AuthDAO.generateAuth(userData.username());
            authDAO.createAuth(token);
            return new RegisterResponse(userData.username(), token.authToken());
        }

    }
}