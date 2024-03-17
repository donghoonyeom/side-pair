package sidepair.feed.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sidepair.feed.application.mapper.FeedMapper;
import sidepair.feed.configuration.dto.FeedNodeSaveDto;
import sidepair.feed.configuration.dto.FeedSaveDto;
import sidepair.feed.configuration.dto.FeedTagSaveDto;
import sidepair.feed.configuration.requesst.FeedCategorySaveRequest;
import sidepair.feed.configuration.requesst.FeedSaveRequest;
import sidepair.feed.domain.Feed;
import sidepair.feed.domain.FeedCategory;
import sidepair.feed.domain.FeedContent;
import sidepair.feed.domain.FeedNode;
import sidepair.feed.domain.FeedNodes;
import sidepair.feed.domain.FeedTag;
import sidepair.feed.domain.FeedTags;
import sidepair.feed.domain.vo.FeedTagName;
import sidepair.service.aop.ExceptionConvert;
import sidepair.service.exception.AuthenticationException;
import sidepair.service.exception.ConflictException;
import sidepair.service.exception.ForbiddenException;
import sidepair.service.exception.NotFoundException;
import sidepair.member.domain.Member;
import sidepair.member.domain.vo.Email;
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

    @CacheEvict(value = "feedList", allEntries = true)
    public Long create(final FeedSaveRequest request, final String email) {
        final Member member = findMemberByEmail(email);
        final FeedCategory feedCategory = findFeedCategoryById(request.categoryId());
        final FeedSaveDto feedSaveDto = FeedMapper.convertToFeedSaveDto(request);
        final Feed feed = createFeed(member, feedSaveDto, feedCategory);
        final Feed savedFeed = feedRepository.save(feed);

        return savedFeed.getId();
    }

    private Member findMemberByEmail(final String email) {
        return memberRepository.findByEmail(new Email(email))
                .orElseThrow(() -> new AuthenticationException("존재하지 않는 회원입니다."));
    }

    private FeedCategory findFeedCategoryById(final Long categoryId) {
        return feedCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 카테고리입니다. categoryId = " + categoryId));
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
        feedRepository.delete(feed);
        // 프로젝트 확인
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
