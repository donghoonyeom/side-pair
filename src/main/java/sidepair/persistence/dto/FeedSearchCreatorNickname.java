package sidepair.persistence.dto;

public record FeedSearchCreatorNickname(
        String value
) {
    public FeedSearchCreatorNickname(final String value) {
        this.value = trim(value);
    }

    private String trim(final String nickname) {
        return nickname.trim();
    }
}
