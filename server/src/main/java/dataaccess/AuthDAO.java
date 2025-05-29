package dataaccess;

import model.AuthData;
import java.util.UUID;

public interface AuthDAO {
    void createAuth(AuthData data) throws DataAccessException;
    AuthData authenticate(String authToken) throws DataAccessException;
    void deleteAuth(AuthData data) throws DataAccessException;
    void clear() throws DataAccessException;
    public static AuthData generateAuth(String username) {
        return new AuthData(UUID.randomUUID().toString(), username);
    }
}