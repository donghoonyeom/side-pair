package sidepair.service.dto.feed;

import java.util.List;

public record FeedNodeDto(

        Long id,
        String title,
        String description,
        List<String> imageUrls
) {
}
