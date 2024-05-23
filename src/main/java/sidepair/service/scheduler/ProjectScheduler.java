package sidepair.service.scheduler;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sidepair.domain.BaseEntity;
import sidepair.domain.project.Project;
import sidepair.domain.project.ProjectMember;
import sidepair.domain.project.ProjectPendingMember;
import sidepair.persistence.project.ProjectMemberRepository;
import sidepair.persistence.project.ProjectPendingMemberRepository;
import sidepair.persistence.project.ProjectRepository;
import sidepair.service.aop.ExceptionConvert;

@Component
@Transactional
@RequiredArgsConstructor
@ExceptionConvert
public class ProjectScheduler {

    private final ProjectRepository projectRepository;
    private final ProjectPendingMemberRepository projectPendingMemberRepository;
    private final ProjectMemberRepository projectMemberRepository;

    @Scheduled(cron = "0 0 0 * * *")
    public void startProjects() {
        final List<Project> projectsToStart = projectRepository.findAllRecruitingProjectsByStartDateEarlierThan(
                LocalDate.now());
        for (final Project project : projectsToStart) {
            final List<ProjectPendingMember> projectPendingMembers = project.getProjectPendingMembers().getValues();
            saveProjectMemberFromPendingMembers(projectPendingMembers);
            project.start();
        }
    }

    private void saveProjectMemberFromPendingMembers(final List<ProjectPendingMember> projectPendingMembers) {
        final List<ProjectMember> projectMembers = makeProjectMembers(projectPendingMembers);
        projectMemberRepository.saveAllInBatch(projectMembers);
        final List<Long> ids = makeProjectPendingMemberIds(projectPendingMembers);
        projectPendingMemberRepository.deleteAllByIdIn(ids);
    }

    private List<ProjectMember> makeProjectMembers(final List<ProjectPendingMember> projectPendingMembers) {
        return projectPendingMembers.stream()
                .map(this::makeProjectMember)
                .toList();
    }

    private ProjectMember makeProjectMember(final ProjectPendingMember projectPendingMember) {
        return new ProjectMember(projectPendingMember.getRole(), projectPendingMember.getJoinedAt(),
                projectPendingMember.getProject(), projectPendingMember.getMember());
    }

    private List<Long> makeProjectPendingMemberIds(final List<ProjectPendingMember> projectPendingMembers) {
        return projectPendingMembers.stream()
                .map(BaseEntity::getId)
                .toList();
    }

    @Scheduled(cron = "0 0 4 * * *")
    public void endProjects() {
        final List<Project> projectsToEnd = projectRepository.findAllByEndDate(LocalDate.now().minusDays(1));
        projectsToEnd.forEach(Project::complete);
    }
}
