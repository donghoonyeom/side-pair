package sidepair.feed.configuration.requesst;

public enum FeedOrderTypeRequest {
    LATEST("최신순");

    private final String description;

    FeedOrderTypeRequest(final String description) {
        this.description = description;
    }
}