package sidepair.feed.configuration.requesst;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record FeedSaveRequest(

        @NotNull(message = "카테고리를 입력해주세요.")
        Long categoryId,

        @NotBlank(message = "게시물의 제목을 입력해주세요.")
        String title,

        @NotBlank(message = "프로젝트의 소개글을 입력해주세요.")
        String introduction,

        String content,

        @NotNull(message = "예상 소요 기간을 입력해주세요.")
        Integer requiredPeriod,

        @NotEmpty(message = "프로젝트의 첫 번째 단계를 입력해주세요.")
        List<@Valid FeedNodeSaveRequest> feedNodes,

        List<FeedTagSaveRequest> feedTags
) {
}
