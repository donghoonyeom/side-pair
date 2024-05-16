package sidepair.service.dto.project;

public enum ProjectMemberSortTypeDto {
    JOINED_ASC("프로젝트 입장 순 (오래된 순)"),
    JOINED_DESC("프로젝트 입장 순 (최신순)"),
    PARTICIPATION_RATE("회고 작성율 순");

    private final String description;

    ProjectMemberSortTypeDto(final String description) {
        this.description = description;
    }
}
