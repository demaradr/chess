package response;

import model.GameData;
import java.util.Set;

public class ListGamesResponse {
    public Set<GameData> games;

    public ListGamesResponse(Set<GameData> games) {
        this.games = games;
    }
}
