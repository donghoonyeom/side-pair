package sidepair.feed.configuration.response;

import java.util.List;

public record FeedNodeResponse(
        Long id,
        String title,
        String description,
        List<String> imageUrls
) {

}
