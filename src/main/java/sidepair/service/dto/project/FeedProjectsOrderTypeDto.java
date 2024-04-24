package sidepair.service.dto.project;

public enum FeedProjectsOrderTypeDto {
    LATEST("최신순");

    private final String description;

    FeedProjectsOrderTypeDto(final String description) {
        this.description = description;
    }
}
