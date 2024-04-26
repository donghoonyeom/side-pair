package sidepair.persistence.project;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import sidepair.domain.feed.Feed;
import sidepair.domain.member.Member;
import sidepair.domain.project.Project;
import sidepair.domain.project.ProjectStatus;
import sidepair.persistence.project.dto.FeedProjectsOrderType;

public interface ProjectQueryRepository {

    Optional<Project> findProjectByIdWithPessimisticLock(Long projectId);

    Optional<Project> findByIdWithFeedContent(final Long projectId);

    Optional<Project> findByIdWithContentAndTodos(final Long projectId);

    List<Project> findProjectsByFeedAndCond(final Feed feed,
                                            final FeedProjectsOrderType filterType,
                                            final Long lastId,
                                            final int pageSize);

    Optional<Project> findByIdWithTodos(final Long projectId);

    List<Project> findByMember(final Member member);

    List<Project> findByMemberAndStatus(final Member member, final ProjectStatus projectStatus);

    Optional<Project> findByIdWithNodes(final Long projectId);

    List<Project> findByFeed(final Feed feed);

    List<Project> findAllRecruitingProjectsByStartDateEarlierThan(final LocalDate startDate);
}
