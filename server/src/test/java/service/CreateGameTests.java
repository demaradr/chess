package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import models.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.request.CreateGameRequest;

import static org.junit.jupiter.api.Assertions.*;

public class CreateGameTests {

    private CreateGameService createGameService;

    private GameDAO gameDAO;
    private AuthDAO authDAO;

    @BeforeEach
    public void setUp() throws Exception {

        gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();
        createGameService = new CreateGameService(gameDAO, authDAO);
        AuthData auth = new AuthData("token","user");
        authDAO.createAuth(auth);

    }


    @Test
    public void createGamePositive() throws Exception {
        CreateGameRequest request = new CreateGameRequest("Game Test");
        var result = createGameService.createGame(request, "token");
        assertEquals(0, result.gameID());
        assertNotNull(gameDAO.getGame(0));

    }

    @Test
    public void createGameNegative() throws Exception {
        CreateGameRequest request = new CreateGameRequest("Game Test");
        assertThrows(ServiceException.class, () -> createGameService.createGame(request,"wrong token"));

    }
}


