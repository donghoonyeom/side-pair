package sidepair.feed.configuration.requesst;

public record FeedSearchRequest(

        String feedTitle,
        String creatorName,
        String tagName
) {
}
