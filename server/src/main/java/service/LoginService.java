package service;

import dataaccess.*;
import request.LoginRequest;
import response.LoginResponse;
import org.mindrot.jbcrypt.BCrypt;

public class LoginService {
    UserDAO loginDAO;
    AuthDAO authDAO;

    public LoginService(UserDAO loginDAO, AuthDAO authDAO) {
        this.loginDAO = loginDAO;
        this.authDAO = authDAO;
    }

    public LoginResponse login(LoginRequest request) throws DataAccessException {
        if (request == null || request.username() == null || request.username().isEmpty()
                || request.password() == null || request.password().isEmpty()) {
            throw new BadRequestException("Error: bad request");
        }

        var user = loginDAO.getUser(request.username());
        if (user == null || !BCrypt.checkpw(request.password(), user.password())) {
            throw new UnauthorizedException("Error: unauthorized");
        }

        var auth = AuthDAO.generateAuth(request.username());
        authDAO.createAuth(auth);
        return new LoginResponse(auth.username(), auth.authToken());
    }

}