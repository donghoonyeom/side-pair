package sidepair.integration.helper;

import static sidepair.integration.fixture.AuthenticationAPIFixture.기본_로그인;
import static sidepair.integration.fixture.CommonFixture.BEARER_TOKEN_FORMAT;
import static sidepair.integration.fixture.FeedAPIFixture.카테고리_생성;
import static sidepair.integration.fixture.MemberAPIFixture.기본_회원가입;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import sidepair.domain.feed.FeedCategory;
import sidepair.service.dto.feed.requesst.FeedApplicantSaveRequest;
import sidepair.service.dto.feed.requesst.FeedNodeSaveRequest;
import sidepair.service.dto.feed.requesst.FeedSaveRequest;
import sidepair.service.dto.feed.requesst.FeedTagSaveRequest;

public class InitIntegrationTest extends IntegrationTest {

    protected static Long 기본_회원_아이디;
    protected static String 기본_로그인_토큰;
    protected static String 기본_재발행_토큰;
    protected static FeedCategory 기본_카테고리;
    protected static FeedSaveRequest 기본_피드_생성_요청;
    protected static FeedApplicantSaveRequest 피드_기본_신청서_생성_요청;

    @BeforeEach
    void init() {
        기본_회원_아이디 = 기본_회원가입();
        기본_로그인_토큰 = String.format(BEARER_TOKEN_FORMAT, 기본_로그인().accessToken());
        기본_재발행_토큰 = 기본_로그인().refreshToken();
        기본_카테고리 = 카테고리_생성(기본_로그인_토큰, "이커머스");
        기본_피드_생성_요청 = new FeedSaveRequest(기본_카테고리.getId(), "피드 제목", "피드 소개글",
                "피드 본문",  30,
                List.of(new FeedNodeSaveRequest("feed 1st week", "피드 1주차 내용", null)),
                List.of(new FeedTagSaveRequest("태그1")));
        피드_기본_신청서_생성_요청 = new FeedApplicantSaveRequest("신청서 내용");
    }
}
