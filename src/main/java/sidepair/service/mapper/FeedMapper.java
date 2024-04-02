package sidepair.service.mapper;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import sidepair.domain.member.Member;
import sidepair.service.dto.feed.FeedApplicantDto;
import sidepair.service.dto.feed.FeedApplicantReadDto;
import sidepair.service.dto.feed.FeedNodeDto;
import sidepair.service.dto.feed.FeedNodeSaveDto;
import sidepair.service.dto.feed.FeedSaveDto;
import sidepair.service.dto.feed.FeedTagDto;
import sidepair.service.dto.feed.FeedTagSaveDto;
import sidepair.service.dto.feed.requesst.FeedApplicantSaveRequest;
import sidepair.service.dto.feed.requesst.FeedCategorySaveRequest;
import sidepair.service.dto.feed.requesst.FeedNodeSaveRequest;
import sidepair.service.dto.feed.requesst.FeedOrderTypeRequest;
import sidepair.service.dto.feed.requesst.FeedSaveRequest;
import sidepair.service.dto.feed.requesst.FeedTagSaveRequest;
import sidepair.domain.feed.Feed;
import sidepair.domain.feed.FeedCategory;
import sidepair.service.dto.FileInformation;
import sidepair.service.dto.feed.FeedCategoryDto;
import sidepair.service.dto.feed.FeedContentDto;
import sidepair.service.dto.feed.FeedDto;
import sidepair.service.dto.feed.FeedForListDto;
import sidepair.service.dto.feed.FeedForListScrollDto;
import sidepair.service.dto.feed.response.FeedApplicantResponse;
import sidepair.service.dto.feed.response.FeedCategoryResponse;
import sidepair.service.dto.feed.response.FeedContentResponse;
import sidepair.service.dto.feed.response.FeedForListResponse;
import sidepair.service.dto.feed.response.FeedForListResponses;
import sidepair.service.dto.feed.response.FeedNodeResponse;
import sidepair.service.dto.feed.response.FeedResponse;
import sidepair.service.dto.feed.response.FeedTagResponse;
import sidepair.service.dto.feed.response.MemberFeedResponse;
import sidepair.service.dto.feed.response.MemberFeedResponses;
import sidepair.service.dto.mamber.MemberDto;
import sidepair.service.dto.mamber.response.MemberResponse;
import sidepair.service.exception.ServerException;
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

    public static FeedApplicantDto convertFeedApplicantDto(final FeedApplicantSaveRequest request,
                                                           final Member member) {
        return new FeedApplicantDto(request.content(), member);
    }

    public static List<FeedApplicantResponse> convertToFeedApplicantResponses(
            final List<FeedApplicantReadDto> feedApplicantReadDtos) {
        return feedApplicantReadDtos.stream()
                .map(FeedMapper::convertToFeedApplicantResponse)
                .toList();
    }

    private static FeedApplicantResponse convertToFeedApplicantResponse(
            final FeedApplicantReadDto feedApplicantReadDto) {
        final MemberDto memberDto = feedApplicantReadDto.member();
        return new FeedApplicantResponse(feedApplicantReadDto.id(),
                new MemberResponse(memberDto.id(), memberDto.name(), memberDto.imageUrl()),
                feedApplicantReadDto.createdAt(), feedApplicantReadDto.content());
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
