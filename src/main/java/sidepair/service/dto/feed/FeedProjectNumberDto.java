package sidepair.service.dto.feed;

public record FeedProjectNumberDto(
        long recruitedProjectNumber,
        long runningProjectNumber,
        long completedProjectNumber
) {

}
