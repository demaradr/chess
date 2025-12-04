package websocket.commands;

import chess.ChessMove;

import java.util.Objects;

public class MakeMoveCommand extends UserGameCommand{

    private final ChessMove move;
    private final String moveDescription;

    public MakeMoveCommand(String authToken, Integer gameID, ChessMove move, String moveDescription) {
        super(CommandType.MAKE_MOVE, authToken, gameID);
        this.move = move;
        this.moveDescription = moveDescription;

    }


    public ChessMove getMove() {
        return move;
    }


    public String getMoveDescription() {
        return moveDescription;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        MakeMoveCommand that = (MakeMoveCommand) o;
        return Objects.equals(move, that.move) && Objects.equals(moveDescription, that.moveDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), move, moveDescription);
    }
}
