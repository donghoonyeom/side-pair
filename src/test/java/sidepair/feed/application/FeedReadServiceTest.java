package sidepair.feed.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sidepair.feed.configuration.requesst.FeedOrderTypeRequest;
import sidepair.feed.configuration.requesst.FeedSearchRequest;
import sidepair.feed.configuration.response.FeedCategoryResponse;
import sidepair.feed.configuration.response.FeedContentResponse;
import sidepair.feed.configuration.response.FeedForListResponse;
import sidepair.feed.configuration.response.FeedForListResponses;
import sidepair.feed.configuration.response.FeedNodeResponse;
import sidepair.feed.configuration.response.FeedTagResponse;
import sidepair.feed.configuration.response.MemberFeedResponse;
import sidepair.feed.configuration.response.MemberFeedResponses;
import sidepair.feed.domain.Feed;
import sidepair.feed.domain.FeedCategory;
import sidepair.feed.domain.FeedContent;
import sidepair.feed.domain.FeedNode;
import sidepair.feed.domain.FeedNodes;
import sidepair.feed.domain.FeedTag;
import sidepair.feed.domain.FeedTags;
import sidepair.feed.domain.vo.FeedTagName;
import sidepair.global.domain.ImageContentType;
import sidepair.service.FileService;
import sidepair.service.dto.CustomScrollRequest;
import sidepair.service.dto.feed.response.FeedResponse;
import sidepair.service.dto.mamber.response.MemberResponse;
import sidepair.service.exception.NotFoundException;
import sidepair.member.domain.EncryptedPassword;
import sidepair.member.domain.Member;
import sidepair.member.domain.MemberProfile;
import sidepair.member.domain.Skill;
import sidepair.member.domain.vo.Email;
import sidepair.member.domain.vo.MemberImage;
import sidepair.member.domain.vo.Nickname;
import sidepair.member.domain.vo.Password;
import sidepair.persistence.feed.FeedCategoryRepository;
import sidepair.persistence.feed.FeedContentRepository;
import sidepair.persistence.feed.FeedRepository;
import sidepair.persistence.member.MemberRepository;

@ExtendWith(MockitoExtension.class)
class FeedReadServiceTest {

    private final Member member = new Member(1L, new Email("test@test.com"),null,
            new EncryptedPassword(new Password("password1!")), new Nickname("닉네임"),
            new MemberImage("originalFileName", "default-member-image", ImageContentType.JPG),
            new MemberProfile(Skill.JAVA));
    private final LocalDateTime now = LocalDateTime.now();

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private FeedCategoryRepository feedCategoryRepository;

    @Mock
    private FeedContentRepository feedContentRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private FileService fileService;

    @InjectMocks
    private FeedReadService feedService;

    @Test
    void 특정_아이디를_가지는_피드_단일_조회시_해당_피드의_정보를_반환한다() throws MalformedURLException {
        //given
        final Member member = 사용자를_생성한다(1L, "test@test.com", "두두");
        final FeedCategory category = 피드_카테고리를_생성한다(1L, "운동");
        final FeedContent content = 피드_컨텐츠를_생성한다(1L, "콘텐츠 내용");
        final Feed feed = 피드을_생성한다("피드 제목", category);
        feed.addContent(content);
        final Long feedId = 1L;


        when(feedRepository.findFeedById(anyLong()))
                .thenReturn(Optional.of(feed));
        when(feedContentRepository.findFirstByFeedOrderByCreatedAtDesc(any()))
                .thenReturn(Optional.of(feed.getContents().getValues().get(0)));
        when(fileService.generateUrl(anyString(), any()))
                .thenReturn(new URL("http://example.com/serverFilePath"));

        //when
        final FeedResponse feedResponse = feedService.findFeed(feedId);

        //then
        final FeedResponse expectedResponse = new FeedResponse(
                feedId, new FeedCategoryResponse(1L, "운동"), "피드 제목", "피드 소개글",
                new MemberResponse(1L, "닉네임", "http://example.com/serverFilePath"),
                new FeedContentResponse(1L, "피드 본문", List.of(
                        new FeedNodeResponse(1L, "피드 노드1 제목", "피드 노드1 설명", Collections.emptyList())
                )),  30, now,
                List.of(new FeedTagResponse(1L, "태그1"),
                        new FeedTagResponse(2L, "태그2"))
        );

        assertThat(feedResponse)
                .usingRecursiveComparison()
                .ignoringFields("createdAt")
                .isEqualTo(expectedResponse);
    }

    @Test
    void 피드_단일_조회_시_피드_아이디가_존재하지_않는_아이디일_경우_예외를_반환한다() {
        //when
        when(feedRepository.findFeedById(anyLong()))
                .thenReturn(Optional.empty());

        //then
        assertThatThrownBy(() -> feedService.findFeed(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 피드_목록_조회시_카테고리_아이디가_유효하지_않으면_예외가_발생한다() {
        // given
        when(feedCategoryRepository.findById(any()))
                .thenReturn(Optional.empty());

        final Long categoryId = 1L;
        final FeedOrderTypeRequest filterType = FeedOrderTypeRequest.LATEST;
        final CustomScrollRequest scrollRequest = new CustomScrollRequest(null, 10);

        // expected
        assertThatThrownBy(() -> feedService.findFeedsByOrderType(categoryId, filterType, scrollRequest))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 피드_목록_조회_시_필터_조건이_null이면_최신순으로_조회한다() throws MalformedURLException {
        // given
        final FeedCategory category = new FeedCategory(1L, "여행");
        final List<Feed> feeds = List.of(
                피드을_생성한다("첫 번째 피드", category),
                피드을_생성한다("두 번째 피드", category));

        when(feedCategoryRepository.findById(any()))
                .thenReturn(Optional.of(category));
        when(feedRepository.findFeedsByCategory(any(), any(), any(), anyInt()))
                .thenReturn(feeds);
        given(fileService.generateUrl(anyString(), any()))
                .willReturn(new URL("http://example.com/serverFilePath"));

        final Long categoryId = 1L;
        final FeedOrderTypeRequest filterType = null;
        final CustomScrollRequest scrollRequest = new CustomScrollRequest(null, 10);

        // when
        final FeedForListResponses feedResponses = feedService.findFeedsByOrderType(
                categoryId, filterType, scrollRequest);

        // then
        final FeedForListResponse firstFeedResponse = new FeedForListResponse(1L, "첫 번째 피드", "피드 소개글",
                 30, LocalDateTime.now(),
                new MemberResponse(1L, "닉네임", "http://example.com/serverFilePath"),
                new FeedCategoryResponse(1, "여행"),
                List.of(
                        new FeedTagResponse(1L, "태그1"),
                        new FeedTagResponse(2L, "태그2")));

        final FeedForListResponse secondFeedResponse = new FeedForListResponse(1L, "두 번째 피드", "피드 소개글",
                30, LocalDateTime.now(),
                new MemberResponse(1L, "닉네임", "http://example.com/serverFilePath"),
                new FeedCategoryResponse(1, "여행"),
                List.of(
                        new FeedTagResponse(1L, "태그1"),
                        new FeedTagResponse(2L, "태그2")));

        final List<FeedForListResponse> responses = List.of(firstFeedResponse, secondFeedResponse);
        final FeedForListResponses expected = new FeedForListResponses(responses, false);

        assertThat(feedResponses)
                .usingRecursiveComparison()
                .ignoringFields("responses.createdAt")
                .isEqualTo(expected);
    }

    @Test
    void 피드_목록_조회시_다음_요소가_존재하면_true로_반환한다() throws MalformedURLException {
        // given
        final FeedCategory category = new FeedCategory(1L, "여행");
        final List<Feed> feeds = List.of(
                피드을_생성한다("첫 번째 피드", category),
                피드을_생성한다("두 번째 피드", category));

        when(feedCategoryRepository.findById(any()))
                .thenReturn(Optional.of(category));
        when(feedRepository.findFeedsByCategory(any(), any(), any(), anyInt()))
                .thenReturn(feeds);
        given(fileService.generateUrl(anyString(), any()))
                .willReturn(new URL("http://example.com/serverFilePath"));

        final Long categoryId = 1L;
        final FeedOrderTypeRequest filterType = null;
        final CustomScrollRequest scrollRequest = new CustomScrollRequest(null, 1);

        // when
        final FeedForListResponses feedResponses = feedService.findFeedsByOrderType(
                categoryId, filterType, scrollRequest);

        // then
        final FeedForListResponse firstFeedResponse = new FeedForListResponse(
                1L, "첫 번째 피드", "피드 소개글",  30, LocalDateTime.now(),
                new MemberResponse(1L, "닉네임", "http://example.com/serverFilePath"),
                new FeedCategoryResponse(1, "여행"),
                List.of(
                        new FeedTagResponse(1L, "태그1"),
                        new FeedTagResponse(2L, "태그2")));

        final List<FeedForListResponse> responses = List.of(firstFeedResponse);
        final FeedForListResponses expected = new FeedForListResponses(responses, true);

        assertThat(feedResponses)
                .usingRecursiveComparison()
                .ignoringFields("responses.createdAt")
                .isEqualTo(expected);
    }

    @Test
    void 피드_목록_조회_시_카테고리_조건이_null이면_전체_카테고리를_대상으로_최신순으로_조회한다() throws MalformedURLException {
        // given
        final FeedCategory category = new FeedCategory(1L, "여행");
        final List<Feed> feeds = List.of(피드을_생성한다("첫 번째 피드", category), 피드을_생성한다("두 번째 피드", category));

        when(feedRepository.findFeedsByCategory(any(), any(), any(), anyInt()))
                .thenReturn(feeds);
        given(fileService.generateUrl(anyString(), any()))
                .willReturn(new URL("http://example.com/serverFilePath"));

        final Long categoryId = null;
        final FeedOrderTypeRequest filterType = FeedOrderTypeRequest.LATEST;
        final CustomScrollRequest scrollRequest = new CustomScrollRequest(null, 10);

        // when
        final FeedForListResponses feedResponses = feedService.findFeedsByOrderType(
                categoryId, filterType, scrollRequest);

        // then
        final FeedForListResponse firstFeedResponse = new FeedForListResponse(1L, "첫 번째 피드", "피드 소개글",
                 30, LocalDateTime.now(),
                new MemberResponse(1L, "닉네임", "http://example.com/serverFilePath"),
                new FeedCategoryResponse(1, "여행"),
                List.of(
                        new FeedTagResponse(1L, "태그1"),
                        new FeedTagResponse(2L, "태그2")));

        final FeedForListResponse secondFeedResponse = new FeedForListResponse(1L, "두 번째 피드", "피드 소개글",
                 30, LocalDateTime.now(),
                new MemberResponse(1L, "닉네임", "http://example.com/serverFilePath"),
                new FeedCategoryResponse(1, "여행"),
                List.of(
                        new FeedTagResponse(1L, "태그1"),
                        new FeedTagResponse(2L, "태그2")));

        final FeedForListResponses expected = new FeedForListResponses(
                List.of(firstFeedResponse, secondFeedResponse), false);

        assertThat(feedResponses)
                .usingRecursiveComparison()
                .ignoringFields("responses.createdAt")
                .isEqualTo(expected);
    }

    @Test
    void 카테고리_아이디와_필터링_조건을_통해_피드_목록을_조회한다() throws MalformedURLException {
        // given
        final FeedCategory category = new FeedCategory(1L, "여행");
        final List<Feed> feeds = List.of(피드을_생성한다("첫 번째 피드", category));

        when(feedCategoryRepository.findById(any()))
                .thenReturn(Optional.of(new FeedCategory("여행")));
        when(feedRepository.findFeedsByCategory(any(), any(), any(), anyInt()))
                .thenReturn(feeds);
        given(fileService.generateUrl(anyString(), any()))
                .willReturn(new URL("http://example.com/serverFilePath"));

        final Long categoryId = 1L;
        final FeedOrderTypeRequest filterType = FeedOrderTypeRequest.LATEST;
        final CustomScrollRequest scrollRequest = new CustomScrollRequest(null, 10);

        // when
        final FeedForListResponses feedResponses = feedService.findFeedsByOrderType(
                categoryId, filterType, scrollRequest);

        // then
        final FeedForListResponse feedResponse = new FeedForListResponse(1L, "첫 번째 피드", "피드 소개글",
                 30, LocalDateTime.now(),
                new MemberResponse(1L, "닉네임", "http://example.com/serverFilePath"),
                new FeedCategoryResponse(1, "여행"),
                List.of(
                        new FeedTagResponse(1L, "태그1"),
                        new FeedTagResponse(2L, "태그2")));

        final FeedForListResponses expected = new FeedForListResponses(List.of(feedResponse), false);

        assertThat(feedResponses)
                .usingRecursiveComparison()
                .ignoringFields("responses.createdAt")
                .isEqualTo(expected);
    }

    @Test
    void 피드_전체_카테고리_리스트를_반환한다() {
        // given
        final List<FeedCategory> feedCategories = 피드_카테고리_리스트를_반환한다();
        when(feedCategoryRepository.findAll())
                .thenReturn(feedCategories);

        // when
        final List<FeedCategoryResponse> categoryResponses = feedService.findAllFeedCategories();

        // then
        final List<FeedCategoryResponse> expected = 피드_카테고리_응답_리스트를_반환한다();
        assertThat(categoryResponses)
                .isEqualTo(expected);
    }

    @Test
    void 피드을_검색한다() throws MalformedURLException {
        // given
        final FeedCategory category = new FeedCategory(1L, "여행");
        final List<Feed> feeds = List.of(
                피드을_생성한다("첫 번째 피드", category),
                피드을_생성한다("두 번째 피드", category));

        when(feedRepository.findFeedsByCond(any(), any(), any(), anyInt()))
                .thenReturn(feeds);
        given(fileService.generateUrl(anyString(), any()))
                .willReturn(new URL("http://example.com/serverFilePath"));

        final FeedSearchRequest feedSearchRequest = new FeedSearchRequest("피드", "닉네임", "태그");
        final FeedOrderTypeRequest filterType = FeedOrderTypeRequest.LATEST;
        final CustomScrollRequest scrollRequest = new CustomScrollRequest(null, 10);

        // when
        final FeedForListResponses feedResponses = feedService.search(
                filterType, feedSearchRequest, scrollRequest);

        // then
        final FeedForListResponse firstFeedResponse = new FeedForListResponse(1L, "첫 번째 피드", "피드 소개글",
                 30, LocalDateTime.now(),
                new MemberResponse(1L, "닉네임", "http://example.com/serverFilePath"),
                new FeedCategoryResponse(1, "여행"),
                List.of(
                        new FeedTagResponse(1L, "태그1"),
                        new FeedTagResponse(2L, "태그2")));

        final FeedForListResponse secondFeedResponse = new FeedForListResponse(1L, "두 번째 피드", "피드 소개글",
                 30, LocalDateTime.now(),
                new MemberResponse(1L, "닉네임", "http://example.com/serverFilePath"),
                new FeedCategoryResponse(1, "여행"),
                List.of(
                        new FeedTagResponse(1L, "태그1"),
                        new FeedTagResponse(2L, "태그2")));

        final FeedForListResponses expected = new FeedForListResponses(
                List.of(firstFeedResponse, secondFeedResponse), false);

        assertThat(feedResponses)
                .usingRecursiveComparison()
                .ignoringFields("responses.createdAt")
                .isEqualTo(expected);
    }

    @Test
    void 사용자가_생성한_피드을_조회한다() {
        // given
        final Member member = 사용자를_생성한다(1L, "test@test.com", "두두");
        final FeedCategory category1 = 피드_카테고리를_생성한다(1L, "운동");
        final FeedCategory category2 = 피드_카테고리를_생성한다(2L, "여가");
        final Feed feed1 = 피드을_생성한다("피드1", category1);
        final Feed feed2 = 피드을_생성한다("피드2", category2);

        when(memberRepository.findByEmail(any()))
                .thenReturn(Optional.of(member));
        when(feedRepository.findFeedsWithCategoryByMemberOrderByLatest(any(), any(), anyInt()))
                .thenReturn(List.of(feed2, feed1));

        // when
        final MemberFeedResponses memberFeedResponse = feedService.findAllMemberFeeds(
                "test@test.com", new CustomScrollRequest(null, 10));

        // then
        final MemberFeedResponses expected = new MemberFeedResponses(List.of(
                new MemberFeedResponse(2L, "피드2",  LocalDateTime.now(),
                        new FeedCategoryResponse(2L, "여가")),
                new MemberFeedResponse(1L, "피드1",  LocalDateTime.now(),
                        new FeedCategoryResponse(1L, "운동"))), false);

        assertThat(memberFeedResponse)
                .usingRecursiveComparison()
                .ignoringFields("responses.feedId", "responses.createdAt")
                .isEqualTo(expected);
    }

    @Test
    void 사용자가_생성한_피드을_조회할때_존재하지_않는_회원이면_예외가_발생한다() {
        // given
        when(memberRepository.findByEmail(any()))
                .thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> feedService.findAllMemberFeeds("test@test.com",
                new CustomScrollRequest(null, 10))).isInstanceOf(NotFoundException.class)
                .hasMessageContaining("존재하지 않는 회원입니다.");
    }

    private Member 사용자를_생성한다(final Long id, final String email, final String nickname) {
        return new Member(id, new Email(email),null,
                 new EncryptedPassword(new Password("password1!")),
                new Nickname(nickname),
                new MemberImage("originalFileName", "default-profile-image", ImageContentType.JPG),
                new MemberProfile(Skill.JAVA));
    }

    private Feed 피드을_생성한다(final String feedTitle, final FeedCategory category) {
        final Feed feed = new Feed(1L, feedTitle, "피드 소개글", 30,
                member, category);

        final FeedTags feedTags = new FeedTags(
                List.of(new FeedTag(1L, new FeedTagName("태그1")),
                        new FeedTag(2L, new FeedTagName("태그2"))));
        feed.addTags(feedTags);

        final FeedContent feedContent = new FeedContent(1L, "피드 본문");
        final FeedNodes feedNodes = new FeedNodes(
                List.of(new FeedNode(1L, "피드 노드1 제목", "피드 노드1 설명")));
        feedContent.addNodes(feedNodes);
        feed.addContent(feedContent);

        return feed;
    }

    private FeedCategory 피드_카테고리를_생성한다(final Long id, final String title) {
        return new FeedCategory(id, title);
    }

    private FeedContent 피드_컨텐츠를_생성한다(final Long id, final String content) {
        return new FeedContent(id, content);
    }

    private List<FeedCategory> 피드_카테고리_리스트를_반환한다() {
        final FeedCategory category1 = new FeedCategory(1L, "교육");
        final FeedCategory category2 = new FeedCategory(2L, "IT");
        final FeedCategory category3 = new FeedCategory(3L, "이커머스");
        final FeedCategory category4 = new FeedCategory(4L, "헬스케어");
        final FeedCategory category5 = new FeedCategory(5L, "게임");
        final FeedCategory category6 = new FeedCategory(6L, "음악");
        final FeedCategory category7 = new FeedCategory(7L, "라이프");
        final FeedCategory category8 = new FeedCategory(8L, "여가");
        final FeedCategory category9 = new FeedCategory(9L, "기타");
        return List.of(category1, category2, category3, category4, category5,
                category6, category7, category8, category9);
    }

    private List<FeedCategoryResponse> 피드_카테고리_응답_리스트를_반환한다() {
        final FeedCategoryResponse category1 = new FeedCategoryResponse(1L, "교육");
        final FeedCategoryResponse category2 = new FeedCategoryResponse(2L, "IT");
        final FeedCategoryResponse category3 = new FeedCategoryResponse(3L, "이커머스");
        final FeedCategoryResponse category4 = new FeedCategoryResponse(4L, "헬스케어");
        final FeedCategoryResponse category5 = new FeedCategoryResponse(5L, "게임");
        final FeedCategoryResponse category6 = new FeedCategoryResponse(6L, "음악");
        final FeedCategoryResponse category7 = new FeedCategoryResponse(7L, "라이프");
        final FeedCategoryResponse category8 = new FeedCategoryResponse(8L, "여가");
        final FeedCategoryResponse category9 = new FeedCategoryResponse(9L, "기타");
        return List.of(category1, category2, category3, category4, category5,
                category6, category7, category8, category9);
    }
}
