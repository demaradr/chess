package dataaccess;

import model.AuthData;

public interface AuthDAO {

    void addAuth(String authToken, String username);
    void addAuth(AuthData authData);

    void deleteAuth(String authToken);

    AuthData getAuth(String authToken) throws
            DataAccessException;
    void clear();
}
