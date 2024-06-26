package sidepair.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sidepair.common.interceptor.Authenticated;
import sidepair.common.resolver.MemberEmail;
import sidepair.service.dto.feed.requesst.FeedApplicantSaveRequest;
import sidepair.service.dto.feed.response.FeedApplicantResponse;
import sidepair.service.dto.feed.response.FeedProjectResponses;
import sidepair.service.feed.FeedCreateService;
import sidepair.service.feed.FeedReadService;
import sidepair.service.dto.feed.requesst.FeedCategorySaveRequest;
import sidepair.service.dto.feed.requesst.FeedOrderTypeRequest;
import sidepair.service.dto.feed.requesst.FeedSaveRequest;
import sidepair.service.dto.feed.requesst.FeedSearchRequest;
import sidepair.service.dto.CustomScrollRequest;
import sidepair.service.dto.feed.response.FeedCategoryResponse;
import sidepair.service.dto.feed.response.FeedForListResponses;
import sidepair.service.dto.feed.response.FeedResponse;
import sidepair.service.dto.feed.response.MemberFeedResponses;

@RestController
@RequestMapping("/feeds")
@RequiredArgsConstructor
public class FeedController {

    private final FeedCreateService feedCreateService;
    private final FeedReadService feedReadService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Authenticated
    public ResponseEntity<Void> create(final FeedSaveRequest request, @MemberEmail final String email) {
        final Long feedId = feedCreateService.create(request, email);
        return ResponseEntity.created(URI.create("/api/feeds/" + feedId)).build();
    }

    @PostMapping("/{feedId}/applicants")
    @Authenticated
    public ResponseEntity<Void> createApplicant(
            @PathVariable("feedId") final Long feedId,
            @MemberEmail final String email,
            @RequestBody @Valid final FeedApplicantSaveRequest request) {
        final Long applicantId = feedCreateService.createApplicant(feedId, email, request);
        return ResponseEntity.created(URI.create("/api/feeds/" + feedId + "/applicants/" + applicantId)).build();
    }

    @GetMapping("/{feedId}")
    public ResponseEntity<FeedResponse> findFeed(@PathVariable final Long feedId) {
        final FeedResponse response = feedReadService.findFeed(feedId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<FeedForListResponses> findFeedsByOrderType(
            @RequestParam(value = "categoryId", required = false) final Long categoryId,
            @RequestParam(value = "filterCond", required = false) final FeedOrderTypeRequest orderTypeRequest,
            @ModelAttribute @Valid final CustomScrollRequest scrollRequest
    ) {
        final FeedForListResponses feedResponses = feedReadService.findFeedsByOrderType(
                categoryId, orderTypeRequest, scrollRequest);
        return ResponseEntity.ok(feedResponses);
    }

    @GetMapping("/{feedId}/projects")
    public ResponseEntity<FeedProjectResponses> findProjects(@PathVariable final Long feedId) {
        final FeedProjectResponses responses = feedReadService.findFeedProjects(feedId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/search")
    public ResponseEntity<FeedForListResponses> search(
            @RequestParam(value = "filterCond", required = false) final FeedOrderTypeRequest orderTypeRequest,
            @ModelAttribute final FeedSearchRequest searchRequest,
            @ModelAttribute @Valid final CustomScrollRequest scrollRequest
    ) {
        final FeedForListResponses feedResponses = feedReadService.search(
                orderTypeRequest, searchRequest, scrollRequest);
        return ResponseEntity.ok(feedResponses);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<FeedCategoryResponse>> findAllFeedCategories() {
        final List<FeedCategoryResponse> feedCategoryResponses = feedReadService.findAllFeedCategories();
        return ResponseEntity.ok(feedCategoryResponses);
    }

    @PostMapping("/categories")
    public ResponseEntity<Void> createFeedCategory(
            @RequestBody @Valid final FeedCategorySaveRequest feedCategorySaveRequest) {
        feedCreateService.createFeedCategory(feedCategorySaveRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/me")
    @Authenticated
    public ResponseEntity<MemberFeedResponses> findAllMyFeeds(@MemberEmail final String email,
                                                              @ModelAttribute final CustomScrollRequest scrollRequest) {
        final MemberFeedResponses responses = feedReadService.findAllMemberFeeds(email, scrollRequest);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/me/{feedId}/applicants")
    @Authenticated
    public ResponseEntity<List<FeedApplicantResponse>> findFeedApplicants(
            @PathVariable final Long feedId,
            @MemberEmail final String email,
            @ModelAttribute final CustomScrollRequest scrollRequest
    ) {
        final List<FeedApplicantResponse> responses = feedReadService.findFeedApplicants(feedId, email, scrollRequest);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/me/{feedId}/applicants/{applicantId}/join")
    @Authenticated
    public ResponseEntity<Void> joinProject(@MemberEmail final String email,
                                            @PathVariable final Long feedId,
                                            @PathVariable final Long applicantId) {
        feedCreateService.projectJoinPermission(email, feedId, applicantId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/{feedId}")
    @Authenticated
    public ResponseEntity<Void> deleteFeed(@MemberEmail final String email,
                                           @PathVariable("feedId") final Long feedId) {
        feedCreateService.deleteFeed(email, feedId);
        return ResponseEntity.noContent().build();
    }
}
