package sidepair.service.dto.project.reequest;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ProjectTodoRequest(
        @NotBlank(message = "투두의 컨텐츠는 빈 값일 수 없습니다.")
        String content,

        @NotNull(message = "프로젝트 투두 시작 날짜는 빈 값일 수 없습니다.")
        @JsonFormat(pattern = "yyyyMMdd")
        LocalDate startDate,

        @NotNull(message = "프로젝트 투두 종료 날짜는 빈 값일 수 없습니다.")
        @JsonFormat(pattern = "yyyyMMdd")
        LocalDate endDate
) {
}
