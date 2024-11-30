package barh.hangmantask.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GameMessage {
    private String action; // NEW_SINGLE_PLAYER, NEW_DUEL, JOIN_DUEL, MAKE_GUESS
    private String word;
    private String gameId;
    private String inviteToken;
    private String guess;
    @JsonProperty(value = "isPlayerOne")
    private boolean isPlayerOne;
}

