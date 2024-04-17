package sidepair.service.dto.project;

public enum ProjectFilterTypeDto{
        LATEST("최신순"),
        PARTICIPATION_RATE("참가율 순");

        private final String description;

        ProjectFilterTypeDto(final String description) {
                this.description = description;
        }
}
