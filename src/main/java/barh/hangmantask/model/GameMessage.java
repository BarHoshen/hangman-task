package barh.hangmantask.model;

import lombok.Data;

@Data
public class GameMessage {
    private String action; // NEW_SINGLE_PLAYER, NEW_DUEL, JOIN_DUEL, MAKE_GUESS
    private String word;
    private String gameId;
    private String inviteToken;
    private String guess;
    private boolean isPlayerOne;
}

