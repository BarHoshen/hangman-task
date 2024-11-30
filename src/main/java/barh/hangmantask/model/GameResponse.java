package barh.hangmantask.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GameResponse {
    private String action;
    private String message;
    private String gameState;
    private int attemptsLeft;
}
