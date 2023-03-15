package org.ssssssss.magicapi.utils;

import org.ssssssss.magicapi.core.config.Constants;
import org.ssssssss.magicapi.core.context.MagicUser;
import org.ssssssss.magicapi.core.servlet.MagicHttpServletRequest;
import org.ssssssss.magicapi.core.servlet.MagicRequestContextHolder;

import java.security.Principal;
import java.util.Optional;

/**
 * Web相关工具类
 *
 * @author mxd
 */
public class WebUtils {

	public static MagicRequestContextHolder magicRequestContextHolder;

	public static String currentUserName() {
		Optional<MagicHttpServletRequest> request = Optional.ofNullable(magicRequestContextHolder.getRequest());
		return request.map(r -> (MagicUser) r.getAttribute(Constants.ATTRIBUTE_MAGIC_USER))
				.map(MagicUser::getUsername)
				.orElseGet(() -> request.map(MagicHttpServletRequest::getUserPrincipal)
						.map(Principal::getName)
						.orElse(null)
				);
	}
}
