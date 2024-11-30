package barh.hangmantask.service;

import barh.hangmantask.entity.Game;
import barh.hangmantask.repository.GameRepository;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@Data

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final RestTemplate restTemplate;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
        this.restTemplate = new RestTemplate();
    }

    public Game createSinglePlayerGame() {
        String word = fetchRandomWord();
        Game game = new Game();
        game.setMode("SINGLE");
        game.setPlayerOneWord(word);
        game.setCurrentProgressPlayerOne("_".repeat(word.length()));
        game.setAttemptsLeftPlayerOne(6);
        game.setPlayerOneTurn(true);
        return gameRepository.save(game);
    }

    public Game createDuel(String word) {
        Game game = new Game();
        game.setMode("DUEL");
        game.setPlayerTwoWord(word);
        game.setCurrentProgressPlayerTwo("_".repeat(word.length()));
        game.setAttemptsLeftPlayerOne(6);
        game.setPlayerOneTurn(true);
        game.setInviteToken(UUID.randomUUID().toString());
        return gameRepository.save(game);
    }

    public Game joinDuel(String inviteToken, String word) {
        Game game = gameRepository.findByInviteToken(inviteToken);
        game.setPlayerOneWord(word);
        game.setCurrentProgressPlayerOne("_".repeat(word.length()));
        game.setAttemptsLeftPlayerTwo(6);
        return gameRepository.save(game);
    }

    //TODO: reconsider logic that recreates progress string every time.
    public Game makeGuess(String gameId, char guess, boolean isPlayerOne) {
        Optional<Game> gameOptional = gameRepository.findById(gameId);
        if (gameOptional.isEmpty()) {
            throw new IllegalArgumentException("Game not found");
        }
        Game game = gameOptional.get();
        String word = isPlayerOne ? game.getPlayerOneWord() : game.getPlayerTwoWord();
        StringBuilder progress = new StringBuilder(isPlayerOne ? game.getCurrentProgressPlayerOne() : game.getCurrentProgressPlayerTwo());
        boolean correct = false;
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == guess) {
                progress.setCharAt(i, guess);
                correct = true;
            }
        }
        if (!correct) {
            if (isPlayerOne) {
                game.setAttemptsLeftPlayerOne(game.getAttemptsLeftPlayerOne() - 1);
            } else {
                game.setAttemptsLeftPlayerTwo(game.getAttemptsLeftPlayerTwo() - 1);
            }
        }
        if (isPlayerOne) {
            game.setCurrentProgressPlayerOne(progress.toString());
        } else {
            game.setCurrentProgressPlayerTwo(progress.toString());
        }
        game.setPlayerOneTurn(!isPlayerOne);
        if (progress.toString().equals(word) || game.getAttemptsLeftPlayerOne() <= 0 || (game.getAttemptsLeftPlayerTwo() <= 0 && game.getMode().equals("DUEL"))) {
            game.setGameFinished(true);
        }
        return gameRepository.save(game);
    }
    //TODO: Add exception handling for api call.
    private String fetchRandomWord() {
        return restTemplate.getForObject("https://random-word-api.herokuapp.com/word", String.class)
                .replaceAll("[\"\\[\\]]", "");
    }
}

