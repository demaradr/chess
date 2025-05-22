package dataaccess;

import model.AuthData;
import java.util.HashMap;

public class MemoryAuthDAO implements AuthDAO {
    private final HashMap<String, AuthData> authTokens = new HashMap<>();

    @Override
    public void createAuth(AuthData auth) {
        authTokens.put(auth.authToken(), auth);
    }

    @Override
    public AuthData getAuth(String authToken) {
        return authTokens.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) {
        authTokens.remove(authToken);
    }

    @Override
    public void createAuth(String username, String token) {
        createAuth(new AuthData(token, username));
    }

    @Override
    public void clear() {
        authTokens.clear();
    }
}