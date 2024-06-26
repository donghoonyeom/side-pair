package sidepair.persistence.auth;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final Long refreshTokenValidityInSeconds;

    public RefreshTokenRepositoryImpl(final RedisTemplate<String, String> redisTemplate,
                                      @Value("${jwt.refresh-token-validity-in-seconds}") final Long refreshTokenValidityInSeconds) {
        this.redisTemplate = redisTemplate;
        this.refreshTokenValidityInSeconds = refreshTokenValidityInSeconds;
    }

    @Override
    public void save(final String refreshToken, final String memberEmail) {
        final long timeToLiveSeconds = refreshTokenValidityInSeconds / 1000;

        redisTemplate.opsForValue()
                .set(refreshToken, memberEmail, timeToLiveSeconds, TimeUnit.SECONDS);
    }

    @Override
    public Optional<String> findMemberEmailByRefreshToken(final String refreshToken) {
        final String memberEmail = redisTemplate.opsForValue().get(refreshToken);
        if (memberEmail == null) {
            return Optional.empty();
        }
        return Optional.of(memberEmail);
    }
}