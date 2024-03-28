//package sidepair.feed.application;
//
//import java.util.List;
//import lombok.RequiredArgsConstructor;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.transaction.event.TransactionalEventListener;
//import sidepair.service.dto.feed.FeedNodeSaveDto;
//import sidepair.feed.domain.Feed;
//import sidepair.feed.domain.FeedContent;
//import sidepair.feed.domain.FeedNode;
//import sidepair.feed.domain.FeedNodeImage;
//import sidepair.feed.domain.FeedNodeImages;
//import sidepair.domain.ImageContentType;
//import sidepair.global.event.FeedCreateEvent;
//import sidepair.global.service.FilePathGenerator;
//import sidepair.global.service.FileService;
//import sidepair.global.service.ImageDirType;
//import sidepair.global.service.aop.ExceptionConvert;
//import sidepair.global.service.dto.FileInformation;
//import sidepair.global.service.exception.BadRequestException;
//import sidepair.global.service.exception.ServerException;
//import sidepair.persistence.feed.FeedContentRepository;
//
//@Service
//@RequiredArgsConstructor
//@ExceptionConvert
//public class FeedCreateEventListener {
//
//    private final FeedContentRepository feedContentRepository;
//    private final FileService fileService;
//    private final FilePathGenerator filePathGenerator;
//
//    @Async
//    @TransactionalEventListener
//    @Transactional
//    public void handleFeedCreate(final FeedCreateEvent feedCreateEvent) {
//        saveFeedNodeImage(feedCreateEvent);
//    }
//
//    private void saveFeedNodeImage(final FeedCreateEvent feedCreateEvent) {
//        final FeedContent lastFeedContent = findLastFeedContent(feedCreateEvent.feed());
//        for (final FeedNodeSaveDto feedNodeSaveDto : feedCreateEvent.feedSaveDto().feedNodes()) {
//            final FeedNode feedNode = findFeedNodeByTitle(lastFeedContent, feedNodeSaveDto);
//            final FeedNodeImages feedNodeImages = makeFeedNodeImages(feedNodeSaveDto, feedNode);
//            feedNode.addImages(feedNodeImages);
//        }
//        feedContentRepository.save(lastFeedContent);
//    }
//
//    private FeedContent findLastFeedContent(final Feed feed) {
//        return feed.findLastFeedContent()
//                .orElseThrow(() -> new ServerException("로드맵 컨텐츠가 존재하지 않습니다."));
//    }
//
//    private FeedNode findFeedNodeByTitle(final FeedContent lastFeedContent,
//                                               final FeedNodeSaveDto feedNodeSaveDto) {
//        return lastFeedContent.findFeedNodeByTitle(feedNodeSaveDto.title())
//                .orElseThrow(() -> new BadRequestException(
//                        "해당 제목을 가지고있는 개시물 노드가 없습니다. title = " + feedNodeSaveDto.title()));
//    }
//
//    private FeedNodeImages makeFeedNodeImages(final FeedNodeSaveDto feedNodeSaveDto,
//                                                    final FeedNode feedNode) {
//        final List<FileInformation> fileInformations = feedNodeSaveDto.fileInformations();
//        final FeedNodeImages feedNodeImages = new FeedNodeImages();
//        for (final FileInformation fileInformation : fileInformations) {
//            final FeedNodeImage feedNodeImage = makeFeedNodeImage(fileInformation);
//            feedNodeImages.add(feedNodeImage);
//            fileService.save(feedNodeImage.getServerFilePath(), fileInformation);
//        }
//        feedNode.addImages(feedNodeImages);
//        return feedNodeImages;
//    }
//
//    private FeedNodeImage makeFeedNodeImage(final FileInformation fileInformation) {
//        final String originalFileName = findOriginalFileName(fileInformation);
//        final ImageContentType imageContentType = ImageContentType.findImageContentType(fileInformation.contentType());
//        final String serverFIlePath = filePathGenerator.makeFilePath(ImageDirType.FEED_NODE, originalFileName);
//        return new FeedNodeImage(originalFileName, serverFIlePath, imageContentType);
//    }
//
//    private String findOriginalFileName(final FileInformation fileInformation) {
//        final String originalFilename = fileInformation.originalFileName();
//        if (originalFilename == null) {
//            throw new BadRequestException("원본 파일의 이름이 존재하지 않습니다.");
//        }
//        return originalFilename;
//    }
//}
