package service.results;

import models.GameData;

import java.util.Collection;

public record ListGamesResult(Collection<GameData> games) {
}
