package service.results;

import models.GameData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public record ListGamesResult(List<GameData> games) {
    public ListGamesResult(Collection<GameData> games) {
        this(games == null ? new java.util.ArrayList<>() : new java.util.ArrayList<>(games));
    }
}
