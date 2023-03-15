package org.ssssssss.magicapi.servlet.jakarta;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.ssssssss.magicapi.core.config.MagicAPIProperties;
import org.ssssssss.magicapi.core.interceptor.AuthorizationInterceptor;
import org.ssssssss.magicapi.core.servlet.MagicRequestContextHolder;

import java.util.List;

@Configuration
public class MagicJakartaServletConfiguration implements WebMvcConfigurer {

	private final MagicAPIProperties properties;

	private final MagicJakartaCorsFilter magicCorsFilter = new MagicJakartaCorsFilter();

	private final ObjectProvider<MagicJakartaWebRequestInterceptor> magicWebRequestInterceptorProvider;

	public MagicJakartaServletConfiguration(MagicAPIProperties properties, ObjectProvider<MagicJakartaWebRequestInterceptor> magicWebRequestInterceptorProvider) {
		this.properties = properties;
		this.magicWebRequestInterceptorProvider = magicWebRequestInterceptorProvider;
	}

	@Bean
	public MagicRequestContextHolder magicRequestContextHolder(MultipartResolver multipartResolver){
		return new MagicJakartaRequestContextHolder(multipartResolver);
	}

	@Bean
	public MagicJakartaWebRequestInterceptor magicWebRequestInterceptor(AuthorizationInterceptor authorizationInterceptor){
		return new MagicJakartaWebRequestInterceptor(properties.isSupportCrossDomain() ? magicCorsFilter : null, authorizationInterceptor);
	}

	@Bean
	public MagicJakartaResponseExtension magicJakartaResponseExtension() {
		return new MagicJakartaResponseExtension();
	}


	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(new MagicJakartaHttpServletRequest.ArgumentsResolver());
		resolvers.add(new MagicJakartaHttpServletResponse.ArgumentsResolver());
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(magicWebRequestInterceptorProvider.getObject()).addPathPatterns("/**");
	}

	@Bean
	@ConditionalOnProperty(prefix = "magic-api", value = "support-cross-domain", havingValue = "true", matchIfMissing = true)
	public FilterRegistrationBean<MagicJakartaCorsFilter> magicCorsFilterRegistrationBean() {
		FilterRegistrationBean<MagicJakartaCorsFilter> registration = new FilterRegistrationBean<>(magicCorsFilter);
		registration.addUrlPatterns("/*");
		registration.setName("Magic Cors Filter");
		registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return registration;
	}
}
