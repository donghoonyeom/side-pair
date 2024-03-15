package sidepair.feed.configuration.dto;

import java.util.List;
import sidepair.global.service.dto.FileInformation;

public record FeedNodeSaveDto(

        String title,
        String content,
        List<FileInformation> fileInformations
) {
}
