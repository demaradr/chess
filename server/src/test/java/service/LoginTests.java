package service;
import dataaccess.*;
import models.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import request.LoginRequest;

import static org.junit.jupiter.api.Assertions.*;

public class LoginTests {

    private LoginService loginService;
    private UserDAO userDAO;
    private AuthDAO authDAO;

    @BeforeEach
    public void setUp() throws Exception {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        loginService = new LoginService(userDAO, authDAO);

        UserData user = new UserData("test", "pass", "test@email.com");
        userDAO.createUser(user);
    }

    @Test
    public void loginPositive() throws Exception {
        LoginRequest request = new LoginRequest("test", "pass");
        var result = loginService.login(request);

        assertEquals("test", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    public void loginNegative() throws Exception {
        LoginRequest request = new LoginRequest("test", "bad_pass");

        assertThrows(ServiceException.class, () -> loginService.login(request));
    }
}
