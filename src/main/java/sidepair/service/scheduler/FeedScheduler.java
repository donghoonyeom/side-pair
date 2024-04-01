package sidepair.service.scheduler;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sidepair.domain.feed.Feed;
import sidepair.domain.feed.FeedStatus;
import sidepair.persistence.feed.FeedRepository;
import sidepair.service.aop.ExceptionConvert;

@Component
@Transactional
@RequiredArgsConstructor
@ExceptionConvert
public class FeedScheduler {

    private final FeedRepository feedRepository;

    @Scheduled(cron = "0 0 4 * * *")
    public void deleteFeeds() {
        final FeedStatus status = FeedStatus.DELETED;
        final List<Feed> deletedStatusFeeds = feedRepository.findWithFeedContentByStatus(status);
        for (final Feed feed : deletedStatusFeeds) {
            deleteFeed(feed);
        }
    }

    private void deleteFeed(final Feed feed) {
        feedRepository.delete(feed);
    }
}
