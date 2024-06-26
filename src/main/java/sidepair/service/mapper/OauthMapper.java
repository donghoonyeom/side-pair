package sidepair.service.mapper;

import sidepair.service.dto.auth.OauthRedirectResponse;

public class OauthMapper {

    public static OauthRedirectResponse convertToOauthRedirectDto(final String url, final String state) {
        return new OauthRedirectResponse(url, state);
    }
}
