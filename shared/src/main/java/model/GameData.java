package model;

import chess.*;

public record GameData(int gameID, String whitePlayer, String blackPlayer, String gameName, ChessGame game) {

}
