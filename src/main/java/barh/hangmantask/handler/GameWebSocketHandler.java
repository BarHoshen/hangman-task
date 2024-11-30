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
                case "NEW_SINGLE_PLAYER" -> createSinglePlayerGame();
                case "NEW_DUEL" -> createDuel(gameMessage);
                case "JOIN_GAME" -> joinGame(gameMessage);
                case "MAKE_GUESS" -> processGuess(gameMessage);
                default ->
                        new GameResponse("-1", "UNKNOWN", "Unrecognized action.", "unknown", -1); //session.sendMessage(new TextMessage("Unknown action: " + gameMessage.getAction()));
            };
        } catch (Exception e) {
            response = new GameResponse("-1", "ERROR", "Error parsing message.", "unknown", -1);
        }
        String jsonResponse = objectMapper.writeValueAsString(response);
        session.sendMessage(new TextMessage(jsonResponse));
    }


    private GameResponse createSinglePlayerGame() {
        Game game = gameService.createSinglePlayerGame();
        return new GameResponse(game.getId(), "GAME_CREATED", game.getCurrentProgressPlayerOne(), "IN_PROGRESS", game.getAttemptsLeftPlayerOne());
    }

    private GameResponse createDuel(GameMessage gameMessage) {
        Game game = gameService.createDuel(gameMessage.getWord());
        return new GameResponse(game.getId(), "GAME_CREATED", "?inviteToken=" + game.getInviteToken(), "WAITING_FOR_PLAYER", 0);
    }

    private GameResponse joinGame(GameMessage gameMessage) {
        Game game = gameService.joinDuel(gameMessage.getInviteToken(), gameMessage.getWord());
        return new GameResponse(game.getId(), "GAME_JOINED", game.getCurrentProgressPlayerOne(), "IN_PROGRESS", game.getAttemptsLeftPlayerTwo());
    }

    private GameResponse processGuess(GameMessage gameMessage) {
        Game game = gameService.makeGuess(gameMessage.getGameId(), gameMessage.getGuess().charAt(0), gameMessage.isPlayerOne());
        return new GameResponse(game.getId(), "GAME_UPDATED", gameMessage.isPlayerOne() ? game.getCurrentProgressPlayerOne() : game.getCurrentProgressPlayerTwo(), game.isGameFinished() ? "FINISHED" : "IN_PROGRESS", gameMessage.isPlayerOne() ? game.getAttemptsLeftPlayerOne() : game.getAttemptsLeftPlayerTwo());
    }
}

