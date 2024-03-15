package sidepair.persistence.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class FeedSearchDto {

    private final FeedSearchCreatorNickname creatorName;
    private final FeedSearchTitle title;
    private final FeedSearchTagName tagName;

    public static FeedSearchDto create(final String creatorName, final String title, final String tagName) {
        if (creatorName != null) {
            return new FeedSearchDto(new FeedSearchCreatorNickname(creatorName), null, null);
        }
        if (title != null) {
            return new FeedSearchDto(null, new FeedSearchTitle(title), null);
        }
        if (tagName != null) {
            return new FeedSearchDto(null, null, new FeedSearchTagName(tagName));
        }
        return new FeedSearchDto(null, null, null);
    }
}
