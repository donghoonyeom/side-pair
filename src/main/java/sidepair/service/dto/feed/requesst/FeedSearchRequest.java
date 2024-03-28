package sidepair.service.dto.feed.requesst;

public record FeedSearchRequest(

        String feedTitle,
        String creatorName,
        String tagName
) {
}
