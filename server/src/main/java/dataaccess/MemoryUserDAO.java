package dataaccess;

import models.UserData;

import java.util.HashMap;
import java.util.Map;

public class MemoryUserDAO implements UserDAO {


    private final Map<String, UserData> users = new HashMap<>();

    @Override
    public void createUser(UserData u) throws DataAccessException {
        users.put(u.username(), u);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return users.get(username);
    }

    @Override
    public void clear() throws DataAccessException {
        users.clear();

    }
}
