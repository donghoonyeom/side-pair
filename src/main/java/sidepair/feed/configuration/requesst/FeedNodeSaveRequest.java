package sidepair.feed.configuration.requesst;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class FeedNodeSaveRequest {
    @NotBlank(message = "게시물 노드의 제목을 입력해주세요.")
    private String title;

    @NotBlank(message = "게시물 노드의 설명을 입력해주세요.")
    private String content;

    private List<MultipartFile> images;

    public void setImages(final List<MultipartFile> images) {
        this.images = images;
    }
}
