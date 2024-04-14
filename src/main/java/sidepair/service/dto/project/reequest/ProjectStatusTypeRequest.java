package sidepair.service.dto.project.reequest;

public enum ProjectStatusTypeRequest {
    RECRUITING("모집 중"),
    RUNNING("진행 중"),
    COMPLETED("완료");

    private final String description;

    ProjectStatusTypeRequest(final String description) {
        this.description = description;
    }
}
