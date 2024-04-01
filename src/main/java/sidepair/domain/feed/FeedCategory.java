package sidepair.domain.feed;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sidepair.domain.feed.exception.FeedException;
import sidepair.domain.BaseEntity;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class FeedCategory extends BaseEntity {
    private static final int MIN_NAME_LENGTH = 1;
    private static final int MAX_NAME_LENGTH = 10;

    @Column(length = 15, nullable = false)
    private String name;

    public FeedCategory(final String name) {
        this(null, name);
    }

    public FeedCategory(final Long id, final String name) {
        super.id = id;
        final String trimmed = name.trim();
        validateNameLength(trimmed);
        this.name = trimmed;
    }

    private void validateNameLength(final String name) {
        if (name.length() < MIN_NAME_LENGTH || name.length() > MAX_NAME_LENGTH) {
            throw new FeedException("카테고리 이름은 1자 이상 10자 이하입니다.");
        }
    }

    public String getName() {
        return name;
    }
}
