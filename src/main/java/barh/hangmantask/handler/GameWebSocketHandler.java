package barh.hangmantask.handler;

import barh.hangmantask.model.GameMessage;
import barh.hangmantask.service.GameService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Component
public class GameWebSocketHandler implements WebSocketHandler {


    private final GameService gameService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    @Override
    @NonNull
    public Mono<Void> handle(@NonNull WebSocketSession session) {
        sessionMap.put(session.getId(), session);
        return session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .flatMap(message -> handleMessage(session, message))
                .doFinally(signalType -> sessionMap.remove(session.getId()))
                .then();
    }

    private Mono<Void> handleMessage(WebSocketSession session, String message) {
        try {
            GameMessage gameMessage = objectMapper.readValue(message, GameMessage.class);
            return switch (gameMessage.getAction()) {
                case "NEW_SINGLE_PLAYER" -> createSinglePlayerGame(session, gameMessage);
                case "NEW_DUEL" -> createDuel(session, gameMessage);
                case "JOIN_GAME" -> joinGame(session, gameMessage);
                case "MAKE_GUESS" -> processGuess(session, gameMessage);
                default -> Mono.empty();
            };
        } catch (Exception e) {
            return session.send(Mono.just(session.textMessage("Error processing message: " + e.getMessage())));
        }
    }

    private Mono<Void> createSinglePlayerGame(WebSocketSession session, GameMessage gameMessage) {
        return gameService.createSinglePlayerGame()
                .flatMap(game -> session.send(Mono.just(session.textMessage("GAME_CREATED: " + game.getId()))));
    }
    private Mono<Void> createDuel(WebSocketSession session, GameMessage gameMessage) {
        return gameService.createDuel(gameMessage.getWord())
                .flatMap(game -> session.send(Mono.just(session.textMessage("GAME_CREATED: " + game.getInviteToken()))));
    }

    private Mono<Void> joinGame(WebSocketSession session, GameMessage gameMessage) {
        return gameService.joinDuel(gameMessage.getInviteToken(), gameMessage.getWord())
                .flatMap(game -> session.send(Mono.just(session.textMessage("GAME_JOINED: " + game.getId()))));
    }

    private Mono<Void> processGuess(WebSocketSession session, GameMessage gameMessage) {
        return gameService.makeGuess(gameMessage.getGameId(), gameMessage.getGuess().charAt(0), gameMessage.isPlayerOne())
                .flatMap(game -> session.send(Mono.just(session.textMessage("GAME_UPDATED"))));
    }
}

