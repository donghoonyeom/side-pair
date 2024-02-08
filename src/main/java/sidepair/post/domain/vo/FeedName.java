package sidepair.post.domain.vo;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import sidepair.post.exception.FeedException;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedName {
    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 40;

    @Column(nullable = false, length = 50, name = "name")
    private String value;

    public FeedName(final String value) {
        validate(value);
        this.value = value;
    }

    private void validate(final String value) {
        if (value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
            throw new FeedException("개시글 이름의 길이가 적절하지 않습니다.");
        }
    }

    public String getValue() {
        return value;
    }
}
