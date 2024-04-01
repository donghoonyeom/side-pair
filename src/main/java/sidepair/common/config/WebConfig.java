package sidepair.common.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import sidepair.common.interceptor.AuthInterceptor;
import sidepair.common.resolver.FeedSaveArgumentResolver;
import sidepair.common.resolver.MemberEmailArgumentResolver;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final MemberEmailArgumentResolver memberEmailArgumentResolver;
    private final FeedSaveArgumentResolver feedSaveArgumentResolver;

    @Override
    public void addInterceptors(final InterceptorRegistry interceptorRegistry) {
        interceptorRegistry.addInterceptor(authInterceptor);
    }

    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(feedSaveArgumentResolver);
        argumentResolvers.add(memberEmailArgumentResolver);
    }
}
