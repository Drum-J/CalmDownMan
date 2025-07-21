package chimhaha.chimcard.game.event;

import chimhaha.chimcard.game.dto.MatchingRequestDto;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PlayerMatchingJoinEvent extends ApplicationEvent {
    private final MatchingRequestDto matchingRequestDto;

    public PlayerMatchingJoinEvent(Object source, MatchingRequestDto matchingRequestDto) {
        super(source);
        this.matchingRequestDto = matchingRequestDto;
    }
}
