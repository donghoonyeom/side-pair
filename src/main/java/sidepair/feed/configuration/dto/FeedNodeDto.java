package sidepair.feed.configuration.dto;

import java.util.List;

public record FeedNodeDto(

        Long id,
        String title,
        String description,
        List<String> imageUrls
) {
}
