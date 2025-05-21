package dataaccess;

import model.UserData;

import javax.xml.crypto.Data;

public interface UserDAO {
    void createUser(UserData user) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;

    boolean authenticateUser(String username, String password);

    void createUser(String username, String password, String email);
    void clear();
}
