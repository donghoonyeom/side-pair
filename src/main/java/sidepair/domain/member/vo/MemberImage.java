package sidepair.domain.member.vo;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import sidepair.domain.BaseEntity;
import sidepair.domain.ImageContentType;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MemberImage extends BaseEntity {

    @Column(length = 100, nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String serverFilePath;

    @Enumerated(value = EnumType.STRING)
    @Column(length = 10, nullable = false)
    private ImageContentType imageContentType;

    public String getServerFilePath() {
        return serverFilePath;
    }
}