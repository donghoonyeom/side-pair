package sidepair.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import sidepair.domain.ImageContentType;
import sidepair.domain.feed.FeedApplicant;
import sidepair.domain.member.vo.MemberImage;
import sidepair.persistence.feed.FeedApplicantRepository;
import sidepair.service.dto.feed.requesst.FeedApplicantSaveRequest;
import sidepair.service.dto.feed.requesst.FeedCategorySaveRequest;
import sidepair.service.dto.feed.requesst.FeedNodeSaveRequest;
import sidepair.service.dto.feed.requesst.FeedSaveRequest;
import sidepair.service.dto.feed.requesst.FeedTagSaveRequest;
import sidepair.domain.feed.Feed;
import sidepair.domain.feed.FeedCategory;
import sidepair.domain.feed.FeedContent;
import sidepair.domain.member.MemberSkill;
import sidepair.domain.member.MemberSkills;
import sidepair.domain.member.Position;
import sidepair.domain.member.vo.SkillName;
import sidepair.service.exception.AuthenticationException;
import sidepair.service.exception.BadRequestException;
import sidepair.service.exception.ConflictException;
import sidepair.service.exception.ForbiddenException;
import sidepair.service.exception.NotFoundException;
import sidepair.domain.member.EncryptedPassword;
import sidepair.domain.member.Member;
import sidepair.domain.member.MemberProfile;
import sidepair.domain.member.vo.Email;
import sidepair.domain.member.vo.Nickname;
import sidepair.domain.member.vo.Password;
import sidepair.persistence.feed.FeedCategoryRepository;
import sidepair.persistence.feed.FeedRepository;
import sidepair.persistence.member.MemberRepository;
import sidepair.service.feed.FeedCreateService;

@ExtendWith(MockitoExtension.class)
class FeedCreateServiceTest {

    private static final Member MEMBER = new Member(1L, new Email("test@email.com"), null,
            new EncryptedPassword(new Password("password123!")), new Nickname("닉네임"),
            null, new MemberProfile(Position.BACKEND),
            new MemberSkills(
                    List.of(new MemberSkill(1L, new SkillName("Java")),
                            new MemberSkill(2L, new SkillName("CSS")))));

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private FeedApplicantRepository feedApplicantRepository;

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private FeedCategoryRepository feedCategoryRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private FeedCreateService feedCreateService;

    @Test
    void 피드를_생성한다() {
        // given
        final String feedTitle = "피드 제목";
        final String feedIntroduction = "피드 소개글";
        final String feedContent = "피드 본문";
        final int requiredPeriod = 30;
        final FeedCategory category = new FeedCategory(1L, "이커머스");

        final List<FeedNodeSaveRequest> feedNodes = List.of(
                new FeedNodeSaveRequest("피드 노드1 제목", "피드 노드1 설명", Collections.emptyList()));
        final List<FeedTagSaveRequest> feedTags = List.of(new FeedTagSaveRequest("태그 1"));
        final FeedSaveRequest request = new FeedSaveRequest(1L, feedTitle, feedIntroduction, feedContent,
                requiredPeriod, feedNodes, feedTags);

        given(feedCategoryRepository.findById(any()))
                .willReturn(Optional.of(category));
        given(feedRepository.save(any()))
                .willReturn(new Feed(1L, feedTitle, feedIntroduction, requiredPeriod, MEMBER, category));
        when(memberRepository.findByEmail(MEMBER.getEmail()))
                .thenReturn(Optional.of(MEMBER));

        // expect
        assertDoesNotThrow(() -> feedCreateService.create(request, "test@email.com"));
    }

    @Test
    void 피드에_대한_신청서를_추가한다() {
        // given
        final Member subscriber = 신청자를_생성한다(2L, "subscriber@example.com", "신청자");
        final FeedCategory category = new FeedCategory(1L, "이커머스");

        final Feed feed = 피드를_생성한다(MEMBER, category);

        when(feedRepository.findById(anyLong()))
                .thenReturn(Optional.of(feed));
        when(feedApplicantRepository.findByFeedAndMember(any(), any()))
                .thenReturn(Optional.empty());
        when(memberRepository.findByEmail(subscriber.getEmail()))
                .thenReturn(Optional.of(subscriber));

        final FeedApplicantSaveRequest feedApplicantSaveRequest = new FeedApplicantSaveRequest("자바 백엔드 개발자 신청합니다.");

        // expected
        assertDoesNotThrow(
                () -> feedCreateService.createApplicant(1L, "subscriber@example.com", feedApplicantSaveRequest));
    }

    @Test
    void 피드_신청서_작성시_존재하지_않는_피드_아이디를_받으면_예외가_발생한다() {
        // given
        when(feedRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        final FeedApplicantSaveRequest feedApplicantSaveRequest = new FeedApplicantSaveRequest("신청서 내용");

        // expected
        assertThatThrownBy(() ->
                feedCreateService.createApplicant(1L, "subscriber@example.com", feedApplicantSaveRequest))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 피드_신청서_작성시_이미_작성을_완료했으면_예외가_발생한다() {
        // given
        final Member subscriber = 신청자를_생성한다(2L, "subscriber@example.com", "신청자");

        final FeedCategory category = new FeedCategory(1L, "이커머스");

        final Feed feed = 피드를_생성한다(MEMBER, category);

        when(feedRepository.findById(anyLong()))
                .thenReturn(Optional.of(feed));
        when(memberRepository.findByEmail(subscriber.getEmail()))
                .thenReturn(Optional.of(subscriber));
        when(feedApplicantRepository.findByFeedAndMember(any(), any()))
                .thenReturn(Optional.of(new FeedApplicant("같이 프로젝트 하고싶어요!", subscriber)));

        final FeedApplicantSaveRequest feedApplicantSaveRequest = new FeedApplicantSaveRequest("프로젝트 신청합니다.");

        // expected
        assertThatThrownBy(
                () -> feedCreateService.createApplicant(1L, "subscriber@example.com", feedApplicantSaveRequest))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void 피드_신청서_작성시_피드_작성자이면_예외가_발생한다() {
        // given
        final FeedCategory category = new FeedCategory(1L, "운동");

        final Feed feed = 피드를_생성한다(MEMBER, category);

        when(feedRepository.findById(anyLong()))
                .thenReturn(Optional.of(feed));
        when(memberRepository.findByEmail(any(Email.class)))
                .thenReturn(Optional.of(MEMBER));

        final FeedApplicantSaveRequest feedApplicantSaveRequest = new FeedApplicantSaveRequest("프로젝트 신청합니다.");

        // expected
        assertThatThrownBy(() -> feedCreateService.createApplicant(1L, "test@example.com", feedApplicantSaveRequest))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void 피드_생성시_존재하지_않는_회원이면_입력하면_예외가_발생한다() {
        // given
        final FeedSaveRequest request = new FeedSaveRequest(10L, "피드 제목", "피드 소개글", "피드 본문", 30,
                List.of(new FeedNodeSaveRequest("피드 노드1", "피드 노드1 설명", Collections.emptyList())),
                List.of(new FeedTagSaveRequest("태그 1")));

        given(memberRepository.findByEmail(any()))
                .willReturn(Optional.empty());

        // expect
        assertThatThrownBy(() -> feedCreateService.create(request, "test@email.com"))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void 피드_생성시_존재하지_않는_카테고리를_입력하면_예외가_발생한다() {
        // given
        final FeedSaveRequest request = new FeedSaveRequest(10L, "피드 제목", "피드 소개글", "피드 본문", 30,
                List.of(new FeedNodeSaveRequest("피드 노드1", "피드 노드1 설명", Collections.emptyList())),
                List.of(new FeedTagSaveRequest("태그 1")));

        given(memberRepository.findByEmail(any()))
                .willReturn(Optional.of(MEMBER));
        given(feedCategoryRepository.findById(any()))
                .willReturn(Optional.empty());

        // expect
        assertThatThrownBy(() -> feedCreateService.create(request, "test@email.com"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 프로젝트_생성된_적이_없는_피드를_삭제한다() {
        // given
        final FeedCategory category = new FeedCategory(1L, "운동");
        final Feed feed = 피드를_생성한다(MEMBER, category);

        when(feedRepository.findById(anyLong()))
                .thenReturn(Optional.of(feed));
        when(feedRepository.findByIdAndMemberEmail(anyLong(), anyString()))
                .thenReturn(Optional.of(feed));

        // when
        // then
        assertDoesNotThrow(() -> feedCreateService.deleteFeed("test@email.com", 1L));
        verify(feedRepository, times(1)).delete(any());
    }

    @Test
    void 피드를_삭제할_때_존재하지_않는_피드인_경우_예외가_발생한다() {
        // given
        when(feedRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> feedCreateService.deleteFeed("test@email.com", 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 피드입니다. feedId = 1");
    }

    @Test
    void 피드를_삭제할_때_자신이_생성한_피드가_아니면_예외가_발생한다() {
        // given
        final FeedCategory category = new FeedCategory(1L, "헬스케어");
        final Feed feed = 피드를_생성한다(MEMBER, category);

        when(feedRepository.findById(anyLong()))
                .thenReturn(Optional.of(feed));
        when(feedRepository.findByIdAndMemberEmail(anyLong(), anyString()))
                .thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> feedCreateService.deleteFeed("test2@email.com", 1L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("해당 피드를 생성한 사용자가 아닙니다.");
    }

    @Test
    void 정상적으로_피드_카테고리를_생성한다() {
        //given
        final FeedCategorySaveRequest category = new FeedCategorySaveRequest("운동");

        when(feedCategoryRepository.findByName(anyString()))
                .thenReturn(Optional.empty());

        //when
        //then
        assertDoesNotThrow(() -> feedCreateService.createFeedCategory(category));
    }

    @Test
    void 피드_카테고리_생성_시_중복될_이름일_경우_예외를_던진다() {
        //given
        final FeedCategorySaveRequest category = new FeedCategorySaveRequest("운동");

        when(feedCategoryRepository.findByName(anyString()))
                .thenReturn(Optional.of(new FeedCategory("운동")));

        //when
        //then
        assertThatThrownBy(() -> feedCreateService.createFeedCategory(category))
                .isInstanceOf(ConflictException.class);
    }

    private Feed 피드를_생성한다(final Member creator, final FeedCategory category) {
        final FeedContent content = new FeedContent("콘텐츠 제목");
        final Feed feed = new Feed("피드 제목", "피드 설명", 100, creator, category);
        feed.addContent(content);
        return feed;
    }

    private Member 신청자를_생성한다(final Long id, final String email, final String nickname) {
        return new Member(id, new Email(email),
                null, new EncryptedPassword(new Password("password1!")),
                new Nickname(nickname),
                new MemberImage("originalFileName", "default-profile-image", ImageContentType.JPG),
                new MemberProfile(Position.BACKEND),
                new MemberSkills(List.of(new MemberSkill(1L, new SkillName("Java")))));
    }
}