package sidepair.service.feed;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sidepair.domain.feed.FeedApplicant;
import sidepair.domain.project.Project;
import sidepair.persistence.feed.FeedApplicantRepository;
import sidepair.persistence.project.ProjectRepository;
import sidepair.service.dto.feed.FeedApplicantDto;
import sidepair.service.dto.feed.requesst.FeedApplicantSaveRequest;
import sidepair.service.event.FeedCreateEvent;
import sidepair.service.exception.BadRequestException;
import sidepair.service.mapper.FeedMapper;
import sidepair.service.dto.feed.FeedNodeSaveDto;
import sidepair.service.dto.feed.FeedSaveDto;
import sidepair.service.dto.feed.FeedTagSaveDto;
import sidepair.service.dto.feed.requesst.FeedCategorySaveRequest;
import sidepair.service.dto.feed.requesst.FeedSaveRequest;
import sidepair.domain.feed.Feed;
import sidepair.domain.feed.FeedCategory;
import sidepair.domain.feed.FeedContent;
import sidepair.domain.feed.FeedNode;
import sidepair.domain.feed.FeedNodes;
import sidepair.domain.feed.FeedTag;
import sidepair.domain.feed.FeedTags;
import sidepair.domain.feed.vo.FeedTagName;
import sidepair.service.aop.ExceptionConvert;
import sidepair.service.exception.AuthenticationException;
import sidepair.service.exception.ConflictException;
import sidepair.service.exception.ForbiddenException;
import sidepair.service.exception.NotFoundException;
import sidepair.domain.member.Member;
import sidepair.domain.member.vo.Email;
import sidepair.persistence.feed.FeedCategoryRepository;
import sidepair.persistence.feed.FeedRepository;
import sidepair.persistence.member.MemberRepository;

@Service
@Transactional
@RequiredArgsConstructor
@ExceptionConvert
public class FeedCreateService {

    private final MemberRepository memberRepository;
    private final FeedRepository feedRepository;
    private final FeedCategoryRepository feedCategoryRepository;
    private final FeedApplicantRepository feedApplicantRepository;
    private final ProjectRepository projectRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @CacheEvict(value = "feedList", allEntries = true)
    public Long create(final FeedSaveRequest request, final String email) {
        final Member member = findMemberByEmail(email);
        final FeedCategory feedCategory = findFeedCategoryById(request.categoryId());
        final FeedSaveDto feedSaveDto = FeedMapper.convertToFeedSaveDto(request);
        final Feed feed = createFeed(member, feedSaveDto, feedCategory);
        final Feed savedFeed = feedRepository.save(feed);

        applicationEventPublisher.publishEvent(new FeedCreateEvent(savedFeed, feedSaveDto));

        return savedFeed.getId();
    }

    public Long createApplicant(final Long feedId, final String email, final FeedApplicantSaveRequest request) {
        final Feed feed = findFeedById(feedId);
        final Member member = findMemberByEmail(email);
        final FeedApplicantDto feedApplicantDto = FeedMapper.convertFeedApplicantDto(request, member);
        validateApplicantQualification(feed, member);
        validateApplicantCount(feed, member);
        final FeedApplicant feedApplicant = new FeedApplicant(feedApplicantDto.content(), feedApplicantDto.member());

        feed.addApplicant(feedApplicant);
        feedApplicantRepository.save(feedApplicant);
        return feedApplicant.getId();
    }

    private Member findMemberByEmail(final String email) {
        return memberRepository.findByEmail(new Email(email))
                .orElseThrow(() -> new AuthenticationException("존재하지 않는 회원입니다."));
    }

    private FeedCategory findFeedCategoryById(final Long categoryId) {
        return feedCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 카테고리입니다. categoryId = " + categoryId));
    }

    private void validateApplicantQualification(final Feed feed, final Member member) {
        if (feed.isCreator(member)) {
            throw new BadRequestException(
                    "피드 생성자는 신청서를 보낼 수 없습니다. feedId = " + feed.getId() + " memberId = " + member.getId());
        }
    }

    private void validateApplicantCount(final Feed feed, final Member member) {
        if (feedApplicantRepository.findByFeedAndMember(feed, member).isPresent()) {
            throw new BadRequestException(
                    "이미 작성한 신청서가 존재합니다. feedId = " + feed.getId() + " memberId = " + member.getId());
        }
    }

    public void projectJoinPermission(final String email, final Long feedId, final Long applicantId) {
        final Member leader = findMemberByEmail(email);
        final FeedApplicant applicant = findByApplicantId(applicantId);
        final Project project = findProjectByFeedIdWithPessimisticLock(feedId);
        final Member member = findMemberInformationByApplicant(applicant);
        checkProjectPendingLeader(leader, project);
        project.join(member);
    }

    public FeedApplicant findByApplicantId(Long applicantId) {
        return feedApplicantRepository.findById(applicantId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 신청서입니다."));
    }

    private Member findMemberInformationByApplicant(final FeedApplicant applicant) {
        return memberRepository.findWithMemberProfileAndImageByApplicant(applicant)
                .orElseThrow(() -> new NotFoundException("작성한 신청서가 존재하지 않는 회원입니다."));
    }

    private Project findProjectByFeedIdWithPessimisticLock(final Long feedId) {
        return projectRepository.findProjectByFeedIdWithPessimisticLock(feedId)
                .orElseThrow(() -> new NotFoundException("프로젝트가 존재하지 않는 피드입니다. feedId = " + feedId));
    }

    private void checkProjectPendingLeader(final Member member, final Project project) {
        if (project.isNotPendingLeader(member)) {
            throw new BadRequestException("프로젝트의 리더만 멤버를 추가할 수 있습니다.");
        }
    }

    private Feed createFeed(final Member member, final FeedSaveDto feedSaveDto,
                            final FeedCategory feedCategory) {
        final FeedNodes feedNodes = makeFeedNodes(feedSaveDto.feedNodes());
        final FeedContent feedContent = makeFeedContent(feedSaveDto, feedNodes);
        final FeedTags feedTags = makeFeedTags(feedSaveDto.tags());
        final Feed feed = makeFeed(member, feedSaveDto, feedCategory);
        feed.addContent(feedContent);
        feed.addTags(feedTags);
        return feed;
    }

    private FeedNodes makeFeedNodes(final List<FeedNodeSaveDto> feedNodeSaveDtos) {
        return new FeedNodes(
                feedNodeSaveDtos.stream()
                        .map(node -> new FeedNode(node.title(), node.content()))
                        .toList()
        );
    }

    private FeedContent makeFeedContent(final FeedSaveDto feedSaveDto, final FeedNodes feedNodes) {
        final FeedContent feedContent = new FeedContent(feedSaveDto.content());
        feedContent.addNodes(feedNodes);
        return feedContent;
    }

    private FeedTags makeFeedTags(final List<FeedTagSaveDto> feedTagSaveDto) {
        return new FeedTags(
                feedTagSaveDto.stream()
                        .map(tag -> new FeedTag(new FeedTagName(tag.name())))
                        .toList()
        );
    }

    @CacheEvict(value = "categoryList", allEntries = true)
    public void createFeedCategory(final FeedCategorySaveRequest feedCategorySaveRequest) {
        final FeedCategory feedCategory = FeedMapper.convertToFeedCategory(feedCategorySaveRequest);
        feedCategoryRepository.findByName(feedCategory.getName())
                .ifPresent(it -> {
                    throw new ConflictException("이미 존재하는 이름의 카테고리입니다.");
                });
        feedCategoryRepository.save(feedCategory);
    }

    private Feed makeFeed(final Member member, final FeedSaveDto feedSaveDto,
                          final FeedCategory feedCategory) {
        return new Feed(feedSaveDto.title(), feedSaveDto.introduction(),
                feedSaveDto.requiredPeriod(), member, feedCategory);
    }

    @CacheEvict(value = {"feedList", "feed"}, allEntries = true)
    public void deleteFeed(final String email, final Long feedId) {
        final Feed feed = findFeedById(feedId);
        validateFeedCreator(feedId, email);
        final List<Project> project = projectRepository.findByFeed(feed);
        if (project.isEmpty()) {
            feedRepository.delete(feed);
            return;
        }
        feed.delete();
    }

    private Feed findFeedById(final Long id) {
        return feedRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 피드입니다. feedId = " + id));
    }

    private void validateFeedCreator(final Long feedId, final String email) {
        feedRepository.findByIdAndMemberEmail(feedId, email)
                .orElseThrow(() -> new ForbiddenException("해당 피드를 생성한 사용자가 아닙니다."));
    }
}
