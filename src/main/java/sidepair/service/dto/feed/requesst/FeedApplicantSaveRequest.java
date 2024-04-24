package sidepair.service.dto.feed.requesst;

import jakarta.validation.constraints.NotBlank;

public record FeedApplicantSaveRequest(
        @NotBlank(message = "신청서 내용을 작성해주세요")
        String content
) {
}
