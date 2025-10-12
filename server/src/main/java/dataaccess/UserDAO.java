package dataaccess;

import models.UserData;

public interface UserDAO {


    void createUser(UserData u) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;
    void clear() throws DataAccessException;
}
