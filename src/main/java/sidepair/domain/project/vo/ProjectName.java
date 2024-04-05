package sidepair.domain.project.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import sidepair.domain.project.exeption.ProjectException;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectName {

    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 20;

    @Column(nullable = false, length = 30, name = "name")
    private String value;

    public ProjectName(final String value) {
        validate(value);
        this.value = value;
    }

    private void validate(final String value) {
        if (value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
            throw new ProjectException("프로젝트 이름의 길이가 적절하지 않습니다.");
        }
    }

    public String getValue() {
        return value;
    }
}
