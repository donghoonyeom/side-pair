package sidepair.service.dto.project.reequest;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ProjectFeedNodeRequest(
        @NotNull(message = "피드 노드 아이디는 빈 값일 수 없습니다.")
        Long feedNodeId,

        @NotNull(message = "인증 횟수는 빈 값일 수 없습니다.")
        Integer checkCount,

        @NotNull(message = "피드 노드 시작 날짜는 빈 값일 수 없습니다.")
        @JsonFormat(pattern = "yyyyMMdd")
        LocalDate startDate,

        @NotNull(message = "피드 노드 종료 날짜는 빈 값일 수 없습니다.")
        @JsonFormat(pattern = "yyyyMMdd")
        LocalDate endDate
) {

}
