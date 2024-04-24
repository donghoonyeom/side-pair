package sidepair.domain.feed;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import sidepair.domain.BaseEntity;
import sidepair.domain.ImageContentType;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedNodeImage extends BaseEntity {
    @Column(length = 100, nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String serverFilePath;

    @Enumerated(value = EnumType.STRING)
    @Column(length = 10, nullable = false)
    private ImageContentType imageContentType;

    public FeedNodeImage(final String originalFileName, final String serverFilePath,
                         final ImageContentType imageContentType) {
        this(null, originalFileName, serverFilePath, imageContentType);
    }

    public FeedNodeImage(final Long id, final String originalFileName, final String serverFilePath,
                         final ImageContentType imageContentType) {
        this.id = id;
        this.originalFileName = originalFileName;
        this.serverFilePath = serverFilePath;
        this.imageContentType = imageContentType;
    }

    public String getServerFilePath() {
        return serverFilePath;
    }
}
