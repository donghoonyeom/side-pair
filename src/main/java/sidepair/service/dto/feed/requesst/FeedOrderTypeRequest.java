package sidepair.service.dto.feed.requesst;

public enum FeedOrderTypeRequest {
    LATEST("최신순"),
    APPLICANT_COUNT("신청서 많은순");

    private final String description;

    FeedOrderTypeRequest(final String description) {
        this.description = description;
    }
}