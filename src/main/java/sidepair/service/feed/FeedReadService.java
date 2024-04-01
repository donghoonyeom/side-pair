package sidepair.service.feed;

import java.net.URL;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sidepair.service.mapper.FeedMapper;
import sidepair.service.dto.feed.FeedNodeDto;
import sidepair.service.dto.feed.FeedTagDto;
import sidepair.service.dto.feed.requesst.FeedOrderTypeRequest;
import sidepair.service.dto.feed.requesst.FeedSearchRequest;
import sidepair.domain.feed.Feed;
import sidepair.domain.feed.FeedCategory;
import sidepair.domain.feed.FeedContent;
import sidepair.domain.feed.FeedNode;
import sidepair.domain.feed.FeedNodes;
import sidepair.domain.feed.FeedTags;
import sidepair.service.FileService;
import sidepair.service.aop.ExceptionConvert;
import sidepair.service.dto.CustomScrollRequest;
import sidepair.service.dto.feed.FeedCategoryDto;
import sidepair.service.dto.feed.FeedContentDto;
import sidepair.service.dto.feed.FeedDto;
import sidepair.service.dto.feed.FeedForListDto;
import sidepair.service.dto.feed.FeedForListScrollDto;
import sidepair.service.dto.feed.response.FeedCategoryResponse;
import sidepair.service.dto.feed.response.FeedForListResponses;
import sidepair.service.dto.feed.response.FeedResponse;
import sidepair.service.dto.feed.response.MemberFeedResponses;
import sidepair.service.dto.mamber.MemberDto;
import sidepair.service.exception.NotFoundException;
import sidepair.service.mapper.ScrollResponseMapper;
import sidepair.domain.member.Member;
import sidepair.domain.member.vo.Email;
import sidepair.persistence.dto.FeedOrderType;
import sidepair.persistence.dto.FeedSearchDto;
import sidepair.persistence.feed.FeedCategoryRepository;
import sidepair.persistence.feed.FeedContentRepository;
import sidepair.persistence.feed.FeedRepository;
import sidepair.persistence.member.MemberRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@ExceptionConvert
public class FeedReadService {

    private final FeedRepository feedRepository;
    private final FeedCategoryRepository feedCategoryRepository;
    private final FeedContentRepository feedContentRepository;
    private final MemberRepository memberRepository;
    private final FileService fileService;

    @Cacheable(value = "feed", keyGenerator = "cacheKeyGenerator", cacheManager = "redisCacheManager")
    public FeedResponse findFeed(final Long id) {
        final Feed feed = findFeedById(id);
        final FeedContent recentFeedContent = findRecentContent(feed);
        final FeedDto feedDto = makeFeedDto(feed, recentFeedContent);
        return FeedMapper.convertToFeedResponse(feedDto);
    }

    private FeedDto makeFeedDto(final Feed feed, final FeedContent feedContent) {
        final FeedCategory category = feed.getCategory();
        final Member creator = feed.getCreator();
        final FeedContentDto feedContentDto = new FeedContentDto(
                feedContent.getId(),
                feedContent.getContent(),
                makeFeedNodeDtos(feedContent.getNodes()));
        return new FeedDto(feed.getId(), new FeedCategoryDto(category.getId(), category.getName()),
                feed.getTitle(), feed.getIntroduction(), makeMemberDto(creator),
                feedContentDto, feed.getRequiredPeriod(),
                feed.getCreatedAt(), makeFeedTagDtos(feed.getTags()));
    }

    private MemberDto makeMemberDto(final Member creator) {
        final URL url = fileService.generateUrl(creator.getImage().getServerFilePath(), HttpMethod.GET);
        return new MemberDto(creator.getId(), creator.getNickname().getValue(), url.toExternalForm());
    }

    private List<FeedNodeDto> makeFeedNodeDtos(final FeedNodes nodes) {
        return nodes.getValues()
                .stream()
                .map(this::makeFeedNodeDto)
                .toList();
    }

    private FeedNodeDto makeFeedNodeDto(final FeedNode feedNode) {
        final List<String> imageUrls = feedNode.getFeedNodeImages()
                .getValues()
                .stream()
                .map(it -> fileService.generateUrl(it.getServerFilePath(), HttpMethod.GET).toExternalForm())
                .toList();
        return new FeedNodeDto(feedNode.getId(), feedNode.getTitle(), feedNode.getContent(), imageUrls);
    }

    private List<FeedTagDto> makeFeedTagDtos(final FeedTags feedTags) {
        return feedTags.getValues()
                .stream()
                .map(it -> new FeedTagDto(it.getId(), it.getName().getValue()))
                .toList();
    }

    private Feed findFeedById(final Long id) {
        return feedRepository.findFeedById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 피드입니다. feedId = " + id));
    }

    private FeedContent findRecentContent(final Feed feed) {
        return feedContentRepository.findFirstByFeedOrderByCreatedAtDesc(feed)
                .orElseThrow(() -> new NotFoundException("피드에 컨텐츠가 존재하지 않습니다."));
    }

    @Cacheable(value = "feedList", keyGenerator = "cacheKeyGenerator", cacheManager = "redisCacheManager")
    public FeedForListResponses findFeedsByOrderType(final Long categoryId,
                                                           final FeedOrderTypeRequest orderTypeRequest,
                                                           final CustomScrollRequest scrollRequest) {
        final FeedCategory category = findCategoryById(categoryId);
        final FeedOrderType orderType = FeedMapper.convertFeedOrderType(orderTypeRequest);
        final List<Feed> feeds = feedRepository.findFeedsByCategory(category, orderType,
                scrollRequest.lastId(), scrollRequest.size());
        final FeedForListScrollDto feedForListScrollDto = makeFeedForListScrollDto(feeds,
                scrollRequest.size());
        return FeedMapper.convertFeedResponses(feedForListScrollDto);
    }

    private FeedCategory findCategoryById(final Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return feedCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 카테고리입니다. categoryId = " + categoryId));
    }

    private FeedForListScrollDto makeFeedForListScrollDto(final List<Feed> feeds, final int requestSize) {
        final List<FeedForListDto> feedForListDtos = feeds.stream()
                .map(this::makeFeedForListDto)
                .toList();
        final List<FeedForListDto> subDtos = ScrollResponseMapper.getSubResponses(feedForListDtos, requestSize);
        final boolean hasNext = ScrollResponseMapper.hasNext(feedForListDtos.size(), requestSize);
        return new FeedForListScrollDto(subDtos, hasNext);
    }

    private FeedForListDto makeFeedForListDto(final Feed feed) {
        final FeedCategory category = feed.getCategory();
        final FeedCategoryDto feedCategoryDto = new FeedCategoryDto(category.getId(),
                category.getName());
        final Member creator = feed.getCreator();
        final URL creatorImageUrl = fileService.generateUrl(creator.getImage().getServerFilePath(), HttpMethod.GET);
        final MemberDto memberDto = new MemberDto(creator.getId(), creator.getNickname().getValue(),
                creatorImageUrl.toExternalForm());
        final List<FeedTagDto> feedTagDtos = makeFeedTagDto(feed.getTags());

        return new FeedForListDto(
                feed.getId(),
                feed.getTitle(),
                feed.getIntroduction(),
                feed.getRequiredPeriod(),
                feed.getCreatedAt(),
                memberDto,
                feedCategoryDto,
                feedTagDtos
        );
    }

    private List<FeedTagDto> makeFeedTagDto(final FeedTags feedTags) {
        return feedTags.getValues()
                .stream()
                .map(tag -> new FeedTagDto(tag.getId(), tag.getName().getValue()))
                .toList();
    }

    public FeedForListResponses search(final FeedOrderTypeRequest orderTypeRequest,
                                       final FeedSearchRequest searchRequest,
                                       final CustomScrollRequest scrollRequest) {
        final FeedOrderType orderType = FeedMapper.convertFeedOrderType(orderTypeRequest);
        final FeedSearchDto feedSearchDto = FeedSearchDto.create(
                searchRequest.creatorName(), searchRequest.feedTitle(), searchRequest.tagName());
        final List<Feed> feeds = feedRepository.findFeedsByCond(feedSearchDto, orderType,
                scrollRequest.lastId(), scrollRequest.size());
        final FeedForListScrollDto feedForListScrollDto = makeFeedForListScrollDto(feeds,
                scrollRequest.size());
        return FeedMapper.convertFeedResponses(feedForListScrollDto);
    }

    @Cacheable(value = "categoryList", keyGenerator = "cacheKeyGenerator", cacheManager = "redisCacheManager")
    public List<FeedCategoryResponse> findAllFeedCategories() {
        final List<FeedCategory> feedCategories = feedCategoryRepository.findAll();
        return FeedMapper.convertFeedCategoryResponses(feedCategories);
    }

    public MemberFeedResponses findAllMemberFeeds(final String email,
                                                  final CustomScrollRequest scrollRequest) {
        final Member member = findMemberByEmail(email);
        final List<Feed> feeds = feedRepository.findFeedsWithCategoryByMemberOrderByLatest(member,
                scrollRequest.lastId(), scrollRequest.size());
        return FeedMapper.convertMemberFeedResponses(feeds, scrollRequest.size());
    }

    private Member findMemberByEmail(final String email) {
        return memberRepository.findByEmail(new Email(email))
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));
    }
}

