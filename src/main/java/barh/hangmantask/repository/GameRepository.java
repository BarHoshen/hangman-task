package barh.hangmantask.repository;

import barh.hangmantask.entity.Game;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface GameRepository extends ReactiveCrudRepository<Game, Long> {
    Mono<Game> findByInviteToken(String inviteToken);
}
