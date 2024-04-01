package sidepair.service.event;

import sidepair.domain.feed.Feed;
import sidepair.service.dto.feed.FeedSaveDto;


public record FeedCreateEvent(
        Feed feed,
        FeedSaveDto feedSaveDto
) {

}