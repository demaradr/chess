package dataaccess;

import models.AuthData;
import java.util.UUID;

public interface AuthDAO {
    void createAuth(AuthData auth) throws DataAccessException;
    AuthData getAuth(String authToken) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;
    void clear() throws DataAccessException;

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }
}
