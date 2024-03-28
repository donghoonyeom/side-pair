package sidepair.service.dto.mamber.request;

import java.util.Arrays;
import sidepair.service.exception.BadRequestException;


public enum PositionType {
    BACKEND("B"),
    FRONTEND("F"),
    DESIGNER("D"),
    ETC("E");

    private final String oauthPositionType;

    PositionType(final String oauthPositionType) {
        this.oauthPositionType = oauthPositionType;
    }

    public static PositionType findByOauthType(final String oauthPositionType) {
        return Arrays.stream(values())
                .filter(it -> it.oauthPositionType.equals(oauthPositionType))
                .findAny()
                .orElseThrow(() -> new BadRequestException("존재하지 않는 포지션 타입입니다."));
    }
}
