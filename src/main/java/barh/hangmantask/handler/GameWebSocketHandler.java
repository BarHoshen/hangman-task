package barh.hangmantask.handler;

import barh.hangmantask.entity.Game;
import barh.hangmantask.model.GameMessage;
import barh.hangmantask.model.GameResponse;
import barh.hangmantask.service.GameService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class GameWebSocketHandler extends TextWebSocketHandler {


    private final GameService gameService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, WebSocketSession> sessionMap = new HashMap<>();


    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessionMap.put(session.getId(), session);
        System.out.println("Connected: " + session.getId());
    }
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionMap.remove(session.getId());
        System.out.println("Disconnected: " + session.getId());
    }
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        System.out.println("Received: " + message.getPayload());
        GameResponse response;
        try {
            GameMessage gameMessage = objectMapper.readValue(message.getPayload(), GameMessage.class);
            response = switch (gameMessage.getAction()) {
                case "NEW_SINGLE_PLAYER" -> createSinglePlayerGame(session, gameMessage);
                case "NEW_DUEL" -> createDuel(session, gameMessage);
                case "JOIN_GAME" -> joinGame(session, gameMessage);
                case "MAKE_GUESS" -> processGuess(session, gameMessage);
                default -> new GameResponse("UNKNOWN", "Unrecognized action.", "unknown", -1); //session.sendMessage(new TextMessage("Unknown action: " + gameMessage.getAction()));
            };
        } catch (Exception e) {
            response = new GameResponse("ERROR", "Error parsing message.", "unknown", -1);
        }
        String jsonResponse = objectMapper.writeValueAsString(response);
        session.sendMessage(new TextMessage(jsonResponse));
    }


    private GameResponse createSinglePlayerGame(WebSocketSession session, GameMessage gameMessage) throws IOException {
        Game game = gameService.createSinglePlayerGame();
        return new GameResponse("GAME_CREATED", game.getId(), "IN_PROGRESS", game.getAttemptsLeftPlayerOne());
        //session.sendMessage(new TextMessage("GAME_CREATED: " + gameService.createSinglePlayerGame().getId()));
    }
    private GameResponse createDuel(WebSocketSession session, GameMessage gameMessage) throws IOException {
        Game game = gameService.createDuel(gameMessage.getWord());
        return new GameResponse("GAME_CREATED", game.getInviteToken(), "WAITING_FOR_PLAYER", 0);
        //session.sendMessage(new TextMessage("GAME_CREATED: " + game.getInviteToken()));
    }

    private GameResponse joinGame(WebSocketSession session, GameMessage gameMessage) throws IOException {
        Game game = gameService.joinDuel(gameMessage.getInviteToken(), gameMessage.getWord());
        return new GameResponse("GAME_JOINED", game.getId(), "IN_PROGRESS", game.getAttemptsLeftPlayerTwo());
        //session.sendMessage(new TextMessage("GAME_JOINED: " + gameService.joinDuel(gameMessage.getInviteToken(), gameMessage.getWord()).getId()));
    }

    private GameResponse processGuess(WebSocketSession session, GameMessage gameMessage) throws IOException {
        Game game = gameService.makeGuess(gameMessage.getGameId(), gameMessage.getGuess().charAt(0), gameMessage.isPlayerOne());
        return new GameResponse("GAME_UPDATED", game.getId(), game.isGameFinished() ? "FINISHED" : "IN_PROGRESS", gameMessage.isPlayerOne() ? game.getAttemptsLeftPlayerTwo() : game.getAttemptsLeftPlayerOne());
        //session.sendMessage(new TextMessage("GAME_UPDATED: " + gameService.makeGuess(gameMessage.getGameId(), gameMessage.getGuess().charAt(0), gameMessage.isPlayerOne())));
    }
}

