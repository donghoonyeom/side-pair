package sidepair.domain.feed;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import sidepair.domain.feed.vo.FeedTagName;
import sidepair.domain.BaseEntity;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedTag extends BaseEntity {
    @Embedded
    private FeedTagName name;

    public FeedTag(final FeedTagName name) {
        this.name = name;
    }

    public FeedTag(final Long id, final FeedTagName name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final FeedTag that = (FeedTag) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getId(), getName());
    }

    @Override
    public Long getId() {
        return id;
    }

    public FeedTagName getName() {
        return name;
    }
}
