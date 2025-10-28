package dataaccess;

import models.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.crypto.Data;

import static org.junit.jupiter.api.Assertions.*;

public class SqlUserDAOTests {

    private MySqlUserDAO userDAO;

    @BeforeEach
    public void setUp() throws DataAccessException {
        userDAO = new MySqlUserDAO();
        userDAO.clear();
    }

    @Test
    public void createUserPositive() throws DataAccessException {
        UserData user = new UserData("user", "password","test@gmail.com");
        userDAO.createUser(user);

        UserData fetchedUser = userDAO.getUser("user");
        assertNotNull(fetchedUser);
        assertEquals("user", fetchedUser.username());
        assertNotEquals("password", fetchedUser.password());
        assertEquals("test@gmail.com", fetchedUser.email());
    }

    @Test
    public void createUserNegative() throws DataAccessException {
        UserData user1 = new UserData("user", "password", "test@email.com");
        UserData user2 = new UserData("user", "password2", "test2@email.com");
        userDAO.createUser(user1);
        assertThrows(DataAccessException.class, () -> userDAO.createUser(user2));
    }

    @Test
    public void getUserPositive() throws DataAccessException {
        UserData user = new UserData("user", "pass", "test@gmail.com");
        userDAO.createUser(user);
        UserData fetchedUser = userDAO.getUser("user");
        assertNotNull(fetchedUser);
    }

    @Test
    public void getUserNegative() throws DataAccessException {
        UserData fetchedUser = userDAO.getUser("notuser");
        assertNull(fetchedUser);
    }


    @Test
    public void clearTest () throws DataAccessException {
        UserData user1 = new UserData("user", "pass","email@emial.com");
        UserData user2 = new UserData("user2", "pass2", "email2@gmail.com");
        userDAO.createUser(user1);
        userDAO.createUser(user2);

        assertNotNull(userDAO.getUser("user"));
        assertNotNull(userDAO.getUser("user2"));
        userDAO.clear();
        assertNull(userDAO.getUser("user"));
        assertNull(userDAO.getUser("user2"));
    }
}
