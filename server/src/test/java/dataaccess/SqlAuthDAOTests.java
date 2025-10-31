package dataaccess;

import models.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

public class SqlAuthDAOTests {

    private MySqlAuthDAO authDAO;


    @BeforeEach
    public void setUp() throws DataAccessException {
        authDAO = new MySqlAuthDAO();
        authDAO.clear();

    }


    @Test
    public void createAuthPositive() throws DataAccessException {
        AuthData auth = new AuthData("guapo", "adriano");
        authDAO.createAuth(auth);

        AuthData fetchedAuth = authDAO.getAuth("guapo");
        assertNotNull(fetchedAuth);
        assertEquals("guapo", fetchedAuth.authToken());
        assertEquals("adriano", fetchedAuth.username());
    }

    @Test
    public void createAuthNegative() throws DataAccessException {
        AuthData auth = new AuthData("token", "usuario");
        AuthData auth2 = new AuthData("token", "u2");
        authDAO.createAuth(auth);
        assertThrows(DataAccessException.class, () -> authDAO.createAuth(auth2));
    }



    @Test
    public void getAuthPos() throws DataAccessException {
        AuthData auth = new AuthData("t", "chaval");
        authDAO.createAuth(auth);
        AuthData fetchedAuth = authDAO.getAuth("t");
        assertEquals("t", fetchedAuth.authToken());
        assertEquals("chaval", fetchedAuth.username());


    }

    @Test
    public void getAuthNeg() throws DataAccessException {
        AuthData auth = authDAO.getAuth("not token");
        assertNull(auth);

    }


    @Test
    public void deletePositive() throws DataAccessException {
        AuthData auth = new AuthData("token", "user");
        authDAO.createAuth(auth);
        authDAO.deleteAuth("token");
        assertNull(authDAO.getAuth("token"));
    }


    @Test
    public void deleteNegative() throws DataAccessException {
        assertDoesNotThrow(() -> authDAO.deleteAuth("token"));
    }


    @Test
    public void testClear() throws DataAccessException {
        AuthData auth = new AuthData("token", "usuario");
        AuthData auth2 = new AuthData("token2", "u2");
        authDAO.createAuth(auth);
        authDAO.createAuth(auth2);
        authDAO.clear();

        assertNull(authDAO.getAuth("token"));
        assertNull(authDAO.getAuth("token2"));
    }
}
