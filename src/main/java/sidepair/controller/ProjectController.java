package sidepair.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sidepair.common.interceptor.Authenticated;
import sidepair.common.resolver.MemberEmail;
import sidepair.service.dto.mamber.response.MemberProjectForListResponse;
import sidepair.service.dto.mamber.response.MemberProjectResponse;
import sidepair.service.dto.project.ProjectMemberSortTypeDto;
import sidepair.service.dto.project.request.MemoirRequest;
import sidepair.service.dto.project.request.ProjectCreateRequest;
import sidepair.service.dto.project.request.ProjectStatusTypeRequest;
import sidepair.service.dto.project.request.ProjectTodoRequest;
import sidepair.service.dto.project.response.ProjectCertifiedResponse;
import sidepair.service.dto.project.response.ProjectMemoirResponse;
import sidepair.service.dto.project.response.ProjectFeedNodeDetailResponse;
import sidepair.service.dto.project.response.ProjectMemberResponse;
import sidepair.service.dto.project.response.ProjectResponse;
import sidepair.service.dto.project.response.ProjectToDoCheckResponse;
import sidepair.service.dto.project.response.ProjectTodoResponse;
import sidepair.service.project.ProjectCreateService;
import sidepair.service.project.ProjectReadService;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectCreateService projectCreateService;
    private final ProjectReadService projectReadService;

    @PostMapping
    @Authenticated
    public ResponseEntity<Void> create(@RequestBody @Valid final ProjectCreateRequest request,
                                       @MemberEmail final String email) {
        final Long id = projectCreateService.create(request, email);
        return ResponseEntity.created(URI.create("/api/projects/" + id)).build();
    }

    @PostMapping("/{projectId}/todos")
    @Authenticated
    public ResponseEntity<Void> addTodo(@RequestBody @Valid final ProjectTodoRequest projectTodoRequest,
                                        @PathVariable final Long projectId,
                                        @MemberEmail final String email) {
        final Long id = projectCreateService.addProjectTodo(projectId, email, projectTodoRequest);
        return ResponseEntity.created(URI.create("/api/projects/" + projectId + "/todos/" + id)).build();
    }

    @PostMapping("/{projectId}/memoirs")
    @Authenticated
    public ResponseEntity<Void> createMemoir(@RequestBody @Valid final MemoirRequest memoirRequest,
                                             @MemberEmail final String email,
                                             @PathVariable("projectId") final Long projectId) {
        final Long id = projectCreateService.createMemoir(email, projectId, memoirRequest);
        return ResponseEntity.created(URI.create("/api/project/" + projectId + "/memoirs/" + id)).build();
    }

    @PostMapping("/{projectId}/leave")
    @Authenticated
    public ResponseEntity<Void> leave(@MemberEmail final String email,
                                      @PathVariable("projectId") final Long projectId) {
        projectCreateService.leave(email, projectId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{projectId}/start")
    @Authenticated
    public ResponseEntity<Void> start(@MemberEmail final String email,
                                      @PathVariable("projectId") final Long projectId) {
        projectCreateService.startProject(email, projectId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/{projectId}", headers = "Authorization")
    @Authenticated
    public ResponseEntity<ProjectCertifiedResponse> findProject(@MemberEmail final String email,
                                                                @PathVariable("projectId") final Long projectId) {
        final ProjectCertifiedResponse projectResponse = projectReadService.findProject(email, projectId);
        return ResponseEntity.ok(projectResponse);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> findProject(@PathVariable("projectId") final Long projectId) {
        final ProjectResponse projectResponse = projectReadService.findProject(projectId);
        return ResponseEntity.ok(projectResponse);
    }

    @GetMapping("/{projectId}/members")
    @Authenticated
    public ResponseEntity<List<ProjectMemberResponse>> findProjectMembers(
            @PathVariable final Long projectId,
            @RequestParam(value = "sortCond", required = false) final ProjectMemberSortTypeDto sortType) {
        final List<ProjectMemberResponse> projectMembers = projectReadService.findProjectMembers(projectId,
                sortType);
        return ResponseEntity.ok(projectMembers);
    }

    @GetMapping("/{projectId}/me")
    @Authenticated
    public ResponseEntity<MemberProjectResponse> findMemberProject(
            @MemberEmail final String email, @PathVariable final Long projectId) {
        final MemberProjectResponse memberProjectResponse = projectReadService.findMemberProject(email,
                projectId);
        return ResponseEntity.ok(memberProjectResponse);
    }

    @GetMapping("/me")
    @Authenticated
    public ResponseEntity<List<MemberProjectForListResponse>> findMemberProjectsByStatus(
            @MemberEmail final String email,
            @RequestParam(value = "statusCond", required = false) final ProjectStatusTypeRequest projectStatusTypeRequest) {
        if (projectStatusTypeRequest == null) {
            final List<MemberProjectForListResponse> memberProjectForListResponses =
                    projectReadService.findMemberProjects(email);
            return ResponseEntity.ok(memberProjectForListResponses);
        }
        final List<MemberProjectForListResponse> memberProjectForListResponses =
                projectReadService.findMemberProjectsByStatusType(email, projectStatusTypeRequest);
        return ResponseEntity.ok(memberProjectForListResponses);
    }

    @GetMapping("/{projectId}/todos")
    @Authenticated
    public ResponseEntity<List<ProjectTodoResponse>> findAllTodos(
            @PathVariable final Long projectId,
            @MemberEmail final String email) {
        final List<ProjectTodoResponse> todoResponses = projectReadService.findAllProjectTodo(projectId,
                email);
        return ResponseEntity.ok(todoResponses);
    }

    @PostMapping("/{projectId}/todos/{todoId}")
    @Authenticated
    public ResponseEntity<ProjectToDoCheckResponse> checkTodo(@PathVariable final Long projectId,
                                                              @PathVariable final Long todoId,
                                                              @MemberEmail final String email) {
        final ProjectToDoCheckResponse checkResponse = projectCreateService.checkProjectTodo(projectId, todoId,
                email);
        return ResponseEntity.ok(checkResponse);
    }

    @GetMapping("/{projectId}/nodes")
    @Authenticated
    public ResponseEntity<List<ProjectFeedNodeDetailResponse>> findAllNodes(
            @PathVariable final Long projectId,
            @MemberEmail final String email
    ) {
        final List<ProjectFeedNodeDetailResponse> nodeResponses = projectReadService.findAllProjectNodes(
                projectId, email);
        return ResponseEntity.ok(nodeResponses);
    }

    @GetMapping("/{projectId}/memoirs")
    @Authenticated
    public ResponseEntity<List<ProjectMemoirResponse>> findProjectMemoirs(
            @MemberEmail final String email,
            @PathVariable("projectId") final Long projectId) {
        final List<ProjectMemoirResponse> response = projectReadService.findProjectMemoirs(
                email, projectId);
        return ResponseEntity.ok(response);
    }
}
