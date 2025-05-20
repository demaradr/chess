package model;

import chess.*;

public record GameData(int gameID, String whiterPlayer, String blackPlayer, String gameName, ChessGame game) {
}
