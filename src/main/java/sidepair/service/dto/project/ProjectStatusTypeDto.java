package sidepair.service.dto.project;

public enum ProjectStatusTypeDto {
    RECRUITING("모집 중"),
    RUNNING("진행 중"),
    COMPLETED("완료");

    private final String description;

    ProjectStatusTypeDto(final String description) {
        this.description = description;
    }
}
