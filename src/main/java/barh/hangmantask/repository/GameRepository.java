package barh.hangmantask.repository;

import barh.hangmantask.entity.Game;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GameRepository extends MongoRepository<Game, String> {
    Game findByInviteToken(String inviteToken);
}
