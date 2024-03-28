package sidepair.service.dto.feed;

import java.util.List;
import sidepair.service.dto.FileInformation;

public record FeedNodeSaveDto(

        String title,
        String content,
        List<FileInformation> fileInformations
) {
}
