package sidepair.integration.helper;

import static sidepair.integration.fixture.MemberAPIFixture.DEFAULT_PASSWORD;
import static sidepair.integration.fixture.ProjectAPIFixture.십일_후;
import static sidepair.integration.fixture.ProjectAPIFixture.오늘;
import static sidepair.integration.fixture.ProjectAPIFixture.정상적인_프로젝트_이름;
import static sidepair.integration.fixture.ProjectAPIFixture.정상적인_프로젝트_제한_인원;
import static sidepair.integration.fixture.ProjectAPIFixture.정상적인_프로젝트_회고_횟수;
import static sidepair.integration.helper.InitIntegrationTest.기본_로그인_토큰;
import static sidepair.integration.fixture.ProjectAPIFixture.프로젝트를_생성하고_아이디를_반환한다;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import sidepair.domain.member.EncryptedPassword;
import sidepair.domain.member.Member;
import sidepair.domain.member.MemberProfile;
import sidepair.domain.member.MemberSkill;
import sidepair.domain.member.MemberSkills;
import sidepair.domain.member.Position;
import sidepair.domain.member.vo.Email;
import sidepair.domain.member.vo.Nickname;
import sidepair.domain.member.vo.Password;
import sidepair.domain.member.vo.SkillName;
import sidepair.domain.project.Project;
import sidepair.domain.project.ProjectMember;
import sidepair.domain.project.ProjectRole;
import sidepair.domain.project.ProjectStatus;
import sidepair.persistence.project.ProjectMemberRepository;
import sidepair.persistence.project.ProjectRepository;
import sidepair.service.dto.feed.response.FeedResponse;
import sidepair.service.dto.mamber.response.MemberInformationResponse;
import sidepair.service.dto.project.request.ProjectCreateRequest;
import sidepair.service.dto.project.request.ProjectFeedNodeRequest;

@Transactional
public class TestTransactionService {

    @PersistenceContext
    private EntityManager em;

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public TestTransactionService(final ProjectRepository projectRepository,
                                  final ProjectMemberRepository projectMemberRepository) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    public Project 프로젝트를_완료시킨다(final Long 프로젝트_아이디) {
        final Project 프로젝트 = projectRepository.findById(프로젝트_아이디).get();
        프로젝트.complete();
        return projectRepository.save(프로젝트);
    }

    public Project 완료한_프로젝트를_생성한다(final FeedResponse 피드_응답) {
        final List<ProjectFeedNodeRequest> 프로젝트_노드_별_기간_요청 = List.of(
                new ProjectFeedNodeRequest(피드_응답.content().nodes().get(0).id(), 정상적인_프로젝트_회고_횟수, 오늘, 십일_후));
        final ProjectCreateRequest 프로젝트_생성_요청 = new ProjectCreateRequest(피드_응답.feedId(), 정상적인_프로젝트_이름, 정상적인_프로젝트_제한_인원,
                프로젝트_노드_별_기간_요청);
        final Long 프로젝트_아이디 = 프로젝트를_생성하고_아이디를_반환한다(프로젝트_생성_요청, 기본_로그인_토큰);
        return 프로젝트를_완료시킨다(프로젝트_아이디);
    }

    public void 프로젝트에_대한_참여자_리스트를_생성한다(final MemberInformationResponse 리더_정보, final Project 프로젝트,
                                     final MemberInformationResponse... 팔로워들_정보) {
        final Member 리더 = 사용자_정보에서_사용자를_생성한다(리더_정보);
        final ProjectMember 프로젝트_멤버_리더 = new ProjectMember(ProjectRole.LEADER,
                LocalDateTime.of(2023, 7, 1, 12, 0), 프로젝트, 리더);
        final List<ProjectMember> 프로젝트_멤버_리스트 = new ArrayList<>();
        프로젝트_멤버_리스트.add(프로젝트_멤버_리더);

        for (final MemberInformationResponse 팔로워_정보 : 팔로워들_정보) {
            final Member 팔로워 = 사용자_정보에서_사용자를_생성한다(팔로워_정보);
            final ProjectMember 프로젝트_멤버_팔로워 = new ProjectMember(ProjectRole.FOLLOWER,
                    LocalDateTime.of(2023, 7, 5, 18, 0), 프로젝트, 팔로워);
            프로젝트_멤버_리스트.add(프로젝트_멤버_팔로워);
        }
        프로젝트_멤버를_저장한다(프로젝트_멤버_리스트);
    }

    private Member 사용자_정보에서_사용자를_생성한다(final MemberInformationResponse 사용자_정보) {
        final MemberProfile memberProfile = new MemberProfile(Position.valueOf(사용자_정보.position()));
        final MemberSkills skills = new MemberSkills (List.of(new MemberSkill(1L, new SkillName("Java"))));
        return new Member(사용자_정보.id(), new Email(사용자_정보.email()), null, new EncryptedPassword(new Password(
                DEFAULT_PASSWORD)), new Nickname(사용자_정보.nickname()), null, memberProfile, skills);
    }

    public void 프로젝트_멤버를_저장한다(final List<ProjectMember> 프로젝트_멤버_리스트) {
        projectMemberRepository.saveAllInBatch(프로젝트_멤버_리스트);
    }

    public void 프로젝트의_상태와_종료날짜를_변경한다(final Long 프로젝트_아이디, final ProjectStatus 프로젝트_상태, final LocalDate 변경할_종료날짜) {
        em.createQuery("update Project g set endDate = :endDate, status = :status where id = :projectId")
                .setParameter("status", 프로젝트_상태)
                .setParameter("endDate", 변경할_종료날짜)
                .setParameter("projectId", 프로젝트_아이디)
                .executeUpdate();
    }

    public void 프로젝트의_종료날짜를_변경한다(final Long 프로젝트_아이디, final LocalDate 변경할_종료날짜) {
        em.createQuery("update Project g set endDate = :endDate where id = :projectId")
                .setParameter("projectId", 프로젝트_아이디)
                .setParameter("endDate", 변경할_종료날짜)
                .executeUpdate();
    }

    public void 프로젝트의_시작날짜를_변경한다(final Long 프로젝트_아이디, final LocalDate 변경할_시작날짜) {
        em.createQuery("update Project g set startDate = :startDate where id = :projectId")
                .setParameter("projectId", 프로젝트_아이디)
                .setParameter("startDate", 변경할_시작날짜)
                .executeUpdate();
    }
}
