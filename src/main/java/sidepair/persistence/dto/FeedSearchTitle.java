package sidepair.persistence.dto;


import sidepair.service.exception.BadRequestException;

public record FeedSearchTitle(
        String value
) {
    private static final int MIN_LENGTH = 1;

    public FeedSearchTitle(final String value) {
        final String removedBlankTitle = removeBlank(value);
        validateLength(removedBlankTitle);
        this.value = removedBlankTitle;
    }

    private String removeBlank(final String title) {
        return title.replaceAll(" ", "");
    }

    private void validateLength(final String title) {
        if (title.length() < MIN_LENGTH) {
            throw new BadRequestException(
                    String.format("검색어는 최소 %d자부터 가능합니다.", MIN_LENGTH));
        }
    }
}
