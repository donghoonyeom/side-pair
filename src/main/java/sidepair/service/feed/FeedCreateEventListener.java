package sidepair.service.feed;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import sidepair.domain.feed.Feed;
import sidepair.domain.feed.FeedContent;
import sidepair.domain.feed.FeedNode;
import sidepair.domain.feed.FeedNodeImage;
import sidepair.domain.feed.FeedNodeImages;
import sidepair.service.FilePathGenerator;
import sidepair.service.FileService;
import sidepair.service.ImageDirType;
import sidepair.service.aop.ExceptionConvert;
import sidepair.service.dto.FileInformation;
import sidepair.service.dto.feed.FeedNodeSaveDto;
import sidepair.domain.ImageContentType;
import sidepair.persistence.feed.FeedContentRepository;
import sidepair.service.event.FeedCreateEvent;
import sidepair.service.exception.BadRequestException;
import sidepair.service.exception.ServerException;

@Service
@RequiredArgsConstructor
@ExceptionConvert
public class FeedCreateEventListener {

    private final FeedContentRepository feedContentRepository;
    private final FileService fileService;
    private final FilePathGenerator filePathGenerator;

    @Async
    @TransactionalEventListener
    public void handleFeedCreate(final FeedCreateEvent feedCreateEvent) {
        saveFeedNodeImage(feedCreateEvent);
    }

    private void saveFeedNodeImage(final FeedCreateEvent feedCreateEvent) {
        final FeedContent lastFeedContent = findLastFeedContent(feedCreateEvent.feed());
        for (final FeedNodeSaveDto feedNodeSaveDto : feedCreateEvent.feedSaveDto().feedNodes()) {
            final FeedNode feedNode = findFeedNodeByTitle(lastFeedContent, feedNodeSaveDto);
            final FeedNodeImages feedNodeImages = makeFeedNodeImages(feedNodeSaveDto, feedNode);
            feedNode.addImages(feedNodeImages);
        }
        feedContentRepository.save(lastFeedContent);
    }

    private FeedContent findLastFeedContent(final Feed feed) {
        return feed.findLastFeedContent()
                .orElseThrow(() -> new ServerException("피드 컨텐츠가 존재하지 않습니다."));
    }

    private FeedNode findFeedNodeByTitle(final FeedContent lastFeedContent,
                                         final FeedNodeSaveDto feedNodeSaveDto) {
        return lastFeedContent.findFeedNodeByTitle(feedNodeSaveDto.title())
                .orElseThrow(() -> new BadRequestException(
                        "해당 제목을 가지고있는 피드 노드가 없습니다. title = " + feedNodeSaveDto.title()));
    }

    private FeedNodeImages makeFeedNodeImages(final FeedNodeSaveDto feedNodeSaveDto,
                                              final FeedNode feedNode) {
        final List<FileInformation> fileInformations = feedNodeSaveDto.fileInformations();
        final FeedNodeImages feedNodeImages = new FeedNodeImages();
        for (final FileInformation fileInformation : fileInformations) {
            final FeedNodeImage feedNodeImage = makeFeedNodeImage(fileInformation);
            feedNodeImages.add(feedNodeImage);
            fileService.save(feedNodeImage.getServerFilePath(), fileInformation);
        }
        feedNode.addImages(feedNodeImages);
        return feedNodeImages;
    }

    private FeedNodeImage makeFeedNodeImage(final FileInformation fileInformation) {
        final String originalFileName = findOriginalFileName(fileInformation);
        final ImageContentType imageContentType = ImageContentType.findImageContentType(fileInformation.contentType());
        final String serverFIlePath = filePathGenerator.makeFilePath(ImageDirType.FEED_NODE, originalFileName);
        return new FeedNodeImage(originalFileName, serverFIlePath, imageContentType);
    }

    private String findOriginalFileName(final FileInformation fileInformation) {
        final String originalFilename = fileInformation.originalFileName();
        if (originalFilename == null) {
            throw new BadRequestException("원본 파일의 이름이 존재하지 않습니다.");
        }
        return originalFilename;
    }
}
