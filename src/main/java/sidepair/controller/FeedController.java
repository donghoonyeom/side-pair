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
import sidepair.common.resolver.MemberIdentifier;
import sidepair.feed.application.FeedCreateService;
import sidepair.feed.application.FeedReadService;
import sidepair.feed.configuration.requesst.FeedCategorySaveRequest;
import sidepair.feed.configuration.requesst.FeedOrderTypeRequest;
import sidepair.feed.configuration.requesst.FeedSaveRequest;
import sidepair.feed.configuration.requesst.FeedSearchRequest;
import sidepair.global.service.dto.CustomScrollRequest;
import sidepair.feed.configuration.response.FeedCategoryResponse;
import sidepair.feed.configuration.response.FeedForListResponses;
import sidepair.global.service.dto.feed.response.FeedResponse;
import sidepair.feed.configuration.response.MemberFeedResponses;

@RestController
@RequestMapping("/feeds")
@RequiredArgsConstructor
public class FeedController {

    private final FeedCreateService feedCreateService;
    private final FeedReadService feedReadService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Authenticated
    public ResponseEntity<Void> create(final FeedSaveRequest request, @MemberIdentifier final String identifier) {
        final Long feedId = feedCreateService.create(request, identifier);
        return ResponseEntity.created(URI.create("/api/feeds/" + feedId)).build();
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
    public ResponseEntity<MemberFeedResponses> findAllMyFeeds(@MemberIdentifier final String identifier,
                                                              @ModelAttribute final CustomScrollRequest scrollRequest) {
        final MemberFeedResponses responses = feedReadService.findAllMemberFeeds(identifier, scrollRequest);
        return ResponseEntity.ok(responses);
    }


    @DeleteMapping("/{feedId}")
    @Authenticated
    public ResponseEntity<Void> deleteFeed(@MemberIdentifier final String identifier,
                                              @PathVariable("feedId") final Long feedId) {
        feedCreateService.deleteFeed(identifier, feedId);
        return ResponseEntity.noContent().build();
    }
}
