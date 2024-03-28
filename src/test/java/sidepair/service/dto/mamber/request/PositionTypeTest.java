package sidepair.service.dto.mamber.request;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PositionTypeTest {

    @Test
    void oauthPositionType이_B인_경우_BACKEND을_반환한다() {
        //given
        final String oauthPositionType = "B";

        //when
        final PositionType positionType = PositionType.findByOauthType(oauthPositionType);

        //then
        assertThat(positionType).isEqualTo(PositionType.BACKEND);
    }

    @Test
    void oauthPositionType이_F인_경우_FRONTEND을_반환한다() {
        //given
        final String oauthPositionType = "F";

        //when
        final PositionType positionType = PositionType.findByOauthType(oauthPositionType);

        //then
        assertThat(positionType).isEqualTo(PositionType.FRONTEND);
    }

    @Test
    void oauthPositionType이_D인_경우_DESIGNER을_반환한다() {
        //given
        final String oauthPositionType = "D";

        //when
        final PositionType positionType = PositionType.findByOauthType(oauthPositionType);

        //then
        assertThat(positionType).isEqualTo(PositionType.DESIGNER);
    }

    @Test
    void oauthPositionType이_E인_경우_ETC을_반환한다() {
        //given
        final String oauthPositionType = "E";

        //when
        final PositionType positionType = PositionType.findByOauthType(oauthPositionType);

        //then
        assertThat(positionType).isEqualTo(PositionType.ETC);
    }
}
