package org.ssssssss.magicapi.utils;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.ssssssss.magicapi.core.context.RequestContext;
import org.ssssssss.magicapi.core.config.Constants;
import org.ssssssss.magicapi.core.context.MagicUser;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Optional;

/**
 * Web相关工具类
 *
 * @author mxd
 */
public class WebUtils {

	public static Optional<HttpServletRequest> getRequest() {
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		if (requestAttributes instanceof ServletRequestAttributes) {
			return Optional.of(((ServletRequestAttributes) requestAttributes).getRequest());
		}
		return Optional.ofNullable(RequestContext.getHttpServletRequest());
	}

	public static String currentUserName() {
		Optional<HttpServletRequest> request = getRequest();
		return request.map(r -> (MagicUser) r.getAttribute(Constants.ATTRIBUTE_MAGIC_USER))
				.map(MagicUser::getUsername)
				.orElseGet(() -> request.map(HttpServletRequest::getUserPrincipal)
						.map(Principal::getName)
						.orElse(null)
				);
	}
}
