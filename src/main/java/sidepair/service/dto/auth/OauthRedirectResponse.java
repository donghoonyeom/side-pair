package sidepair.service.dto.auth;

public record OauthRedirectResponse(
        String url,
        String state
) {
}
