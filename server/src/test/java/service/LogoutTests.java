package service;
import dataaccess.AuthDAO;
import dataaccess.MemoryAuthDAO;
import models.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import request.LogoutRequest;

import static org.junit.jupiter.api.Assertions.*;

public class LogoutTests {

    private LogoutService logoutService;
    private AuthDAO authDAO;

    @BeforeEach
    public void setUp() throws Exception {
        authDAO = new MemoryAuthDAO();
        logoutService = new LogoutService(authDAO);
        AuthData auth = new AuthData("token", "test");
        authDAO.createAuth(auth);
    }

    @Test
    public void logoutPositive() throws Exception {
        LogoutRequest request = new LogoutRequest("token");
        var result = logoutService.logout(request);

        assertNotNull(result);
        assertNull(authDAO.getAuth("token"));
    }

    @Test
    public void logoutNegative() throws Exception {
        LogoutRequest request = new LogoutRequest("nottoken");
        assertThrows(ServiceException.class, () -> logoutService.logout(request));

    }
}

