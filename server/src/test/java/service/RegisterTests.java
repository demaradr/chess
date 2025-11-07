package service;

import dataaccess.AuthDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.UserDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import request.RegisterRequest;

import static org.junit.jupiter.api.Assertions.*;

public class RegisterTests {


    private RegisterService registerService;

    @BeforeEach
    public void setUp() {
        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        registerService = new RegisterService(userDAO, authDAO);
    }

    @Test
    public void registerPositive() throws Exception {
        RegisterRequest request = new RegisterRequest("test", "pass", "test@email.com");
        var result = registerService.register(request);
        assertEquals("test", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    public void registerNegative() throws Exception {
        RegisterRequest request1 = new RegisterRequest("test", "pass", "test@email.com");
        registerService.register(request1);

        RegisterRequest request2 = new RegisterRequest("test", "pass2", "test2@email.com");
        assertThrows(ServiceException.class, () -> registerService.register(request2));
    }
}
