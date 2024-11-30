package barh.hangmantask.service;

import barh.hangmantask.entity.Game;
import barh.hangmantask.repository.GameRepository;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Data

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final WebClient webClient;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
        this.webClient = WebClient.create("https://random-word-api.herokuapp.com/word"); // Example API
    }

    public Mono<Game> createSinglePlayerGame() {
        return fetchRandomWord()
                .flatMap(word -> {
                    Game game = new Game();
                    game.setMode("SINGLE");
                    game.setPlayerOneWord(word);
                    game.setCurrentProgressPlayerOne("_".repeat(word.length()));
                    game.setAttemptsLeftPlayerOne(6);
                    game.setIsPlayerOneTurn(true);
                    return gameRepository.save(game);
                });
    }

    public Mono<Game> createDuel(String playerOneWord) {
        Game game = new Game();
        game.setMode("DUEL");
        game.setPlayerOneWord(playerOneWord);
        game.setCurrentProgressPlayerOne("_".repeat(playerOneWord.length()));
        game.setAttemptsLeftPlayerOne(6);
        game.setIsPlayerOneTurn(true);
        game.setInviteToken(UUID.randomUUID().toString());
        return gameRepository.save(game);
    }

    public Mono<Game> joinDuel(String inviteToken, String playerTwoWord) {
        return gameRepository.findByInviteToken(inviteToken).flatMap(game -> {
            game.setPlayerTwoWord(playerTwoWord);
            game.setCurrentProgressPlayerTwo("_".repeat(playerTwoWord.length()));
            game.setAttemptsLeftPlayerTwo(6);
            return gameRepository.save(game);
        });
    }

    public Mono<Game> makeGuess(Long gameId, char guess, boolean isPlayerOne) {
        return gameRepository.findById(gameId).flatMap(game -> {
            String word = isPlayerOne ? game.getPlayerTwoWord() : game.getPlayerOneWord();
            StringBuilder progress = new StringBuilder(isPlayerOne ? game.getCurrentProgressPlayerTwo() : game.getCurrentProgressPlayerOne());
            boolean correct = false;

            for (int i = 0; i < word.length(); i++) {
                if (word.charAt(i) == guess) {
                    progress.setCharAt(i, guess);
                    correct = true;
                }
            }

            if (!correct) {
                if (isPlayerOne) {
                    game.setAttemptsLeftPlayerTwo(game.getAttemptsLeftPlayerTwo() - 1);
                } else {
                    game.setAttemptsLeftPlayerOne(game.getAttemptsLeftPlayerOne() - 1);
                }
            }

            if (isPlayerOne) {
                game.setCurrentProgressPlayerTwo(progress.toString());
            } else {
                game.setCurrentProgressPlayerOne(progress.toString());
            }

            game.setIsPlayerOneTurn(!isPlayerOne);

            if (progress.toString().equals(word) || game.getAttemptsLeftPlayerOne() <= 0 || game.getAttemptsLeftPlayerTwo() <= 0) {
                game.setGameFinished(true);
            }

            return gameRepository.save(game);
        });
    }

    private Mono<String> fetchRandomWord() {
        return webClient.get()
                .retrieve()
                .bodyToMono(String.class)
                .map(word -> word.replaceAll("[\"\\[\\]]", "")); // Cleaning up the response
    }
}

