package org.ssssssss.magicapi.servlet.javaee;

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
public class MagicJavaEEServletConfiguration implements WebMvcConfigurer {

	private final MagicAPIProperties properties;

	private final ObjectProvider<MagicJavaEEWebRequestInterceptor> magicWebRequestInterceptorProvider;


	public MagicJavaEEServletConfiguration(MagicAPIProperties properties, ObjectProvider<MagicJavaEEWebRequestInterceptor> magicWebRequestInterceptorProvider) {
		this.properties = properties;
		this.magicWebRequestInterceptorProvider = magicWebRequestInterceptorProvider;
	}

	private MagicJavaEECorsFilter magicCorsFilter = new MagicJavaEECorsFilter();

	@Bean
	public MagicRequestContextHolder magicRequestContextHolder(MultipartResolver multipartResolver){
		return new MagicJavaEERequestContextHolder(multipartResolver);
	}

	@Bean
	public MagicJavaEEWebRequestInterceptor magicWebRequestInterceptor(AuthorizationInterceptor authorizationInterceptor){
		return new MagicJavaEEWebRequestInterceptor(properties.isSupportCrossDomain() ? magicCorsFilter : null, authorizationInterceptor);
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(magicWebRequestInterceptorProvider.getObject()).addPathPatterns("/**");
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(0, new MagicJavaEEHttpServletRequest.ArgumentsResolver());
		resolvers.add(0, new MagicJavaEEHttpServletResponse.ArgumentsResolver());
	}

	@Bean
	public MagicJavaEEResponseExtension magicJavaEEResponseExtension() {
		return new MagicJavaEEResponseExtension();
	}

	@Bean
	@ConditionalOnProperty(prefix = "magic-api", value = "support-cross-domain", havingValue = "true", matchIfMissing = true)
	public FilterRegistrationBean<MagicJavaEECorsFilter> magicCorsFilterRegistrationBean() {
		FilterRegistrationBean<MagicJavaEECorsFilter> registration = new FilterRegistrationBean<>(magicCorsFilter);
		registration.addUrlPatterns("/*");
		registration.setName("Magic Cors Filter");
		registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return registration;
	}
}
