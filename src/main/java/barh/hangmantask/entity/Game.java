package barh.hangmantask.entity;

import lombok.Data;

@Data
public class Game {

    private Long id;

    private String mode; // SINGLE or DUEL
    private String playerOneWord;
    private String playerTwoWord;
    private String currentProgressPlayerOne;
    private String currentProgressPlayerTwo;
    private String guessedLetters;
    private int attemptsLeftPlayerOne;
    private int attemptsLeftPlayerTwo;
    private boolean isPlayerOneTurn;
    private boolean gameFinished;
    private String inviteToken;

    //TODO: check why lombok doesn't create this setter automatically?
    public void setIsPlayerOneTurn(boolean b) {
    }
}
