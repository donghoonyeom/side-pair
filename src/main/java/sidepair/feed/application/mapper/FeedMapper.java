package sidepair.feed.application.mapper;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import sidepair.feed.configuration.dto.FeedNodeDto;
import sidepair.feed.configuration.dto.FeedNodeSaveDto;
import sidepair.feed.configuration.dto.FeedSaveDto;
import sidepair.feed.configuration.dto.FeedTagDto;
import sidepair.feed.configuration.dto.FeedTagSaveDto;
import sidepair.feed.configuration.requesst.FeedCategorySaveRequest;
import sidepair.feed.configuration.requesst.FeedNodeSaveRequest;
import sidepair.feed.configuration.requesst.FeedOrderTypeRequest;
import sidepair.feed.configuration.requesst.FeedSaveRequest;
import sidepair.feed.configuration.requesst.FeedTagSaveRequest;
import sidepair.feed.domain.Feed;
import sidepair.feed.domain.FeedCategory;
import sidepair.global.service.dto.FileInformation;
import sidepair.feed.configuration.dto.FeedCategoryDto;
import sidepair.feed.configuration.dto.FeedContentDto;
import sidepair.global.service.dto.feed.FeedDto;
import sidepair.feed.configuration.dto.FeedForListDto;
import sidepair.feed.configuration.dto.FeedForListScrollDto;
import sidepair.feed.configuration.response.FeedCategoryResponse;
import sidepair.feed.configuration.response.FeedContentResponse;
import sidepair.feed.configuration.response.FeedForListResponse;
import sidepair.feed.configuration.response.FeedForListResponses;
import sidepair.feed.configuration.response.FeedNodeResponse;
import sidepair.global.service.dto.feed.response.FeedResponse;
import sidepair.feed.configuration.response.FeedTagResponse;
import sidepair.feed.configuration.response.MemberFeedResponse;
import sidepair.feed.configuration.response.MemberFeedResponses;
import sidepair.global.service.dto.mamber.MemberDto;
import sidepair.global.service.dto.mamber.response.MemberResponse;
import sidepair.global.service.exception.ServerException;
import sidepair.global.service.mapper.ScrollResponseMapper;
import sidepair.persistence.dto.FeedOrderType;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FeedMapper {

    public static FeedSaveDto convertToFeedSaveDto(final FeedSaveRequest request) {
        final List<FeedNodeSaveDto> feedNodes = request.feedNodes()
                .stream()
                .map(FeedMapper::convertToFeedNodesSaveDto)
                .toList();
        final List<FeedTagSaveDto> feedTags = convertToFeedTagSaveDtos(request);

        return new FeedSaveDto(request.categoryId(), request.title(), request.introduction(), request.content(),
                request.requiredPeriod(), feedNodes, feedTags);
    }

    private static List<FeedTagSaveDto> convertToFeedTagSaveDtos(final FeedSaveRequest request) {
        if (request.feedTags() == null) {
            return Collections.emptyList();
        }
        return request.feedTags()
                .stream()
                .map(FeedMapper::convertToFeedTagSaveDto)
                .toList();
    }

    private static FeedNodeSaveDto convertToFeedNodesSaveDto(final FeedNodeSaveRequest request) {
        final List<FileInformation> fileInformations = request.getImages()
                .stream()
                .map(FeedMapper::convertToFeedNodeImageDto)
                .toList();
        return new FeedNodeSaveDto(request.getTitle(), request.getContent(), fileInformations);
    }

    private static FileInformation convertToFeedNodeImageDto(final MultipartFile it) {
        try {
            return new FileInformation(it.getOriginalFilename(), it.getSize(), it.getContentType(),
                    it.getInputStream());
        } catch (final IOException exception) {
            throw new ServerException(exception.getMessage());
        }
    }

    private static FeedTagSaveDto convertToFeedTagSaveDto(final FeedTagSaveRequest request) {
        return new FeedTagSaveDto(request.name());
    }

    public static FeedResponse convertToFeedResponse(final FeedDto feedDto) {
        return new FeedResponse(
                feedDto.feedId(),
                new FeedCategoryResponse(feedDto.category().id(), feedDto.category().name()),
                feedDto.feedTitle(),
                feedDto.introduction(),
                new MemberResponse(feedDto.creator().id(), feedDto.creator().name(),
                        feedDto.creator().imageUrl()),
                convertToFeedContentResponse(feedDto.content()),
                feedDto.recommendedFeedPeriod(),
                feedDto.createdAt(),
                convertFeedTagResponses(feedDto.tags())
        );
    }

    private static List<FeedTagResponse> convertFeedTagResponses(final List<FeedTagDto> feedTagDtos) {
        return feedTagDtos.stream()
                .map(tag -> new FeedTagResponse(tag.id(), tag.name()))
                .toList();
    }

    public static FeedForListResponses convertFeedResponses(
            final FeedForListScrollDto feedForListScrollDto) {
        final List<FeedForListResponse> responses = feedForListScrollDto.dtos()
                .stream()
                .map(FeedMapper::convertFeedResponse)
                .toList();
        return new FeedForListResponses(responses, feedForListScrollDto.hasNext());
    }

    private static FeedForListResponse convertFeedResponse(final FeedForListDto feedForListDto) {
        final FeedCategoryDto feedCategoryDto = feedForListDto.category();
        final FeedCategoryResponse categoryResponse = new FeedCategoryResponse(feedCategoryDto.id(),
                feedCategoryDto.name());
        final MemberDto memberDto = feedForListDto.creator();
        final MemberResponse creatorResponse = new MemberResponse(memberDto.id(), memberDto.name(),
                memberDto.imageUrl());
        final List<FeedTagResponse> feedTagResponses = convertFeedTagResponses(feedForListDto.tags());

        return new FeedForListResponse(
                feedForListDto.feedId(),
                feedForListDto.feedTitle(),
                feedForListDto.introduction(),
                feedForListDto.recommendedFeedPeriod(),
                feedForListDto.createdAt(),
                creatorResponse,
                categoryResponse,
                feedTagResponses
        );
    }

    private static FeedContentResponse convertToFeedContentResponse(final FeedContentDto feedContentDto) {
        return new FeedContentResponse(
                feedContentDto.id(),
                feedContentDto.content(),
                convertFeedNodeResponse(feedContentDto.nodes())
        );
    }

    private static List<FeedNodeResponse> convertFeedNodeResponse(final List<FeedNodeDto> feedNodeDtos) {
        return feedNodeDtos.stream()
                .map(it -> new FeedNodeResponse(it.id(), it.title(), it.description(), it.imageUrls()))
                .toList();
    }

    public static FeedOrderType convertFeedOrderType(final FeedOrderTypeRequest filterType) {
        if (filterType == null) {
            return FeedOrderType.LATEST;
        }
        return FeedOrderType.valueOf(filterType.name());
    }

    public static List<FeedCategoryResponse> convertFeedCategoryResponses(
            final List<FeedCategory> feedCategories) {
        return feedCategories.stream()
                .map(category -> new FeedCategoryResponse(category.getId(), category.getName()))
                .toList();
    }

    public static MemberFeedResponses convertMemberFeedResponses(final List<Feed> feeds,
                                                                       final int requestSize) {
        final List<MemberFeedResponse> responses = feeds.stream()
                .map(FeedMapper::convertMemberFeedResponse)
                .toList();

        final List<MemberFeedResponse> subResponses = ScrollResponseMapper.getSubResponses(responses, requestSize);
        final boolean hasNext = ScrollResponseMapper.hasNext(responses.size(), requestSize);
        return new MemberFeedResponses(subResponses, hasNext);
    }

    private static MemberFeedResponse convertMemberFeedResponse(final Feed feed) {
        final FeedCategory category = feed.getCategory();
        return new MemberFeedResponse(feed.getId(), feed.getTitle(), feed.getCreatedAt(),
                new FeedCategoryResponse(category.getId(), category.getName()));
    }

    public static FeedCategory convertToFeedCategory(final FeedCategorySaveRequest request) {
        return new FeedCategory(request.name());
    }
}
