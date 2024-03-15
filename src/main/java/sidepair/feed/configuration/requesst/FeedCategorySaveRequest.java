package sidepair.feed.configuration.requesst;

import jakarta.validation.constraints.NotBlank;

public record FeedCategorySaveRequest(

        @NotBlank(message = "카테고리 이름은 빈 값일 수 없습니다.")
        String name
) {
}
