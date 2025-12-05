package client;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import client.websocket.NotificationHandler;
import client.websocket.WebSocketFacade;
import com.google.gson.Gson;
import exception.ResponseException;
import models.GameData;
import results.CreateGameResult;
import results.ListGamesResult;
import results.LoginResult;
import results.RegisterResult;
import server.ServerFacade;
import ui.DrawChessBoard;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static java.lang.Integer.parseInt;
import static ui.EscapeSequences.*;

public class ChessClient implements NotificationHandler {

    private State state = State.LOGGED_OUT;
    private final ServerFacade server;
    private final String serverURL;
    private String authToken;
    private String username;
    private List<GameData> createdGames = new ArrayList<>();
    private WebSocketFacade ws;
    private final Gson gson = new Gson();
    private ChessGame currentGame;
    private ChessGame.TeamColor playerColor;
    private Integer currentGameID;



    public ChessClient(String serverURL) {
        this.server = new ServerFacade(serverURL);
        this.serverURL = serverURL;
    }

    public void run() {
        System.out.println("\nHello there! Welcome to 240 Chess ♕ \nType help to start :)\n");

        Scanner scanner = new Scanner(System.in);
        var result = "";

        while (!result.equals("quit")) {
            printPrompt();



            try {
                String line = scanner.nextLine();
                result = eval(line);
                System.out.print(result);
            }
            catch (Throwable error) {
                var message = error.getMessage();
                System.out.print(message);
            }
        }
    }

    @Override
    public void notify(ServerMessage message) {
        switch(message.getServerMessageType()) {
            case LOAD_GAME -> {
                LoadGameMessage load = (LoadGameMessage) message;
                String json = gson.toJson(load.getGame());
                ChessGame game = gson.fromJson(json, ChessGame.class);

                updateGame(game);
            }

            case NOTIFICATION -> {
                NotificationMessage noti = (NotificationMessage) message;
                System.out.println(noti.getNotification());
            }

            case ERROR -> {
                ErrorMessage error = (ErrorMessage) message;
                System.out.println(error.getError());
            }
        }
    }




    enum State {
        LOGGED_OUT,
        LOGGED_IN,
        IN_GAME
    }

    public String help() {
        if (state == State.LOGGED_OUT) {
            return "to register type:"+ SET_TEXT_COLOR_BLUE + " register <USERNAME> <PASSWORD> <EMAIL>" + RESET_TEXT_COLOR + "\n" +
                    "to login type:"+ SET_TEXT_COLOR_BLUE + " login <USERNAME> <PASSWORD>" + RESET_TEXT_COLOR + "\n" +
                    "to exit chess type:"+ SET_TEXT_COLOR_BLUE + " quit" + RESET_TEXT_COLOR + "\n";
        }
        else if (state == State.IN_GAME){
            return "to redraw board:"+ SET_TEXT_COLOR_BLUE + " redraw" + RESET_TEXT_COLOR + "\n" +
                    "to leave the game:" + SET_TEXT_COLOR_BLUE + " leave" + RESET_TEXT_COLOR + "\n" +
                    "to make a move:" + SET_TEXT_COLOR_BLUE + " move <START_POS> <END_POS>" + RESET_TEXT_COLOR + "\n" +
                    "to resign the game:"+ SET_TEXT_COLOR_BLUE + " resign" + RESET_TEXT_COLOR + "\n" +
                    "to highlight legal moves:"+ SET_TEXT_COLOR_BLUE + " highlight <PIECE_POS>" + RESET_TEXT_COLOR + "\n";
        }
        return  "to create a game type:"+ SET_TEXT_COLOR_BLUE + " create <NAME>" + RESET_TEXT_COLOR + "\n" +
                "to see all games type:" + SET_TEXT_COLOR_BLUE + " list" + RESET_TEXT_COLOR + "\n" +
                "to join a game type:" + SET_TEXT_COLOR_BLUE + " join <GAMEID> [WHITE|BLACK]" + RESET_TEXT_COLOR + "\n" +
                "to watch a game type:"+ SET_TEXT_COLOR_BLUE + " observe <GAMEID>" + RESET_TEXT_COLOR + "\n" +
                "to logout type:"+ SET_TEXT_COLOR_BLUE + " logout" + RESET_TEXT_COLOR + "\n" +
                "to exit chess type:"+ SET_TEXT_COLOR_BLUE + " quit" + RESET_TEXT_COLOR + "\n";
    }


    private void printPrompt() {
        System.out.print("[" + state + "] >>> ");
    }

    public String eval(String input) {

        try {
            String[] tokens = input.toLowerCase().split(" ");
            String command = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (command) {
                case "help" -> help();
                case "register" -> register(params);
                case "login" -> login(params);
                case "logout" -> logout();
                case "create" -> createGame(params);
                case "list" -> listGames();
                case "join" -> join(params);
                case "observe" -> observe(params);
                case "redraw" -> redraw();
                case "leave" -> leave();
                case "resign" -> resign();
                case "move" -> makeMove(params);
                case "highlight" -> highlight(params);
                case "quit" -> "quit";
                default -> throw new IllegalStateException("Unexpected input: " + command +"\n");
            };

        } catch (ResponseException exep) {
            return printError(exep);
        }
    }


    private String register(String... params) throws ResponseException {
        if (state != State.LOGGED_OUT) {
            return SET_TEXT_COLOR_RED + "You are logged in. Logout to register \n" + RESET_TEXT_COLOR;

        }
        if (params.length < 3) {
            return SET_TEXT_COLOR_RED + "Not enough arguments! Type: register <USERNAME> <PASSWORD> <EMAIL> \n" + RESET_TEXT_COLOR;
        }

        if (params.length > 3) {
            return SET_TEXT_COLOR_RED + "Too many arguments! Type: register <USERNAME> <PASSWORD> <EMAIL> \n" + RESET_TEXT_COLOR;
        }
        RegisterResult result = server.register(params[0], params[1], params[2]);
        this.authToken = result.authToken();
        this.username = result.username();
        this.state = State.LOGGED_IN;
        return SET_TEXT_COLOR_GREEN + "Registered and logged in as " + username +"!\n" + RESET_TEXT_COLOR;
    }


    private String login(String... params) throws ResponseException {
        if (state == State.LOGGED_IN) {
            return SET_TEXT_COLOR_RED + "You're already logged in!\n"+ RESET_TEXT_COLOR;

        }

        if (params.length < 2) {
            return SET_TEXT_COLOR_RED + "Not enough arguments! Type: login <USERNAME> <PASSWORD>\n"+ RESET_TEXT_COLOR;

        }
        if (params.length > 2) {
            return SET_TEXT_COLOR_RED + "Too many arguments! Type: login <USERNAME> <PASSWORD>\n"+ RESET_TEXT_COLOR;

        }



        LoginResult result = server.login(params[0], params[1]);
        this.authToken = result.authToken();
        this.username = result.username();
        this.state = State.LOGGED_IN;
        return SET_TEXT_COLOR_GREEN + "Logged in as " + username + "!\n" + RESET_TEXT_COLOR;



    }


    private String logout() throws ResponseException {
        if (state == State.LOGGED_OUT) {
            return SET_TEXT_COLOR_RED + "You're not logged in!\n"+ RESET_TEXT_COLOR;

        }

        server.logout();
        this.authToken = null;
        this.username = null;
        this.state = State.LOGGED_OUT;


        return SET_TEXT_COLOR_GREEN + "You logged out! \n" + RESET_TEXT_COLOR;
    }

    private String createGame(String... params) throws ResponseException {
        if (state == State.LOGGED_OUT) {
            return SET_TEXT_COLOR_RED + "You're not logged in!\n"+ RESET_TEXT_COLOR;

        }
        if (params.length < 1) {
            return SET_TEXT_COLOR_RED + "Not enough arguments! Type: create <GAME_NAME>\n"+ RESET_TEXT_COLOR;

        }
        if (params.length > 1) {
            return SET_TEXT_COLOR_RED + "Too many arguments! Type: create <GAME_NAME>\n"+ RESET_TEXT_COLOR;

        }

        String game = String.join(" ", params);
        server.createGame(game);
        return SET_TEXT_COLOR_GREEN + "Created game " + game + "!\n" + RESET_TEXT_COLOR;


    }

    private String listGames() throws ResponseException {
        if (state == State.LOGGED_OUT) {
            return SET_TEXT_COLOR_RED + "You're not logged in!\n"+ RESET_TEXT_COLOR;
        }
        ListGamesResult allGames = server.listGames();
        this.createdGames = allGames.games();

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < createdGames.size(); i++) {
            GameData game = createdGames.get(i);
            result.append(i + 1);
            result.append(" ");
            result.append(game.gameName());
            result.append(" - White: ");
            result.append(game.whiteUsername());
            result.append(", Black: ");
            result.append(game.blackUsername());
            result.append("\n");

        }
        return result.toString();


    }


    private String join(String... params) throws ResponseException{
        if (state == State.LOGGED_OUT) {
            return SET_TEXT_COLOR_RED + "You're not logged in!\n"+ RESET_TEXT_COLOR;
        }

        if (params.length < 2) {
            return SET_TEXT_COLOR_RED + "Not enough arguments! Type: join <NUMBER> <WHITE|BLACK>\n"+ RESET_TEXT_COLOR;
        }
        if (params.length > 2) {
            return SET_TEXT_COLOR_RED + "Too many arguments! Type: join <NUMBER> <WHITE|BLACK>\n"+ RESET_TEXT_COLOR;
        }

        int gameNum;

        try {
            gameNum = parseInt(params[0]);

        }
        catch (NumberFormatException ex) {
            return SET_TEXT_COLOR_RED + "Please enter a number\n"+ RESET_TEXT_COLOR;

        }

        if (gameNum < 1 || gameNum > createdGames.size()) {
            return SET_TEXT_COLOR_RED + "Invalid game number! List games to see valid numbers\n"+ RESET_TEXT_COLOR;
        }

        String color = params[1].toUpperCase();
        if (!color.equals("WHITE") && !color.equals("BLACK")) {
            return SET_TEXT_COLOR_RED + "Color must be WHITE or BLACK\n"+ RESET_TEXT_COLOR;

        }

        GameData game = createdGames.get(gameNum -1);
        server.joinGame(game.gameID(), color);
        ChessGame.TeamColor userColor = color.equals("WHITE") ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
        ws = new WebSocketFacade(serverURL, this);
        UserGameCommand connect = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, game.gameID());
        ws.send(connect);


        this.currentGameID = game.gameID();
        this.playerColor = userColor;
        this.state = State.IN_GAME;
        this.currentGame = new ChessGame();

        return SET_TEXT_COLOR_GREEN + "Joined " + game.gameName() + " as " + color + "!\n" + RESET_TEXT_COLOR;


    }



    private String observe(String... params) throws ResponseException{
        if (state == State.LOGGED_OUT) {
            return SET_TEXT_COLOR_RED + "You're not logged in!\n"+ RESET_TEXT_COLOR;
        }

        if (params.length < 1) {
            return SET_TEXT_COLOR_RED + "Not enough arguments! Type: observe <NUMBER>\n"+ RESET_TEXT_COLOR;

        }

        if (params.length > 1) {
            return SET_TEXT_COLOR_RED + "Too many arguments! Type: observe <NUMBER>\n"+ RESET_TEXT_COLOR;

        }

        int gameNum;

        try {
            gameNum = parseInt(params[0]);

        }
        catch (NumberFormatException ex) {
            return SET_TEXT_COLOR_RED + "Please enter a valid number\n"+ RESET_TEXT_COLOR;

        }

        if (gameNum < 1 || gameNum > createdGames.size()) {
            return SET_TEXT_COLOR_RED + "Invalid game number! List games to see valid numbers\n"+ RESET_TEXT_COLOR;
        }



        GameData game = createdGames.get(gameNum -1);
        ws = new WebSocketFacade(serverURL, this);
        UserGameCommand connect = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, game.gameID());
        ws.send(connect);

        this.playerColor = null;
        this.currentGameID = game.gameID();
        this.state = State.IN_GAME;
        this.currentGame = new ChessGame();

        return SET_TEXT_COLOR_GREEN + "Observing " + game.gameName() +  "!\n" + RESET_TEXT_COLOR;

    }


    private void updateGame(ChessGame game) {
        this.currentGame = game;
        redraw();
    }



    private String redraw() {
        if (state != State.IN_GAME) {
            return SET_TEXT_COLOR_RED + "You're not in a game!\n" + RESET_TEXT_COLOR;

        }
        ChessGame.TeamColor viewColor = (playerColor != null) ? playerColor : ChessGame.TeamColor.WHITE;
        DrawChessBoard.drawBoard(currentGame, viewColor);
        return "";
    }

    private String leave() {
        if (state != State.IN_GAME) {
            return SET_TEXT_COLOR_RED + "You're not in a game! \n" + RESET_TEXT_COLOR;
        }

        try {
            UserGameCommand leave = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, currentGameID);
            ws.send(leave);
            ws.close();
            state = State.LOGGED_IN;
            ws = null;
            currentGame = null;
            playerColor = null;
            return SET_TEXT_COLOR_GREEN + "You left the game!\n" + RESET_TEXT_COLOR;


        }

        catch (Exception ex) {
            return SET_TEXT_COLOR_RED + "Error leaving the game: " + ex.getMessage() + "\n" + RESET_TEXT_COLOR;
        }
    }


    private String resign() {
        if (state != State.IN_GAME) {
            return SET_TEXT_COLOR_RED + "You're not in a game! \n" + RESET_TEXT_COLOR;
        }

        if (playerColor == null) {
            return SET_TEXT_COLOR_RED + "Observers can't resign! \n" + RESET_TEXT_COLOR;

        }
        try {
            UserGameCommand resign = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, currentGameID);
            ws.send(resign);
            return SET_TEXT_COLOR_GREEN + "You resigned!! \n" + RESET_TEXT_COLOR;

        }
        catch (Exception ex) {
            return SET_TEXT_COLOR_RED + "Error: " + ex.getMessage() + "\n" + RESET_TEXT_COLOR;

        }
    }


    private String makeMove(String... params) {
        if (state != State.IN_GAME) {
            return SET_TEXT_COLOR_RED + "You're not in a game! \n" + RESET_TEXT_COLOR;
        }

        if (playerColor == null) {
            return SET_TEXT_COLOR_RED + "Observers can't make moves! \n" + RESET_TEXT_COLOR;

        }

        if (params.length < 2) {
            return SET_TEXT_COLOR_RED + "Wrong format! Try: move <START_POS> <END_POS> \n" + RESET_TEXT_COLOR;

        }

        try {
            ChessPosition startPos = getPosition(params[0]);
            ChessPosition endPos = getPosition(params[1]);

            if (startPos == null || endPos == null) {
                return SET_TEXT_COLOR_RED + "Wrong format! Example: move a2 a3 \n" + RESET_TEXT_COLOR;

            }

            ChessMove move = new ChessMove(startPos, endPos, null);
            String moveDesc = params[0].toLowerCase() + "to" + params[1].toLowerCase();
            MakeMoveCommand moveCommand = new MakeMoveCommand(authToken, currentGameID, move, null);
            ws.send(moveCommand);
            return SET_TEXT_COLOR_GREEN +"Move was sent!" + RESET_TEXT_COLOR;


        } catch (Exception e) {
            return SET_TEXT_COLOR_RED + "Error: " + e.getMessage() +  "\n" + RESET_TEXT_COLOR;
        }
    }

    private String highlight(String... params) {
        if (state != State.IN_GAME) {
            return SET_TEXT_COLOR_RED + "You're not in a game!\n" + RESET_TEXT_COLOR;

        }
        if (params.length < 1) {
            return SET_TEXT_COLOR_RED + "Wrong format! Try: highlight <POSITION> \n" + RESET_TEXT_COLOR;

        }

        try {
            ChessPosition piecePos = getPosition(params[0]);
            var validMoves = currentGame.validMoves(piecePos);
            if (validMoves == null) {
                return SET_TEXT_COLOR_RED + "No valid moves for piece at " +piecePos + " \n" + RESET_TEXT_COLOR;

            }

            var highlightedPos = new ArrayList<ChessPosition>();
            highlightedPos.add(piecePos);
            for (ChessMove move : validMoves) {
                highlightedPos.add(move.getEndPosition());
            }

            ChessGame.TeamColor color = (playerColor != null) ? playerColor : ChessGame.TeamColor.WHITE;
            DrawChessBoard.drawBoard(currentGame, color, highlightedPos);

            return "";
        }
        catch (Exception ex) {
            return SET_TEXT_COLOR_RED + "Error: " + ex.getMessage() +  "\n" + RESET_TEXT_COLOR;
        }

    }

    private ChessPosition getPosition(String param) {
        if (param == null) {
            return null;
        }
        param = param.toLowerCase().trim();

        char colLetter = param.charAt(0);
        if (colLetter < 'a' || colLetter > 'h') {
            return null;
        }


        int col = colLetter - 'a'+ 1;

        try {
            int row = Integer.parseInt(param.substring(1));
            if (row < 1 || row > 8) {
                return null;
            }
            return new ChessPosition(row, col);


        }
        catch (NumberFormatException e){
            return null;

        }
    }


    private String parseErrors(String message) {

        if (message == null) {
            return "An error occurred";
        }

        if (message.toLowerCase().contains("bad request")) {
            return "Invalid input. Try again\n";
        }

        if (message.toLowerCase().contains("unauthorized")) {
            return "Not authorized. Try logging in again\n";
        }

        if (message.toLowerCase().contains("already exists")) {
            return "That username already exits. Choose a different one\n";
        }

        if (message.toLowerCase().contains("already taken")) {
            return "Color already taken! Try again with a different color or game\n";
        }

        return message;
    }

    private String printError(ResponseException exce) {
        String message = exce.getMessage();
        if(message == null) {
            message = "Request failed. Try again\n";
        }
        message = parseErrors(message);
        return SET_TEXT_COLOR_RED + message + RESET_TEXT_COLOR;
    }



}
