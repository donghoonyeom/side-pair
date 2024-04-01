package sidepair.persistence.auth;

import java.util.Optional;

public interface RefreshTokenRepository {

    void save(final String refreshToken, final String memberEmail);

    Optional<String> findMemberEmailByRefreshToken(final String refreshToken);
}
