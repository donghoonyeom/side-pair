package sidepair.service.scheduler;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sidepair.domain.feed.Feed;
import sidepair.domain.feed.FeedStatus;
import sidepair.domain.project.Project;
import sidepair.persistence.feed.FeedRepository;
import sidepair.persistence.project.ProjectRepository;
import sidepair.service.aop.ExceptionConvert;

@Component
@Transactional
@RequiredArgsConstructor
@ExceptionConvert
public class FeedScheduler {
    private static final int DELETE_AFTER_MONTH = 2;

    private final FeedRepository feedRepository;
    private final ProjectRepository projectRepository;

    @Scheduled(cron = "0 0 4 * * *")
    public void deleteFeeds() {
        final FeedStatus status = FeedStatus.DELETED;
        final List<Feed> deletedStatusFeeds = feedRepository.findWithFeedContentByStatus(status);
        for (final Feed feed : deletedStatusFeeds) {
            delete(feed);
        }
    }

    private void delete(final Feed feed) {
        final List<Project> projects = projectRepository.findByFeed(feed);
        final boolean canDelete = canDeleteFeedBasedOnProjects(projects);
        if (canDelete) {
            deleteProjects(projects);
            deleteFeed(feed);
        }
    }

    private boolean canDeleteFeedBasedOnProjects(final List<Project> projects) {
        return projects.stream()
                .allMatch(project -> project.isCompleted() && project.isCompletedAfterMonths(DELETE_AFTER_MONTH));
    }

    private void deleteProjects(final List<Project> projects) {
        projectRepository.deleteAll(projects);
    }

    private void deleteFeed(final Feed feed) {
        feedRepository.delete(feed);
    }
}
