package sidepair.common.resolver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Validator;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import sidepair.service.dto.feed.requesst.FeedNodeSaveRequest;
import sidepair.service.dto.feed.requesst.FeedSaveRequest;
import sidepair.service.exception.BadRequestException;

@Component
@RequiredArgsConstructor
public class FeedSaveArgumentResolver implements HandlerMethodArgumentResolver {

    private final ObjectMapper objectMapper;
    private final Validator validator;

    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return parameter.getParameterType().equals(FeedSaveRequest.class);
    }

    @Override
    public Object resolveArgument(final MethodParameter parameter, final ModelAndViewContainer mavContainer,
                                  final NativeWebRequest nativeWebRequest, final WebDataBinderFactory binderFactory)
            throws MethodArgumentNotValidException {
        final HttpServletRequest request = (HttpServletRequest) nativeWebRequest.getNativeRequest();
        checkMultipart(request);
        final MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        final FeedSaveRequest feedSaveRequestNotIncludeImage = makeFeedSaveRequestNotIncludeImage(
                multipartRequest);
        validateRequest(parameter, feedSaveRequestNotIncludeImage);
        return makeFeedSaveRequestIncludeImage(feedSaveRequestNotIncludeImage, multipartRequest);
    }

    private void checkMultipart(final HttpServletRequest request) {
        final MultipartResolver multipartResolver = new StandardServletMultipartResolver();
        if (!multipartResolver.isMultipart(request)) {
            throw new BadRequestException("multipart/form-data 형식으로 들어와야합니다.");
        }
    }

    private FeedSaveRequest makeFeedSaveRequestNotIncludeImage(
            final MultipartHttpServletRequest multipartRequest) {
        final String jsonData = getJsonData(multipartRequest);
        return bindFeedSaveRequest(jsonData);
    }

    private void validateRequest(final MethodParameter parameter, final FeedSaveRequest feedSaveRequest)
            throws MethodArgumentNotValidException {
        final DataBinder binder = new DataBinder(feedSaveRequest);
        binder.setValidator(validator);
        binder.validate();

        if (binder.getBindingResult().hasErrors()) {
            throw new MethodArgumentNotValidException(parameter, binder.getBindingResult());
        }
    }

    private FeedSaveRequest makeFeedSaveRequestIncludeImage(final FeedSaveRequest feedSaveRequest,
                                                            final MultipartHttpServletRequest multipartRequest) {
        for (final FeedNodeSaveRequest feedNode : feedSaveRequest.feedNodes()) {
            final List<MultipartFile> images = multipartRequest.getFiles(feedNode.getTitle());
            feedNode.setImages(images);
        }
        return feedSaveRequest;
    }

    private String getJsonData(final MultipartHttpServletRequest multipartRequest) {
        final String jsonData = multipartRequest.getParameter("jsonData");
        if (jsonData == null) {
            throw new BadRequestException("피드 생성 시 jsonData는 필수입니다.");
        }
        return multipartRequest.getParameter("jsonData");
    }

    private FeedSaveRequest bindFeedSaveRequest(final String jsonData) {
        try {
            return objectMapper.readValue(jsonData, FeedSaveRequest.class);
        } catch (final JsonProcessingException exception) {
            throw new BadRequestException("피드 생성 요청 형식이 틀렸습니다.");
        }
    }
}
