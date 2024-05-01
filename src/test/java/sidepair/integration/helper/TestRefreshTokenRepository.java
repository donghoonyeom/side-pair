package sidepair.integration.helper;

import static sidepair.integration.fixture.MemberAPIFixture.DEFAULT_EMAIL;

import java.util.Optional;
import sidepair.persistence.auth.RefreshTokenRepository;

public class TestRefreshTokenRepository implements RefreshTokenRepository {

    @Override
    public void save(final String refreshToken, final String memberEmail) {
    }

    @Override
    public Optional<String> findMemberEmailByRefreshToken(final String refreshToken) {
        return Optional.of(DEFAULT_EMAIL);
    }
}