package sidepair.domain.project;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import sidepair.domain.BaseUpdatedTimeEntity;
import sidepair.domain.feed.FeedContent;
import sidepair.domain.member.Member;
import sidepair.domain.project.exeption.ProjectException;
import sidepair.domain.project.vo.LimitedMemberCount;
import sidepair.domain.project.vo.ProjectName;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project extends BaseUpdatedTimeEntity {

    private static final int DATE_OFFSET = 1;

    @Embedded
    private final ProjectPendingMembers projectPendingMembers = new ProjectPendingMembers();

    @Embedded
    private final ProjectMembers projectMembers = new ProjectMembers();

    @Embedded
    private final ProjectToDos projectToDos = new ProjectToDos();

    @Embedded
    private final ProjectFeedNodes projectFeedNodes = new ProjectFeedNodes();

    @Embedded
    private ProjectName name;

    @Embedded
    private LimitedMemberCount limitedMemberCount;

    @Enumerated(value = EnumType.STRING)
    private ProjectStatus status = ProjectStatus.RECRUITING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_content_id", nullable = false)
    private FeedContent feedContent;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;


    public Project(final ProjectName name, final LimitedMemberCount limitedMemberCount,
                   final FeedContent feedContent, final Member member) {
        this(null, name, limitedMemberCount, feedContent, member);
    }

    public Project(final Long id, final ProjectName name, final LimitedMemberCount limitedMemberCount,
                   final FeedContent feedContent, final Member member) {
        this.id = id;
        this.name = name;
        this.limitedMemberCount = limitedMemberCount;
        this.feedContent = feedContent;
        updateLeader(member);
    }

    private void updateLeader(final Member member) {
        final ProjectPendingMember leader = new ProjectPendingMember(ProjectRole.LEADER, member);
        leader.initProject(this);
        projectPendingMembers.add(leader);
    }

    public static Project createProject(final ProjectName name, final LimitedMemberCount limitedMemberCount,
                                        final FeedContent feedContent, final Member member) {
        validateFeedCreator(feedContent, member);
        return new Project(name, limitedMemberCount, feedContent, member);
    }

    private static void validateFeedCreator(final FeedContent feedContent, final Member member) {
        if (feedContent.isNotFeedCreator(member)) {
            throw new ProjectException("피드를 생성한 사용자가 아닙니다.");
        }
    }

    public void join(final Member member) {
        final ProjectPendingMember newMember = new ProjectPendingMember(ProjectRole.FOLLOWER, member);
        newMember.initProject(this);
        validateJoinProject(newMember);
        projectPendingMembers.add(newMember);
    }

    private void validateJoinProject(final ProjectPendingMember member) {
        validateMemberCount();
        validateStatus();
        validateAlreadyParticipated(member);
    }

    private void validateMemberCount() {
        if (getCurrentMemberCount() >= limitedMemberCount.getValue()) {
            throw new ProjectException("제한 인원이 꽉 찬 프로젝트에는 멤버를 추가할 수 없습니다.");
        }
    }

    private void validateStatus() {
        if (status != ProjectStatus.RECRUITING) {
            throw new ProjectException("모집 중이지 않은 프로젝트에는 멤버를 추가할 수 없습니다.");
        }
    }

    private void validateAlreadyParticipated(final ProjectPendingMember member) {
        if (projectPendingMembers.containProjectPendingMember(member)) {
            throw new ProjectException("이미 프로젝트에 추가한 멤버는 추가할 수 없습니다.");
        }
    }

    public void start() {
        this.status = ProjectStatus.RUNNING;
    }

    public void complete() {
        this.status = ProjectStatus.COMPLETED;
    }

    public int calculateTotalPeriod() {
        return (int) ChronoUnit.DAYS.between(startDate, endDate) + DATE_OFFSET;
    }

    public int getAllMemoirCount() {
        return projectFeedNodes.calculateAllMemoirCount();
    }

    public boolean isRecruiting() {
        return status == ProjectStatus.RECRUITING;
    }

    public boolean isRunning() {
        return status == ProjectStatus.RUNNING;
    }

    public void addAllProjectFeedNodes(final ProjectFeedNodes projectFeedNodes) {
        checkTotalSize(projectFeedNodes.size() + this.projectFeedNodes.size());
        this.projectFeedNodes.addAll(projectFeedNodes);
        this.startDate = projectFeedNodes.getProjectStartDate();
        this.endDate = projectFeedNodes.getProjectEndDate();
    }

    private void checkTotalSize(final int totalSize) {
        if (totalSize > feedContent.nodesSize()) {
            throw new ProjectException("피드의 노드 수보다 프로젝트의 노드 수가 큽니다.");
        }
    }

    public void addProjectTodo(final ProjectToDo projectToDo) {
        projectToDos.add(projectToDo);
    }

    public Member findProjectLeader() {
        if (status == ProjectStatus.RECRUITING) {
            return projectPendingMembers.findProjectLeader();
        }
        return projectMembers.findProjectLeader();
    }

    public boolean isNotLeader(final Member member) {
        if (status == ProjectStatus.RECRUITING) {
            return projectPendingMembers.isNotLeader(member);
        }
        return projectMembers.isNotLeader(member);
    }

    public boolean isNotPendingLeader(final Member member) {
        return projectPendingMembers.isNotLeader(member);
    }

    public boolean isCompleted() {
        return this.status == ProjectStatus.COMPLETED;
    }

    public ProjectToDo findLastProjectTodo() {
        return projectToDos.findLast();
    }

    public Optional<ProjectFeedNode> findNodeByDate(final LocalDate date) {
        return projectFeedNodes.getNodeByDate(date);
    }

    public Integer getCurrentMemberCount() {
        if (status == ProjectStatus.RECRUITING) {
            return projectPendingMembers.size();
        }
        return projectMembers.size();
    }

    // FIXME 테스트용 메서드
    public void addAllProjectMembers(final List<ProjectMember> members) {
        this.projectMembers.addAll(new ArrayList<>(members));
    }

    public boolean isProjectMember(final Member member) {
        if (status == ProjectStatus.RECRUITING) {
            return projectPendingMembers.isMember(member);
        }
        return projectMembers.isMember(member);
    }

    public void leave(final Member member) {
        if (status == ProjectStatus.RECRUITING) {
            final ProjectPendingMember projectPendingMember = findProjectPendingMemberByMember(member);
            deleteAllIfLeaderLeave(projectPendingMember);
            projectPendingMembers.remove(projectPendingMember);
            return;
        }
        final ProjectMember projectMember = findProjectMemberByMember(member);
        changeRoleIfLeaderLeave(projectMembers, projectMember);
        projectMembers.remove(projectMember);
    }

    public boolean cannotStart() {
        return startDate.isAfter(LocalDate.now());
    }

    private ProjectPendingMember findProjectPendingMemberByMember(final Member member) {
        return projectPendingMembers.findByMember(member)
                .orElseThrow(() -> new ProjectException("프로젝트에 참여한 사용자가 아닙니다. memberId = " + member.getId()));
    }

    private void deleteAllIfLeaderLeave(final ProjectPendingMember projectPendingMember) {
        if (projectPendingMember.isLeader()) {
            deleteAllPendingMembers();
        }
    }

    private ProjectMember findProjectMemberByMember(final Member member) {
        return projectMembers.findByMember(member)
                .orElseThrow(() -> new ProjectException("프로젝트에 참여한 사용자가 아닙니다. memberId = " + member.getId()));
    }

    private void changeRoleIfLeaderLeave(final ProjectMembers projectMembers,
                                         final ProjectMember projectMember) {
        if (projectMember.isLeader()) {
            projectMembers.findNextLeader()
                    .ifPresent(ProjectMember::becomeLeader);
        }
    }

    public boolean isEmptyProject() {
        return projectPendingMembers.isEmpty() && projectMembers.isEmpty();
    }

    public Optional<ProjectToDo> findProjectTodoByTodoId(final Long todoId) {
        return projectToDos.findById(todoId);
    }

    public void deleteAllPendingMembers() {
        projectPendingMembers.deleteAll();
    }

    public boolean isCompletedAfterMonths(final long numberOfMonth) {
        final LocalDate currentDate = LocalDate.now();
        return currentDate.isAfter(endDate.plusMonths(numberOfMonth));
    }

    public ProjectName getName() {
        return name;
    }

    public LimitedMemberCount getLimitedMemberCount() {
        return limitedMemberCount;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public FeedContent getFeedContent() {
        return feedContent;
    }

    public ProjectFeedNodes getProjectFeedNodes() {
        return projectFeedNodes;
    }

    public ProjectToDos getProjectToDos() {
        return projectToDos;
    }

    public ProjectPendingMembers getProjectPendingMembers() {
        return projectPendingMembers;
    }
}
